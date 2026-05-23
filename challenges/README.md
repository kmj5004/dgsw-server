# Verified challenge content

각 폴더에는 챌린지 하나의 메타데이터·스켈레톤·숨긴 testbench·검증용 정답 회로가 들어 있다.

학습 흐름은 [`CURRICULUM.md`](CURRICULUM.md) 참고.

| 슬러그 | 제목 | 난이도 | 벡터 | Stage |
|---|---|---|---|---|
| `gate-buf` | Buffer (Passthrough) | EASY | 2 | 0 |
| `gate-not` | NOT Gate | EASY | 2 | 0 |
| `gate-and` | AND Gate | EASY | 4 | 0 |
| `gate-or` | OR Gate | EASY | 4 | 0 |
| `gate-xor` | XOR Gate | EASY | 4 | 0 |
| `gate-nand` | NAND Gate | EASY | 4 | 0 |
| `half-adder` | Half Adder | EASY | 4 | 1 |
| `full-adder` | Full Adder | EASY | 8 | 1 |
| `mux-2-to-1` | 2-to-1 MUX | EASY | 4 | 1 |
| `decoder-2-to-4` | 2-to-4 Decoder | EASY | 4 | 1 |
| `mux-4-to-1` | 4-to-1 MUX | MEDIUM | 4 | 1 |
| `bit-not-4` | 4-bit Inverter | EASY | 5 | 1 |
| `comparator-1bit` | 1-bit Comparator | EASY | 4 | 2 |
| `adder-4bit` | 4-bit Adder | MEDIUM | 6 | 2 |
| `subtractor-4bit` | 4-bit Subtractor | MEDIUM | 5 | 2 |
| `parity-4bit` | 4-bit Odd Parity | EASY | 6 | 2 |
| `dff-with-rst` | D Flip-Flop with Reset | EASY | 5 | 3 |
| `counter-4bit` | 4-bit Counter | MEDIUM | 5 | 3 |
| `seq-detect-1101` | 1101 Sequence Detector | HARD | 8 | 4 |

## 디렉토리 구조

```
challenges/
├── <slug>/
│   ├── meta.json     # 슬러그·제목·설명·태그·testVectors (Verilog 외 모든 메타)
│   ├── skeleton.v    # 사용자가 시작할 빈 모듈
│   ├── testbench.v   # 숨긴 testbench. ::HDLJUDGE_VEC:: 마커로 출력
│   └── reference.v   # 정답 회로 (DB에 들어가지 않음 — 검증용)
├── verify.sh         # reference + testbench가 expectedJson과 일치하는지 docker로 확인
└── seed.sh           # 실 서버에 ADMIN 권한으로 일괄 POST
```

## 워크플로

### 1. 로컬에서 정답·testbench 검증

```sh
./challenges/verify.sh                    # 전체
./challenges/verify.sh half-adder         # 단일
```

`hdl-judge/worker-verilog:latest` 이미지가 빌드되어 있어야 한다 (`docker build -t hdl-judge/worker-verilog:latest docker/worker-verilog`).

### 2. 서버에 등록

서버를 띄우고 ADMIN 계정을 준비한다. 가입은 API로, ADMIN 승격은 DB로:

```sh
# 가입
curl -X POST http://localhost:8080/api/auth/signup \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@example.com","password":"hunter2hunter2"}'

# 승격 (실행 중인 MySQL 컨테이너 이름이 hdljudge-mysql 일 때)
docker exec hdljudge-mysql mysql -u hdljudge -phdljudge hdljudge \
  -e "UPDATE users SET role='ADMIN' WHERE email='admin@example.com';"
```

이후 한 줄:

```sh
BASE_URL=http://localhost:8080 \
ADMIN_EMAIL=admin@example.com \
ADMIN_PASSWORD=hunter2hunter2 \
  ./challenges/seed.sh
```

같은 슬러그가 이미 있으면 409로 건너뛰므로 반복 실행해도 안전하다.

## 새 챌린지 추가하기

1. `challenges/<new-slug>/` 디렉토리 생성
2. `skeleton.v`, `testbench.v`, `reference.v`, `meta.json` 작성
3. `testbench.v` 출력 형식: 한 벡터당 `$display("::HDLJUDGE_VEC::ordering=N::output={...JSON...}", ...)` 한 줄
4. `meta.json` 의 `testVectors[*].expectedJson` 은 정답 회로의 출력과 의미적으로 일치해야 함
5. `./challenges/verify.sh <new-slug>` 으로 통과 확인
6. `./challenges/seed.sh` 로 등록
