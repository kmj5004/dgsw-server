module tb;
    reg a, b;
    wire sum, carry;
    half_adder uut(.a(a), .b(b), .sum(sum), .carry(carry));
    initial begin
        a=0; b=0; #1 $display("::HDLJUDGE_VEC::ordering=1::output={\"sum\":%0d,\"carry\":%0d}", sum, carry);
        a=0; b=1; #1 $display("::HDLJUDGE_VEC::ordering=2::output={\"sum\":%0d,\"carry\":%0d}", sum, carry);
        a=1; b=0; #1 $display("::HDLJUDGE_VEC::ordering=3::output={\"sum\":%0d,\"carry\":%0d}", sum, carry);
        a=1; b=1; #1 $display("::HDLJUDGE_VEC::ordering=4::output={\"sum\":%0d,\"carry\":%0d}", sum, carry);
        $finish;
    end
endmodule
