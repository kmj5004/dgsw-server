module subtractor4(input [3:0] a, input [3:0] b, output [3:0] diff, output borrow);
    wire [4:0] r = {1'b0, a} - {1'b0, b};
    assign diff   = r[3:0];
    assign borrow = r[4];
endmodule
