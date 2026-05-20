# worker-verilog

일회성 채점 워커 이미지. Spring 앱이 Docker Java Client 로 동적 생성한다.

## 빌드

```bash
docker build -t hdl-judge/worker-verilog:latest docker/worker-verilog
```

## 수동 시뮬레이션 예시

```bash
mkdir -p /tmp/run && cat > /tmp/run/user.v <<'EOF'
module half_adder(input a, b, output sum, carry);
  assign sum   = a ^ b;
  assign carry = a & b;
endmodule
EOF
cat > /tmp/run/testbench.v <<'EOF'
module tb;
  reg a, b; wire sum, carry;
  half_adder dut(a, b, sum, carry);
  initial begin
    a=0; b=0; #1 $display("%b%b%b%b", a, b, sum, carry);
    a=0; b=1; #1 $display("%b%b%b%b", a, b, sum, carry);
    a=1; b=0; #1 $display("%b%b%b%b", a, b, sum, carry);
    a=1; b=1; #1 $display("%b%b%b%b", a, b, sum, carry);
    $finish;
  end
endmodule
EOF

docker run --rm --network=none --read-only \
    --tmpfs /tmp:rw,size=16m,mode=1777 \
    --memory=256m --cpus=0.5 --pids-limit=64 \
    --security-opt=no-new-privileges \
    -v /tmp/run:/work:ro \
    hdl-judge/worker-verilog:latest \
    "iverilog -o /tmp/sim /work/user.v /work/testbench.v && vvp /tmp/sim"
```

## 합성 + 시각화 예시

```bash
docker run --rm --read-only \
    --tmpfs /tmp:rw,size=16m,mode=1777 \
    --memory=512m --cpus=1 \
    -v /tmp/run:/work:ro \
    hdl-judge/worker-verilog:latest \
    "yosys -q -p 'read_verilog /work/user.v; synth; write_json /tmp/out.json' && cat /tmp/out.json"
```

netlistsvg 변환은 별도 단계 (Node.js 환경) — 추후 분리 워커 이미지로 추가 예정.
