module tb;
    reg a, b, sel;
    wire y;
    mux2 uut(.a(a), .b(b), .sel(sel), .y(y));
    initial begin
        a=0; b=1; sel=0; #1 $display("::HDLJUDGE_VEC::ordering=1::output={\"y\":%0d}", y);
        a=0; b=1; sel=1; #1 $display("::HDLJUDGE_VEC::ordering=2::output={\"y\":%0d}", y);
        a=1; b=0; sel=0; #1 $display("::HDLJUDGE_VEC::ordering=3::output={\"y\":%0d}", y);
        a=1; b=0; sel=1; #1 $display("::HDLJUDGE_VEC::ordering=4::output={\"y\":%0d}", y);
        $finish;
    end
endmodule
