module tb;
    reg a, b, cin;
    wire sum, cout;
    full_adder uut(.a(a), .b(b), .cin(cin), .sum(sum), .cout(cout));
    initial begin
        {a,b,cin}=3'b000; #1 $display("::HDLJUDGE_VEC::ordering=1::output={\"sum\":%0d,\"cout\":%0d}", sum, cout);
        {a,b,cin}=3'b001; #1 $display("::HDLJUDGE_VEC::ordering=2::output={\"sum\":%0d,\"cout\":%0d}", sum, cout);
        {a,b,cin}=3'b010; #1 $display("::HDLJUDGE_VEC::ordering=3::output={\"sum\":%0d,\"cout\":%0d}", sum, cout);
        {a,b,cin}=3'b011; #1 $display("::HDLJUDGE_VEC::ordering=4::output={\"sum\":%0d,\"cout\":%0d}", sum, cout);
        {a,b,cin}=3'b100; #1 $display("::HDLJUDGE_VEC::ordering=5::output={\"sum\":%0d,\"cout\":%0d}", sum, cout);
        {a,b,cin}=3'b101; #1 $display("::HDLJUDGE_VEC::ordering=6::output={\"sum\":%0d,\"cout\":%0d}", sum, cout);
        {a,b,cin}=3'b110; #1 $display("::HDLJUDGE_VEC::ordering=7::output={\"sum\":%0d,\"cout\":%0d}", sum, cout);
        {a,b,cin}=3'b111; #1 $display("::HDLJUDGE_VEC::ordering=8::output={\"sum\":%0d,\"cout\":%0d}", sum, cout);
        $finish;
    end
endmodule
