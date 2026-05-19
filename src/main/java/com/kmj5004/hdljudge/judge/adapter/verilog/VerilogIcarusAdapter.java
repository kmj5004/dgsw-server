package com.kmj5004.hdljudge.judge.adapter.verilog;

import com.kmj5004.hdljudge.common.enums.Language;
import com.kmj5004.hdljudge.common.error.ApiException;
import com.kmj5004.hdljudge.common.error.ErrorCode;
import com.kmj5004.hdljudge.judge.JudgeProperties;
import com.kmj5004.hdljudge.judge.adapter.HdlAdapter;
import com.kmj5004.hdljudge.judge.adapter.ResourceLimits;
import com.kmj5004.hdljudge.judge.adapter.SimulationOutcome;
import com.kmj5004.hdljudge.judge.adapter.SynthesisOptions;
import com.kmj5004.hdljudge.judge.adapter.SynthesisOutcome;
import com.kmj5004.hdljudge.judge.adapter.WorkerContainerRunner;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class VerilogIcarusAdapter implements HdlAdapter {

    private static final int STDERR_TAIL_LIMIT = 4096;

    private final WorkerContainerRunner runner;
    private final JudgeProperties props;
    private final ObjectMapper objectMapper;

    @Override
    public Language language() {
        return Language.VERILOG;
    }

    @Override
    public SimulationOutcome simulate(String userCode, String testbench, ResourceLimits limits) {
        Path workDir = runner.createWorkspace("hdljudge-sim-");
        try {
            Files.writeString(workDir.resolve("user.v"), userCode);
            Files.writeString(workDir.resolve("testbench.v"), testbench);
        } catch (IOException e) {
            runner.deleteWorkspaceQuietly(workDir);
            throw new ApiException(ErrorCode.JUDGE_FAILURE, "Failed to prepare workspace", e);
        }

        try {
            WorkerContainerRunner.Result run = runner.run(
                props.docker().workerImage(),
                "iverilog -o /tmp/sim user.v testbench.v && vvp /tmp/sim",
                workDir, null, limits, false
            );

            if (run.timedOut()) {
                return new SimulationOutcome(
                    SimulationOutcome.Status.TIMEOUT,
                    run.stdout(), tail(run.stderr()), -1, run.wallTimeMs()
                );
            }
            SimulationOutcome.Status status = run.exitCode() == 0
                ? SimulationOutcome.Status.OK
                : SimulationOutcome.Status.ERROR;
            return new SimulationOutcome(
                status, run.stdout(), tail(run.stderr()), run.exitCode(), run.wallTimeMs()
            );
        } finally {
            runner.deleteWorkspaceQuietly(workDir);
        }
    }

    @Override
    public SynthesisOutcome synthesize(String userCode, SynthesisOptions options) {
        Path workDir = runner.createWorkspace("hdljudge-syn-");
        Path outDir = runner.createWorkspace("hdljudge-syn-out-");
        try {
            Files.writeString(workDir.resolve("user.v"), userCode);
        } catch (IOException e) {
            runner.deleteWorkspaceQuietly(workDir);
            runner.deleteWorkspaceQuietly(outDir);
            throw new ApiException(ErrorCode.SYNTHESIS_FAILURE, "Failed to prepare workspace", e);
        }


        ResourceLimits limits = new ResourceLimits(
            0L,
            Math.max(props.limits().defaultWallTimeLimitMs(), 15_000),
            Math.max(props.limits().defaultMemoryLimitMb(), 512)
        );

        try {
            WorkerContainerRunner.Result run = runner.run(
                props.docker().workerImage(),
                "yosys -p 'read_verilog user.v; synth -auto-top; write_json /out/circuit.json' >/out/yosys.log 2>&1 "
                    + "&& netlistsvg /out/circuit.json -o /out/circuit.svg 2>/out/netlistsvg.err",
                workDir, outDir, limits, false
            );

            if (run.timedOut()) {
                return SynthesisOutcome.failure("synthesis timeout");
            }
            if (run.exitCode() != 0) {
                String stderr = readQuietly(outDir.resolve("yosys.log"))
                    + readQuietly(outDir.resolve("netlistsvg.err"));
                return SynthesisOutcome.failure(tail(stderr));
            }

            String json = readQuietly(outDir.resolve("circuit.json"));
            String svg = readQuietly(outDir.resolve("circuit.svg"));
            if (json.isEmpty() || svg.isEmpty()) {
                return SynthesisOutcome.failure("synthesis produced empty output");
            }

            CellCounts counts = countCells(json);
            int max = options == null ? Integer.MAX_VALUE : Math.max(options.maxGateCount(), 0);
            if (max > 0 && counts.total() > max) {
                log.info("Synthesis exceeds gate threshold: {} > {} (svg dropped)", counts.total(), max);
                return new SynthesisOutcome(true, null, json, counts.gateCount(), counts.ffCount(),
                    "gate count " + counts.total() + " exceeds threshold " + max);
            }
            return SynthesisOutcome.success(svg, json, counts.gateCount(), counts.ffCount());
        } finally {
            runner.deleteWorkspaceQuietly(workDir);
            runner.deleteWorkspaceQuietly(outDir);
        }
    }

    private CellCounts countCells(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            int total = 0, ffs = 0;
            for (JsonNode module : root.path("modules")) {
                JsonNode cells = module.path("cells");
                for (JsonNode cell : cells) {
                    total++;
                    String type = cell.path("type").asText("");
                    if (isFlipFlop(type)) {
                        ffs++;
                    }
                }
            }
            return new CellCounts(total - ffs, ffs);
        } catch (RuntimeException e) {
            log.warn("Failed to parse synthesis JSON", e);
            return new CellCounts(0, 0);
        }
    }

    private static boolean isFlipFlop(String type) {
        if (type == null || type.isEmpty()) {
            return false;
        }
        String lower = type.toLowerCase();
        return lower.startsWith("$_dff") || lower.startsWith("$dff")
            || lower.contains("_dff_") || lower.contains("$_sdff") || lower.contains("$sdff");
    }

    private String readQuietly(Path path) {
        try {
            return Files.exists(path) ? Files.readString(path) : "";
        } catch (IOException e) {
            return "";
        }
    }

    private String tail(String s) {
        if (s == null) {
            return "";
        }
        return s.length() > STDERR_TAIL_LIMIT ? s.substring(s.length() - STDERR_TAIL_LIMIT) : s;
    }

    private record CellCounts(int gateCount, int ffCount) {
        int total() {
            return gateCount + ffCount;
        }
    }
}
