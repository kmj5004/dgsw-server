package com.kmj5004.hdljudge.judge.adapter.vhdl;

import com.kmj5004.hdljudge.common.enums.Language;
import com.kmj5004.hdljudge.common.error.ApiException;
import com.kmj5004.hdljudge.common.error.ErrorCode;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;











@Slf4j
@Component
@RequiredArgsConstructor
public class VhdlGhdlAdapter implements HdlAdapter {

    private static final int STDERR_TAIL_LIMIT = 4096;

    private final WorkerContainerRunner runner;

    @Value("${hdljudge.judge.docker.vhdl-image:hdl-judge/worker-vhdl:latest}")
    private String image;

    @Override
    public Language language() {
        return Language.VHDL;
    }

    @Override
    public SimulationOutcome simulate(String userCode, String testbench, ResourceLimits limits) {
        Path workDir = runner.createWorkspace("hdljudge-vhdl-sim-");
        try {
            Files.writeString(workDir.resolve("user.vhdl"), userCode);
            Files.writeString(workDir.resolve("testbench.vhdl"), testbench);
        } catch (IOException e) {
            runner.deleteWorkspaceQuietly(workDir);
            throw new ApiException(ErrorCode.JUDGE_FAILURE, "Failed to prepare VHDL workspace", e);
        }

        try {


            String cmd = "cd /tmp && cp /work/*.vhdl . "
                + "&& ghdl -a user.vhdl "
                + "&& ghdl -a testbench.vhdl "
                + "&& ghdl -e tb "
                + "&& ghdl -r tb --stop-time=1ms";

            WorkerContainerRunner.Result run = runner.run(
                image, cmd, workDir, null, limits,  true);

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

        throw new UnsupportedOperationException(
            "VHDL synthesis 는 아직 미지원 (ghdl-yosys-plugin 통합 v3 예정).");
    }

    private String tail(String s) {
        if (s == null) {
            return "";
        }
        return s.length() > STDERR_TAIL_LIMIT ? s.substring(s.length() - STDERR_TAIL_LIMIT) : s;
    }
}
