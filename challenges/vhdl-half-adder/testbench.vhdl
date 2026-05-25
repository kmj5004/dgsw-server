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
