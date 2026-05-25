library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

entity adder4 is
    port (
        a, b : in  std_logic_vector(3 downto 0);
        s    : out std_logic_vector(4 downto 0)
    );
end entity;

architecture rtl of adder4 is
begin
    s <= std_logic_vector(unsigned('0' & a) + unsigned('0' & b));
end architecture;
