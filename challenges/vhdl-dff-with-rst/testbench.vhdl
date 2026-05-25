library ieee;
use ieee.std_logic_1164.all;
use std.textio.all;

entity tb is end entity;

architecture sim of tb is
    signal clk, rst, d, q : std_logic := '0';

    function s2c(s : std_logic) return character is
    begin
        if s = '1' then return '1'; else return '0'; end if;
    end function;

    procedure print_vec(ord : integer; q_bit : std_logic) is
        variable l : line;
    begin
        write(l, string'("::HDLJUDGE_VEC::ordering="));
        write(l, ord);
        write(l, string'("::output={""q"":"));
        write(l, s2c(q_bit));
        write(l, string'("}"));
        writeline(output, l);
    end procedure;
begin
    uut: entity work.dff_rst port map(clk => clk, rst => rst, d => d, q => q);


    clk_proc: process
    begin
        clk <= '0'; wait for 5 ns;
        clk <= '1'; wait for 5 ns;
    end process;

    stim_proc: process
    begin

        rst <= '1'; d <= '1';
        wait until rising_edge(clk);
        wait for 1 ns;
        print_vec(1, q);


        rst <= '0'; d <= '1';
        wait until rising_edge(clk);
        wait for 1 ns;
        print_vec(2, q);


        d <= '0';
        wait until rising_edge(clk);
        wait for 1 ns;
        print_vec(3, q);


        d <= '1';
        wait until rising_edge(clk);
        wait for 1 ns;
        print_vec(4, q);


        rst <= '1';
        wait until rising_edge(clk);
        wait for 1 ns;
        print_vec(5, q);

        wait;
    end process;
end architecture;
