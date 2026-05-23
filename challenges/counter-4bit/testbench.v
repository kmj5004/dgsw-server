module tb;
    reg clk = 0;
    reg rst;
    wire [3:0] count;
    counter4 uut(.clk(clk), .rst(rst), .count(count));
    always #5 clk = ~clk;
    initial begin

        @(negedge clk); rst = 1;
        @(posedge clk); #1;
        $display("::HDLJUDGE_VEC::ordering=1::output={\"count\":%0d}", count);


        @(negedge clk); rst = 0;
        @(posedge clk); #1;
        $display("::HDLJUDGE_VEC::ordering=2::output={\"count\":%0d}", count);


        repeat (3) @(posedge clk); #1;
        $display("::HDLJUDGE_VEC::ordering=3::output={\"count\":%0d}", count);


        repeat (5) @(posedge clk); #1;
        $display("::HDLJUDGE_VEC::ordering=4::output={\"count\":%0d}", count);


        repeat (7) @(posedge clk); #1;
        $display("::HDLJUDGE_VEC::ordering=5::output={\"count\":%0d}", count);

        $finish;
    end
endmodule
