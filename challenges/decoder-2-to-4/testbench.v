module tb;
    reg [1:0] sel;
    wire [3:0] y;
    decoder2to4 uut(.sel(sel), .y(y));
    initial begin
        sel=2'd0; #1 $display("::HDLJUDGE_VEC::ordering=1::output={\"y\":%0d}", y);
        sel=2'd1; #1 $display("::HDLJUDGE_VEC::ordering=2::output={\"y\":%0d}", y);
        sel=2'd2; #1 $display("::HDLJUDGE_VEC::ordering=3::output={\"y\":%0d}", y);
        sel=2'd3; #1 $display("::HDLJUDGE_VEC::ordering=4::output={\"y\":%0d}", y);
        $finish;
    end
endmodule
