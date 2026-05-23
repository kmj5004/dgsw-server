module tb;
    reg a, b;
    wire eq, gt, lt;
    comparator1 uut(.a(a), .b(b), .eq(eq), .gt(gt), .lt(lt));
    initial begin
        a=0; b=0; #1 $display("::HDLJUDGE_VEC::ordering=1::output={\"eq\":%0d,\"gt\":%0d,\"lt\":%0d}", eq, gt, lt);
        a=0; b=1; #1 $display("::HDLJUDGE_VEC::ordering=2::output={\"eq\":%0d,\"gt\":%0d,\"lt\":%0d}", eq, gt, lt);
        a=1; b=0; #1 $display("::HDLJUDGE_VEC::ordering=3::output={\"eq\":%0d,\"gt\":%0d,\"lt\":%0d}", eq, gt, lt);
        a=1; b=1; #1 $display("::HDLJUDGE_VEC::ordering=4::output={\"eq\":%0d,\"gt\":%0d,\"lt\":%0d}", eq, gt, lt);
        $finish;
    end
endmodule
