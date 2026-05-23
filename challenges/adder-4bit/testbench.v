module tb;
    reg [3:0] a, b;
    wire [4:0] s;
    adder4 uut(.a(a), .b(b), .s(s));
    initial begin
        a=4'd0;  b=4'd0;  #1 $display("::HDLJUDGE_VEC::ordering=1::output={\"s\":%0d}", s);
        a=4'd1;  b=4'd2;  #1 $display("::HDLJUDGE_VEC::ordering=2::output={\"s\":%0d}", s);
        a=4'd7;  b=4'd8;  #1 $display("::HDLJUDGE_VEC::ordering=3::output={\"s\":%0d}", s);
        a=4'd15; b=4'd15; #1 $display("::HDLJUDGE_VEC::ordering=4::output={\"s\":%0d}", s);
        a=4'd10; b=4'd5;  #1 $display("::HDLJUDGE_VEC::ordering=5::output={\"s\":%0d}", s);
        a=4'd15; b=4'd1;  #1 $display("::HDLJUDGE_VEC::ordering=6::output={\"s\":%0d}", s);
        $finish;
    end
endmodule
