module tb;
    reg [3:0] a, b;
    wire [3:0] diff;
    wire borrow;
    subtractor4 uut(.a(a), .b(b), .diff(diff), .borrow(borrow));
    initial begin
        a=4'd5;  b=4'd3;  #1 $display("::HDLJUDGE_VEC::ordering=1::output={\"diff\":%0d,\"borrow\":%0d}", diff, borrow);
        a=4'd0;  b=4'd0;  #1 $display("::HDLJUDGE_VEC::ordering=2::output={\"diff\":%0d,\"borrow\":%0d}", diff, borrow);
        a=4'd2;  b=4'd5;  #1 $display("::HDLJUDGE_VEC::ordering=3::output={\"diff\":%0d,\"borrow\":%0d}", diff, borrow);
        a=4'd15; b=4'd1;  #1 $display("::HDLJUDGE_VEC::ordering=4::output={\"diff\":%0d,\"borrow\":%0d}", diff, borrow);
        a=4'd8;  b=4'd8;  #1 $display("::HDLJUDGE_VEC::ordering=5::output={\"diff\":%0d,\"borrow\":%0d}", diff, borrow);
        $finish;
    end
endmodule
