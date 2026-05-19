package com.kmj5004.hdljudge.judge.adapter;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.command.WaitContainerResultCallback;
import com.github.dockerjava.api.model.AccessMode;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.StreamType;
import com.github.dockerjava.api.model.Volume;
import com.kmj5004.hdljudge.common.error.ApiException;
import com.kmj5004.hdljudge.common.error.ErrorCode;
import com.kmj5004.hdljudge.judge.JudgeProperties;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;









@Slf4j
@Component
@RequiredArgsConstructor
public class WorkerContainerRunner {

    private static final long LOG_DRAIN_TIMEOUT_MS = 2_000;

    private final DockerClient docker;
    private final JudgeProperties props;

    public Path createWorkspace(String prefix) {
        try {
            Path dir = Files.createTempDirectory(prefix);

            try {
                Files.setPosixFilePermissions(dir, PosixFilePermissions.fromString("rwxrwxrwx"));
            } catch (UnsupportedOperationException ignored) {

            }
            return dir;
        } catch (IOException e) {
            throw new ApiException(ErrorCode.JUDGE_FAILURE, "Failed to create workspace", e);
        }
    }

    public void deleteWorkspaceQuietly(Path dir) {
        if (dir == null) {
            return;
        }
        try (var stream = Files.walk(dir)) {
            stream.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
        } catch (IOException e) {
            log.debug("Failed to delete workspace {}: {}", dir, e.toString());
        }
    }











    public Result run(
        String image,
        String cmd,
        Path workDir,
        Path outDir,
        ResourceLimits limits,
        boolean tmpfsExec
    ) {
        String containerId = null;
        long start = System.currentTimeMillis();
        try {
            HostConfig hostConfig = buildHostConfig(workDir, outDir, limits, tmpfsExec);

            CreateContainerResponse created = docker.createContainerCmd(image)
                .withCmd(cmd)
                .withWorkingDir("/work")
                .withHostConfig(hostConfig)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .withNetworkDisabled(true)
                .exec();
            containerId = created.getId();

            ByteArrayOutputStream stdoutBuf = new ByteArrayOutputStream();
            ByteArrayOutputStream stderrBuf = new ByteArrayOutputStream();
            LogCallback logCallback = new LogCallback(stdoutBuf, stderrBuf);

            docker.startContainerCmd(containerId).exec();
            docker.logContainerCmd(containerId)
                .withStdOut(true).withStdErr(true).withFollowStream(true)
                .exec(logCallback);

            WaitContainerResultCallback waitCallback = new WaitContainerResultCallback();
            docker.waitContainerCmd(containerId).exec(waitCallback);

            boolean finished;
            try {
                finished = waitCallback.awaitCompletion(limits.wallTimeLimitMs(), TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                finished = false;
            }

            if (!finished) {
                killQuietly(containerId);
                drainLogs(logCallback);
                long wall = System.currentTimeMillis() - start;
                return new Result(stdoutBuf.toString(StandardCharsets.UTF_8),
                    stderrBuf.toString(StandardCharsets.UTF_8), -1, wall, true);
            }

            drainLogs(logCallback);
            InspectContainerResponse inspect = docker.inspectContainerCmd(containerId).exec();
            int exit = exitCodeOf(inspect);
            long wall = System.currentTimeMillis() - start;
            return new Result(stdoutBuf.toString(StandardCharsets.UTF_8),
                stderrBuf.toString(StandardCharsets.UTF_8), exit, wall, false);
        } catch (RuntimeException e) {
            log.warn("Container run failure", e);
            throw new ApiException(ErrorCode.JUDGE_FAILURE, "Container run failure", e);
        } finally {
            if (containerId != null) {
                removeQuietly(containerId);
            }
        }
    }

    private HostConfig buildHostConfig(Path workDir, Path outDir, ResourceLimits limits, boolean tmpfsExec) {
        List<Bind> binds = new ArrayList<>();
        binds.add(new Bind(workDir.toAbsolutePath().toString(), new Volume("/work"), AccessMode.ro));
        if (outDir != null) {
            binds.add(new Bind(outDir.toAbsolutePath().toString(), new Volume("/out"), AccessMode.rw));
        }

        long memBytes = (long) Math.max(limits.memoryLimitMb(), props.docker().memoryLimitMb()) * 1024L * 1024L;
        long cpuPeriod = 100_000L;
        long cpuQuota = (long) props.docker().cpuQuotaPercent() * 1000L;

        String tmpfsOptions = tmpfsExec ? "rw,exec,size=64m" : "rw,size=64m";

        return HostConfig.newHostConfig()
            .withBinds(binds.toArray(new Bind[0]))
            .withTmpFs(Map.of("/tmp", tmpfsOptions))
            .withNetworkMode(props.docker().network())
            .withReadonlyRootfs(props.docker().readOnlyRoot())
            .withMemory(memBytes)
            .withMemorySwap(memBytes)
            .withPidsLimit((long) props.docker().pidsLimit())
            .withCpuPeriod(cpuPeriod)
            .withCpuQuota(cpuQuota)
            .withSecurityOpts(List.of("no-new-privileges:true"))
            .withAutoRemove(false);
    }

    private void drainLogs(LogCallback cb) {
        try {
            cb.awaitCompletion(LOG_DRAIN_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            try {
                cb.close();
            } catch (IOException ignored) {

            }
        }
    }

    private void killQuietly(String id) {
        try {
            docker.killContainerCmd(id).withSignal("KILL").exec();
        } catch (RuntimeException ignored) {

        }
    }

    private void removeQuietly(String id) {
        try {
            docker.removeContainerCmd(id).withForce(true).exec();
        } catch (RuntimeException e) {
            log.debug("removeContainer failed for {}: {}", id, e.toString());
        }
    }

    private int exitCodeOf(InspectContainerResponse inspect) {
        Long code = inspect.getState() == null ? null : inspect.getState().getExitCodeLong();
        return code == null ? -1 : code.intValue();
    }


    public record Result(String stdout, String stderr, int exitCode, long wallTimeMs, boolean timedOut) {
    }

    private static final class LogCallback extends ResultCallback.Adapter<Frame> {
        private final ByteArrayOutputStream stdout;
        private final ByteArrayOutputStream stderr;

        LogCallback(ByteArrayOutputStream stdout, ByteArrayOutputStream stderr) {
            this.stdout = stdout;
            this.stderr = stderr;
        }

        @Override
        public void onNext(Frame frame) {
            try {
                if (frame.getStreamType() == StreamType.STDERR) {
                    stderr.write(frame.getPayload());
                } else {
                    stdout.write(frame.getPayload());
                }
            } catch (IOException ignored) {

            }
        }
    }
}
