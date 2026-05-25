library ieee;
use ieee.std_logic_1164.all;

entity gate_xor is
    port (
        a, b : in  std_logic;
        y    : out std_logic
    );
end entity;

architecture rtl of gate_xor is
begin
    y <= a xor b;
end architecture;
