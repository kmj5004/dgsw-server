module tb;
    reg a;
    wire y;
    gate_buf uut(.a(a), .y(y));
    initial begin
        a=0; #1 $display("::HDLJUDGE_VEC::ordering=1::output={\"y\":%0d}", y);
        a=1; #1 $display("::HDLJUDGE_VEC::ordering=2::output={\"y\":%0d}", y);
        $finish;
    end
endmodule
