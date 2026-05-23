module tb;
    reg [3:0] in;
    reg [1:0] sel;
    wire y;
    mux4 uut(.in(in), .sel(sel), .y(y));
    initial begin
        in=4'b1010; sel=2'd0; #1 $display("::HDLJUDGE_VEC::ordering=1::output={\"y\":%0d}", y);
        in=4'b1010; sel=2'd1; #1 $display("::HDLJUDGE_VEC::ordering=2::output={\"y\":%0d}", y);
        in=4'b1010; sel=2'd2; #1 $display("::HDLJUDGE_VEC::ordering=3::output={\"y\":%0d}", y);
        in=4'b1010; sel=2'd3; #1 $display("::HDLJUDGE_VEC::ordering=4::output={\"y\":%0d}", y);
        $finish;
    end
endmodule
