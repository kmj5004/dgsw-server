module tb;
    reg [3:0] a, b;
    reg [1:0] op;
    wire [4:0] y;
    alu4 uut(.a(a), .b(b), .op(op), .y(y));
    initial begin
        a=4'd3;  b=4'd2;  op=2'b00; #1 $display("::HDLJUDGE_VEC::ordering=1::output={\"y\":%0d}", y);
        a=4'd15; b=4'd15; op=2'b00; #1 $display("::HDLJUDGE_VEC::ordering=2::output={\"y\":%0d}", y);
        a=4'd5;  b=4'd2;  op=2'b01; #1 $display("::HDLJUDGE_VEC::ordering=3::output={\"y\":%0d}", y);
        a=4'd0;  b=4'd1;  op=2'b01; #1 $display("::HDLJUDGE_VEC::ordering=4::output={\"y\":%0d}", y);
        a=4'd12; b=4'd10; op=2'b10; #1 $display("::HDLJUDGE_VEC::ordering=5::output={\"y\":%0d}", y);
        a=4'd15; b=4'd0;  op=2'b10; #1 $display("::HDLJUDGE_VEC::ordering=6::output={\"y\":%0d}", y);
        a=4'd12; b=4'd10; op=2'b11; #1 $display("::HDLJUDGE_VEC::ordering=7::output={\"y\":%0d}", y);
        a=4'd0;  b=4'd0;  op=2'b11; #1 $display("::HDLJUDGE_VEC::ordering=8::output={\"y\":%0d}", y);
        $finish;
    end
endmodule
