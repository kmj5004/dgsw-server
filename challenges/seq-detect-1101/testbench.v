module tb;
    reg clk = 0;
    reg rst, x;
    wire detected;
    seq_1101 uut(.clk(clk), .rst(rst), .x(x), .detected(detected));
    always #5 clk = ~clk;

    initial begin

        @(negedge clk); rst = 1; x = 0;
        @(posedge clk); #1;
        $display("::HDLJUDGE_VEC::ordering=1::output={\"detected\":%0d}", detected);


        @(negedge clk); rst = 0; x = 1;
        @(posedge clk); #1;
        $display("::HDLJUDGE_VEC::ordering=2::output={\"detected\":%0d}", detected);


        @(negedge clk); x = 1;
        @(posedge clk); #1;
        $display("::HDLJUDGE_VEC::ordering=3::output={\"detected\":%0d}", detected);


        @(negedge clk); x = 0;
        @(posedge clk); #1;
        $display("::HDLJUDGE_VEC::ordering=4::output={\"detected\":%0d}", detected);


        @(negedge clk); x = 1;
        @(posedge clk); #1;
        $display("::HDLJUDGE_VEC::ordering=5::output={\"detected\":%0d}", detected);


        @(negedge clk); x = 1;
        @(posedge clk); #1;
        $display("::HDLJUDGE_VEC::ordering=6::output={\"detected\":%0d}", detected);


        @(negedge clk); x = 0;
        @(posedge clk); #1;
        $display("::HDLJUDGE_VEC::ordering=7::output={\"detected\":%0d}", detected);


        @(negedge clk); x = 1;
        @(posedge clk); #1;
        $display("::HDLJUDGE_VEC::ordering=8::output={\"detected\":%0d}", detected);

        $finish;
    end
endmodule
