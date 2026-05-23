# 학습 커리큘럼

기초부터 응용까지 점진적으로 풀어보는 추천 순서. 각 단계는 직전 단계의 개념을 활용한다.

## Stage 0 — 첫 게이트

Verilog 한 줄짜리 회로. 한 챌린지가 한 연산자만 가르친다 — `module/input/output/assign/endmodule` 의 골격에 익숙해지는 게 목표.

| # | slug | 제목 | 키 개념 |
|---|---|---|---|
| 1 | `gate-buf` | Buffer (Passthrough) | 모듈 골격 + `assign y = a;` 한 줄 |
| 2 | `gate-not` | NOT Gate | 비트 NOT (`~`) |
| 3 | `gate-and` | AND Gate | 비트 AND (`&`), 진리표 4행 |
| 4 | `gate-or` | OR Gate | 비트 OR (`\|`) |
| 5 | `gate-xor` | XOR Gate | 비트 XOR (`^`) — half-adder 의 sum 비트가 보임 |
| 6 | `gate-nand` | NAND Gate | NOT + AND 함수 합성, 만능 게이트 개념 |

여섯 게이트가 모두 익숙하면 다음 단계는 이 게이트들을 **조합**해서 의미 있는 회로를 만든다.

## Stage 1 — 조합 회로 입문

게이트 두세 개를 합쳐서 만드는 가장 기본적인 회로. Stage 0 의 연산자가 같이 등장한다.

| # | slug | 제목 | 키 개념 |
|---|---|---|---|
| 1 | `half-adder` | Half Adder | XOR + AND 한 번에, 1비트 합·캐리 |
| 2 | `full-adder` | Full Adder | 캐리 입력 cin, 진리표 8행 |
| 3 | `mux-2-to-1` | 2-to-1 MUX | 삼항연산자 / 조건부 |
| 4 | `decoder-2-to-4` | 2-to-4 Decoder | one-hot, shift |
| 5 | `mux-4-to-1` | 4-to-1 MUX | 비트 슬라이싱 (`in[sel]`), 다중 입력 |
| 6 | `bit-not-4` | 4-bit Inverter | 4비트 벡터 (`[3:0]`) 첫 등장, 다비트 연산자 |

## Stage 2 — 조합 응용

여러 비트, 산술 연산, 비교 — Stage 1 의 게이트들이 더 큰 회로로 합쳐지는 단계.

| # | slug | 제목 | 키 개념 |
|---|---|---|---|
| 7 | `comparator-1bit` | 1-bit Comparator | 다중 출력 (eq/gt/lt) |
| 8 | `adder-4bit` | 4-bit Adder | 다비트 연산, carry-out |
| 9 | `subtractor-4bit` | 4-bit Subtractor | 5비트 확장 + borrow 추출, 2의 보수 직관 |
| 10 | `parity-4bit` | 4-bit Odd Parity | reduction XOR (`^a`), 패리티 생성기 |

## Stage 3 — 순차 회로 입문

`always @(posedge clk)` 와 non-blocking 할당(`<=`) — 시간이 흐르면 상태가 바뀌는 회로.

| # | slug | 제목 | 키 개념 |
|---|---|---|---|
| 11 | `dff-with-rst` | D Flip-Flop with Reset | clock edge, 동기 reset, `output reg` |
| 12 | `counter-4bit` | 4-bit Counter | 누산, wrap-around, 동기 reset 응용 |

## Stage 4 — FSM (유한 상태 기계)

여러 상태와 전이 규칙으로 입력 시퀀스에 반응하는 회로. 진짜 디지털 시스템의 핵심.

| # | slug | 제목 | 키 개념 |
|---|---|---|---|
| 13 | `seq-detect-1101` | 1101 시퀀스 검출기 | Moore FSM, 5-state 테이블, 오버랩 처리 |

## 추천 진행

1. Stage 0 → 1 → 2 를 순서대로 (각 챌린지 1~3분)
2. Stage 2 — 4-bit adder 가 Stage 1 의 full-adder 4개를 직렬 연결한 것임을 깨닫는 게 핵심
3. Stage 3 — DFF 전에 `iverilog -g2012 user.v testbench.v` 시뮬레이션 + waveform 을 한 번 손으로 그려보면 훨씬 빨리 이해됨
4. Stage 4 — 종이에 상태 다이어그램을 먼저 그린 다음 코딩하면 훨씬 안전. `case (state)` 안에서 `state <= ...` 만 쓰는 게 정형
5. 익숙해지면 Playground (`POST /api/playground/{simulate,synthesize}`) 로 자기 회로의 합성 결과(SVG)를 보면서 게이트 수를 줄여보기

## 다음에 추가할 만한 것 (TODO)

- Stage 0+: `gate-nor`, `gate-xnor` (NAND처럼 만능성 보여주기)
- Stage 2: 4-bit ALU (op 2비트로 add/sub/and/or 선택), 7-segment decoder
- Stage 3: 4-bit register (load enable), shift register (SISO/SIPO)
- Stage 4: traffic light FSM, 간단한 vending machine, "001" Mealy 검출기 (Moore vs Mealy 비교)

새 챌린지를 추가할 때는 [`./challenges/README.md`](README.md) 의 워크플로를 참고.
