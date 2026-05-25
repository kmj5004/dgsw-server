library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;
use std.textio.all;

entity tb is end entity;

architecture sim of tb is
    signal a, b : std_logic_vector(3 downto 0) := (others => '0');
    signal s    : std_logic_vector(4 downto 0);

    procedure print_vec(ord : integer; sum_vec : std_logic_vector) is
        variable l : line;
    begin
        write(l, string'("::HDLJUDGE_VEC::ordering="));
        write(l, ord);
        write(l, string'("::output={""s"":"));
        write(l, to_integer(unsigned(sum_vec)));
        write(l, string'("}"));
        writeline(output, l);
    end procedure;
begin
    uut: entity work.adder4 port map(a => a, b => b, s => s);
    process
    begin
        a <= "0000"; b <= "0000"; wait for 1 ns; print_vec(1, s);
        a <= "0001"; b <= "0010"; wait for 1 ns; print_vec(2, s);
        a <= "0111"; b <= "1000"; wait for 1 ns; print_vec(3, s);
        a <= "1111"; b <= "1111"; wait for 1 ns; print_vec(4, s);
        a <= "1010"; b <= "0101"; wait for 1 ns; print_vec(5, s);
        a <= "1111"; b <= "0001"; wait for 1 ns; print_vec(6, s);
        wait;
    end process;
end architecture;
