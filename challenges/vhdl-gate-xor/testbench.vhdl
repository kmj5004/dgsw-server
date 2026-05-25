library ieee;
use ieee.std_logic_1164.all;
use std.textio.all;

entity tb is end entity;

architecture sim of tb is
    signal a, b, y : std_logic := '0';

    function s2c(s : std_logic) return character is
    begin
        if s = '1' then return '1'; else return '0'; end if;
    end function;

    procedure print_vec(ord : integer; y_bit : std_logic) is
        variable l : line;
    begin
        write(l, string'("::HDLJUDGE_VEC::ordering="));
        write(l, ord);
        write(l, string'("::output={""y"":"));
        write(l, s2c(y_bit));
        write(l, string'("}"));
        writeline(output, l);
    end procedure;
begin
    uut: entity work.gate_xor port map(a => a, b => b, y => y);
    process
    begin
        a <= '0'; b <= '0'; wait for 1 ns; print_vec(1, y);
        a <= '0'; b <= '1'; wait for 1 ns; print_vec(2, y);
        a <= '1'; b <= '0'; wait for 1 ns; print_vec(3, y);
        a <= '1'; b <= '1'; wait for 1 ns; print_vec(4, y);
        wait;
    end process;
end architecture;
