module tb;
    reg [3:0] a;
    wire p;
    parity4 uut(.a(a), .p(p));
    initial begin
        a=4'd0;  #1 $display("::HDLJUDGE_VEC::ordering=1::output={\"p\":%0d}", p);
        a=4'd1;  #1 $display("::HDLJUDGE_VEC::ordering=2::output={\"p\":%0d}", p);
        a=4'd3;  #1 $display("::HDLJUDGE_VEC::ordering=3::output={\"p\":%0d}", p);
        a=4'd7;  #1 $display("::HDLJUDGE_VEC::ordering=4::output={\"p\":%0d}", p);
        a=4'd15; #1 $display("::HDLJUDGE_VEC::ordering=5::output={\"p\":%0d}", p);
        a=4'd10; #1 $display("::HDLJUDGE_VEC::ordering=6::output={\"p\":%0d}", p);
        $finish;
    end
endmodule
