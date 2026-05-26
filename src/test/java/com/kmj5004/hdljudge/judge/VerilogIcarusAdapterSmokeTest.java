package com.kmj5004.hdljudge.judge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.github.dockerjava.api.DockerClient;
import com.kmj5004.hdljudge.judge.adapter.ResourceLimits;
import com.kmj5004.hdljudge.judge.adapter.SimulationOutcome;
import com.kmj5004.hdljudge.judge.adapter.SynthesisOptions;
import com.kmj5004.hdljudge.judge.adapter.SynthesisOutcome;
import com.kmj5004.hdljudge.judge.adapter.verilog.VerilogIcarusAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;





@SpringBootTest
@ActiveProfiles("test")
class VerilogIcarusAdapterSmokeTest {

    private static final String USER_CODE = """
        module half_adder(input a, b, output sum, carry);
            assign sum   = a ^ b;
            assign carry = a & b;
        endmodule
        """;

    private static final String TESTBENCH = """
        module tb;
            reg a, b;
            wire sum, carry;
            half_adder uut(.a(a), .b(b), .sum(sum), .carry(carry));
            initial begin
                a=0; b=0; #1 $display("00 sum=%b carry=%b", sum, carry);
                a=0; b=1; #1 $display("01 sum=%b carry=%b", sum, carry);
                a=1; b=0; #1 $display("10 sum=%b carry=%b", sum, carry);
                a=1; b=1; #1 $display("11 sum=%b carry=%b", sum, carry);
                $finish;
            end
        endmodule
        """;

    @Autowired
    VerilogIcarusAdapter adapter;

    @Autowired
    DockerClient dockerClient;

    @Test
    void simulatesHalfAdder() {
        assumeTrue(dockerReachable(), "Docker daemon not reachable; skipping.");

        ResourceLimits limits = new ResourceLimits(100_000_000L, 5_000, 256);
        SimulationOutcome outcome = adapter.simulate(USER_CODE, TESTBENCH, limits);

        assertThat(outcome.status()).isEqualTo(SimulationOutcome.Status.OK);
        assertThat(outcome.exitCode()).isZero();
        assertThat(outcome.stdout())
            .contains("00 sum=0 carry=0")
            .contains("01 sum=1 carry=0")
            .contains("10 sum=1 carry=0")
            .contains("11 sum=0 carry=1");
        assertThat(outcome.wallTimeMs()).isPositive();
    }

    @Test
    void timesOutOnInfiniteLoop() {
        assumeTrue(dockerReachable(), "Docker daemon not reachable; skipping.");

        String userCode = "module dummy; endmodule\n";
        String tb = """
            module tb;
                initial begin
                    forever #1 $display("tick");
                end
            endmodule
            """;

        ResourceLimits limits = new ResourceLimits(100_000_000L, 1_500, 256);
        SimulationOutcome outcome = adapter.simulate(userCode, tb, limits);

        assertThat(outcome.status()).isEqualTo(SimulationOutcome.Status.TIMEOUT);
    }

    @Test
    void compileErrorReturnsErrorStatus() {
        assumeTrue(dockerReachable(), "Docker daemon not reachable; skipping.");

        String userCode = "module broken( ;;; not real verilog\n";
        String tb = "module tb; initial $finish; endmodule\n";

        ResourceLimits limits = new ResourceLimits(100_000_000L, 5_000, 256);
        SimulationOutcome outcome = adapter.simulate(userCode, tb, limits);

        assertThat(outcome.status()).isEqualTo(SimulationOutcome.Status.ERROR);
        assertThat(outcome.exitCode()).isNotZero();
    }

    @Test
    void synthesizesHalfAdderToSvg() {
        assumeTrue(dockerReachable(), "Docker daemon not reachable; skipping.");

        SynthesisOutcome outcome = adapter.synthesize(USER_CODE, new SynthesisOptions(500));

        assertThat(outcome.ok()).isTrue();
        assertThat(outcome.svg()).isNotBlank().contains("<svg");
        assertThat(outcome.gateCount()).isPositive();
        assertThat(outcome.ffCount()).isZero();
        assertThat(outcome.json()).contains("half_adder");
    }

    @Test
    void synthesizeBrokenCodeReturnsFailure() {
        assumeTrue(dockerReachable(), "Docker daemon not reachable; skipping.");

        SynthesisOutcome outcome = adapter.synthesize("module broken( ;;; not real verilog\n",
            new SynthesisOptions(500));

        assertThat(outcome.ok()).isFalse();
        assertThat(outcome.svg()).isNull();
    }

    private boolean dockerReachable() {
        try {
            dockerClient.pingCmd().exec();
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }
}
