module tb;
    reg [3:0] a;
    wire [3:0] y;
    bit_not_4 uut(.a(a), .y(y));
    initial begin
        a=4'd0;  #1 $display("::HDLJUDGE_VEC::ordering=1::output={\"y\":%0d}", y);
        a=4'd15; #1 $display("::HDLJUDGE_VEC::ordering=2::output={\"y\":%0d}", y);
        a=4'd10; #1 $display("::HDLJUDGE_VEC::ordering=3::output={\"y\":%0d}", y);
        a=4'd5;  #1 $display("::HDLJUDGE_VEC::ordering=4::output={\"y\":%0d}", y);
        a=4'd1;  #1 $display("::HDLJUDGE_VEC::ordering=5::output={\"y\":%0d}", y);
        $finish;
    end
endmodule
