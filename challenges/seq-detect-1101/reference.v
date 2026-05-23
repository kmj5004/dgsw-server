module seq_1101(input clk, input rst, input x, output reg detected);
    reg [2:0] state;
    localparam S0 = 3'd0,
               S1 = 3'd1,
               S2 = 3'd2,
               S3 = 3'd3,
               S4 = 3'd4;

    always @(posedge clk) begin
        if (rst) begin
            state <= S0;
        end else begin
            case (state)
                S0: state <= x ? S1 : S0;
                S1: state <= x ? S2 : S0;
                S2: state <= x ? S2 : S3;
                S3: state <= x ? S4 : S0;
                S4: state <= x ? S2 : S0;
                default: state <= S0;
            endcase
        end
    end

    always @(*) detected = (state == S4);
endmodule
