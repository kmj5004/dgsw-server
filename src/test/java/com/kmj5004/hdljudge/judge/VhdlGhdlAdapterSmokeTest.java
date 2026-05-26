package com.kmj5004.hdljudge.judge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.github.dockerjava.api.DockerClient;
import com.kmj5004.hdljudge.judge.adapter.ResourceLimits;
import com.kmj5004.hdljudge.judge.adapter.SimulationOutcome;
import com.kmj5004.hdljudge.judge.adapter.vhdl.VhdlGhdlAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;





@SpringBootTest
@ActiveProfiles("test")
class VhdlGhdlAdapterSmokeTest {

    private static final String USER_CODE = """
        library ieee;
        use ieee.std_logic_1164.all;

        entity half_adder is
            port (
                a, b       : in  std_logic;
                sum, carry : out std_logic
            );
        end entity;

        architecture rtl of half_adder is
        begin
            sum   <= a xor b;
            carry <= a and b;
        end architecture;
        """;

    private static final String TESTBENCH = """
        library ieee;
        use ieee.std_logic_1164.all;
        use std.textio.all;

        entity tb is
        end entity;

        architecture sim of tb is
            signal a, b       : std_logic := '0';
            signal sum, carry : std_logic;

            function s2c(s : std_logic) return character is
            begin
                if s = '1' then return '1'; else return '0'; end if;
            end function;

            procedure print_vec(ord : integer; sum_bit, carry_bit : std_logic) is
                variable l : line;
            begin
                write(l, string'("::HDLJUDGE_VEC::ordering="));
                write(l, ord);
                write(l, string'("::output={""sum"":"));
                write(l, s2c(sum_bit));
                write(l, string'(",""carry"":"));
                write(l, s2c(carry_bit));
                write(l, string'("}"));
                writeline(output, l);
            end procedure;
        begin
            uut: entity work.half_adder port map(a => a, b => b, sum => sum, carry => carry);
            process
            begin
                a <= '0'; b <= '0'; wait for 1 ns; print_vec(1, sum, carry);
                a <= '0'; b <= '1'; wait for 1 ns; print_vec(2, sum, carry);
                a <= '1'; b <= '0'; wait for 1 ns; print_vec(3, sum, carry);
                a <= '1'; b <= '1'; wait for 1 ns; print_vec(4, sum, carry);
                wait;
            end process;
        end architecture;
        """;

    @Autowired
    VhdlGhdlAdapter adapter;

    @Autowired
    DockerClient dockerClient;

    @Test
    void simulatesVhdlHalfAdder() {
        assumeTrue(dockerReachable(), "Docker daemon not reachable; skipping.");

        ResourceLimits limits = new ResourceLimits(0L, 10_000, 256);
        SimulationOutcome outcome = adapter.simulate(USER_CODE, TESTBENCH, limits);

        assertThat(outcome.status()).isEqualTo(SimulationOutcome.Status.OK);
        assertThat(outcome.exitCode()).isZero();
        assertThat(outcome.stdout())
            .contains("::HDLJUDGE_VEC::ordering=1::output={\"sum\":0,\"carry\":0}")
            .contains("::HDLJUDGE_VEC::ordering=4::output={\"sum\":0,\"carry\":1}");
        assertThat(outcome.wallTimeMs()).isPositive();
    }

    @Test
    void brokenVhdlReturnsErrorStatus() {
        assumeTrue(dockerReachable(), "Docker daemon not reachable; skipping.");

        String userCode = "this is not valid VHDL\n";
        String tb = "entity tb is end; architecture x of tb is begin process begin wait; end process; end;\n";

        ResourceLimits limits = new ResourceLimits(0L, 10_000, 256);
        SimulationOutcome outcome = adapter.simulate(userCode, tb, limits);

        assertThat(outcome.status()).isEqualTo(SimulationOutcome.Status.ERROR);
        assertThat(outcome.exitCode()).isNotZero();
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
