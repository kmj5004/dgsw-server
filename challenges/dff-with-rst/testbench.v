module tb;
    reg clk = 0;
    reg rst, d;
    wire q;
    dff_rst uut(.clk(clk), .rst(rst), .d(d), .q(q));
    always #5 clk = ~clk;
    initial begin

        @(negedge clk); rst = 1; d = 1;
        @(posedge clk); #1;
        $display("::HDLJUDGE_VEC::ordering=1::output={\"q\":%0d}", q);


        @(negedge clk); rst = 0; d = 1;
        @(posedge clk); #1;
        $display("::HDLJUDGE_VEC::ordering=2::output={\"q\":%0d}", q);


        @(negedge clk); d = 0;
        @(posedge clk); #1;
        $display("::HDLJUDGE_VEC::ordering=3::output={\"q\":%0d}", q);


        @(negedge clk); d = 1;
        @(posedge clk); #1;
        $display("::HDLJUDGE_VEC::ordering=4::output={\"q\":%0d}", q);


        @(negedge clk); rst = 1;
        @(posedge clk); #1;
        $display("::HDLJUDGE_VEC::ordering=5::output={\"q\":%0d}", q);

        $finish;
    end
endmodule
