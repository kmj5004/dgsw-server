module alu4(input [3:0] a, input [3:0] b, input [1:0] op, output reg [4:0] y);
    always @(*) begin
        case (op)
            2'b00: y = {1'b0, a} + {1'b0, b};
            2'b01: y = {1'b0, a} - {1'b0, b};
            2'b10: y = {1'b0, a & b};
            2'b11: y = {1'b0, a | b};
            default: y = 5'd0;
        endcase
    end
endmodule
