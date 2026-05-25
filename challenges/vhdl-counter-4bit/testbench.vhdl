library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;
use std.textio.all;

entity tb is end entity;

architecture sim of tb is
    signal clk, rst : std_logic := '0';
    signal count    : std_logic_vector(3 downto 0);

    procedure print_vec(ord : integer; cnt_vec : std_logic_vector) is
        variable l : line;
    begin
        write(l, string'("::HDLJUDGE_VEC::ordering="));
        write(l, ord);
        write(l, string'("::output={""count"":"));
        write(l, to_integer(unsigned(cnt_vec)));
        write(l, string'("}"));
        writeline(output, l);
    end procedure;
begin
    uut: entity work.counter4 port map(clk => clk, rst => rst, count => count);

    clk_proc: process
    begin
        clk <= '0'; wait for 5 ns;
        clk <= '1'; wait for 5 ns;
    end process;

    stim_proc: process
    begin

        rst <= '1';
        wait until rising_edge(clk);
        wait for 1 ns;
        print_vec(1, count);


        rst <= '0';
        wait until rising_edge(clk);
        wait for 1 ns;
        print_vec(2, count);


        for i in 1 to 3 loop wait until rising_edge(clk); end loop;
        wait for 1 ns;
        print_vec(3, count);


        for i in 1 to 5 loop wait until rising_edge(clk); end loop;
        wait for 1 ns;
        print_vec(4, count);


        for i in 1 to 7 loop wait until rising_edge(clk); end loop;
        wait for 1 ns;
        print_vec(5, count);

        wait;
    end process;
end architecture;
