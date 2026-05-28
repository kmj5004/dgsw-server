# HDL Online Judge Platform

브라우저에서 Verilog 코드를 작성하면 서버가 격리된 Docker 컨테이너에서 [iverilog](https://steveicarus.github.io/iverilog/)로 시뮬레이션·자동 채점하고 [Yosys](https://yosyshq.net/yosys/)로 합성한 회로를 [netlistsvg](https://github.com/nturley/netlistsvg) 인라인 SVG로 시각화하는 학습용 온라인 저지.

> 대구소프트웨어마이스터고 학교 평가 프로젝트 · 2026-05-06 ~ 2026-05-28

## 핵심 기능

- 회원가입 / 로그인 / 토큰 회전 / replay-감지 폐기 (JWT, jti 클레임 포함)
- 챌린지 CRUD (ADMIN) · 공개 조회 (필터·페이지)
- Verilog / VHDL 코드 제출 → 비동기 격리 채점 (고정 워커 풀 + 큐, 백프레셔 시 429)
- 벡터별 채점 (`::HDLJUDGE_VEC::ordering=N::output={...}` 마커 기반) + 리더보드
- Verilog 합성: Yosys → netlistsvg 인라인 SVG, 코드 SHA-256 해시 캐시
- 자유 실습 Playground: simulate / synthesize
- 학습 경로 API: 단계별 챌린지 묶음과 사용자 진행도 조회
- 컨테이너 격리: network=none, read-only rootfs, mem/pids/cpu 제한, no-new-privileges, tmpfs `/tmp`

## 문서

- [`docs/PROJECT-BRIEF.md`](docs/PROJECT-BRIEF.md) — 기획서 (배경·사용자·UX·범위)
- [`docs/FUNCTIONAL-SPEC.md`](docs/FUNCTIONAL-SPEC.md) — **프론트엔드 개발자용 API 명세** (모든 엔드포인트·응답 모양·에러·상태 머신)
- [`docs/RESULT-REPORT.md`](docs/RESULT-REPORT.md) — 프로젝트 결과 보고서 (구현·개발 보조 도구·트러블슈팅·성과)
- [`docs/TROUBLESHOOTING.md`](docs/TROUBLESHOOTING.md) — 개발 중 만난 함정·측정 메모
- [`challenges/README.md`](challenges/README.md) — 챌린지 콘텐츠 추가·검증 워크플로

## 관련 저장소

- Backend: https://github.com/kmj5004/dgsw-server
- Frontend: https://github.com/kmj5004/dgsw-client

## 기술 스택

| 레이어 | 기술 |
| --- | --- |
| Language / Runtime | Java 21 (LTS) |
| Framework | Spring Boot 4.0.6 (Web MVC, Security, Data JPA, Validation, Actuator) |
| Build | Gradle (Kotlin DSL) |
| Persistence | MySQL 8 (운영), H2 in-memory (테스트), Flyway V1~V3, JPA/Hibernate 7.2 |
| Cache | Redis 7 (현재는 합성 캐시는 DB-inline, Redis는 향후 락·세션 용도) |
| Auth | jjwt 0.12 (HS256, jti 클레임, SHA-256 해시 refresh) |
| JSON | Jackson 3 (`tools.jackson.databind`) — Boot 4 기본 |
| HDL Tooling | iverilog 11, GHDL, Yosys 0.23, netlistsvg (Node.js) |
| Container Control | Docker Java Client 3.4 (앱이 채점 워커 컨테이너 동적 생성) |
| Test | JUnit 5 + AssertJ + Awaitility + Testcontainers (MySQL + Redis) |

## 빠른 시작

요구사항: JDK 21, Docker Desktop, `jq`

### 제출/평가용 Docker Compose

```bash
docker compose up --build
```

이 명령은 MySQL, Redis, Spring Boot 앱을 실행하고, 앱이 동적으로 사용하는 Verilog/VHDL 워커 이미지도 먼저 빌드한다.

| 무엇 | URL |
|---|---|
| API base | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui/index.html |
| OpenAPI spec | http://localhost:8080/v3/api-docs |
| Health | http://localhost:8080/actuator/health |

처음 실행한 DB에는 계정과 챌린지가 비어 있으므로, 시연용 데이터까지 자동 준비하려면 아래 bootstrap 방식을 사용한다.

### 개발/시연용 Bootstrap

```bash
./bootstrap.sh
```

이 한 줄이 다음을 자동으로 한다:

1. MySQL 8 + Redis 7 컨테이너 시작 (호스트 3306 점유 중이면 자동 3307 매핑)
2. iverilog + yosys + netlistsvg + GHDL 워커 이미지 빌드 (이미 있으면 skip)
3. Spring Boot 백그라운드 실행, `/actuator/health` 까지 대기
4. 기본 admin 계정 생성 + ADMIN 승격 (`admin@hdljudge.local` / `hunter2hunter2`)
5. 검증된 챌린지 27개 일괄 등록

재실행해도 안전 (idempotent) — 이미 떠 있는 건 그대로 두고 빠진 것만 채운다.

끝나면 다음이 준비됨:

| 무엇 | URL |
|---|---|
| API base | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui/index.html |
| OpenAPI spec | http://localhost:8080/v3/api-docs |
| Health | http://localhost:8080/actuator/health |
| 챌린지 목록 | http://localhost:8080/api/challenges |

환경변수로 admin 계정·base URL 변경 가능:
```bash
ADMIN_EMAIL=me@example.com ADMIN_PASSWORD=mysecret ./bootstrap.sh
```

### 수동으로 단계별 실행하고 싶다면

<details>
<summary>펼치기</summary>

```bash
# 1) 인프라
docker compose up -d mysql redis

# 2) 워커 이미지
docker build -t hdl-judge/worker-verilog:latest docker/worker-verilog
docker build -t hdl-judge/worker-vhdl:latest docker/worker-vhdl

# 3) 앱
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun

# 4) admin 만들고 승격
curl -X POST http://localhost:8080/api/auth/signup \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@example.com","password":"hunter2hunter2"}'
docker exec hdljudge-mysql mysql -u hdljudge -phdljudge hdljudge \
  -e "UPDATE users SET role='ADMIN' WHERE email='admin@example.com';"

# 5) 챌린지 시드
ADMIN_EMAIL=admin@example.com ADMIN_PASSWORD=hunter2hunter2 \
  ./challenges/seed.sh
```

</details>

호스트의 3306 포트가 다른 MySQL 에 점유돼 있으면 `docker-compose.yml` 의 mysql 포트 매핑을 `3307:3306` 으로 바꾸고 datasource URL 을 인라인 오버라이드한다 — 자세히는 [TROUBLESHOOTING](docs/TROUBLESHOOTING.md) 참고.

## 테스트

```bash
./gradlew test
```

주요 테스트 범위:

- `HdlJudgeApplicationTests` — context 로드 (H2)
- `VerilogIcarusAdapterSmokeTest` — **실제 worker 이미지** 로 simulate / synthesize 검증
- `VhdlGhdlAdapterSmokeTest` — **실제 worker 이미지** 로 VHDL simulate 검증
- `EndToEndIntegrationTest` — Testcontainers MySQL+Redis + stub 어댑터로 HTTP 레이어 끝까지 (14)

워커 smoke 테스트와 EndToEndIntegrationTest 는 Docker daemon 이 떠 있어야 한다.

## 디렉토리 구조

```
hdl-judge/
├── build.gradle.kts            # Boot 4 + JPA/Security/Redis + jjwt + docker-java
├── docker-compose.yml          # app + mysql + redis + worker image build
├── Dockerfile                  # 앱 멀티스테이지 빌드
├── docker/worker-verilog/      # iverilog + yosys + netlistsvg 워커 이미지
├── docker/worker-vhdl/         # GHDL VHDL 시뮬레이션 워커 이미지
├── challenges/                 # 검증된 챌린지 콘텐츠 + seed/verify 스크립트
│   ├── <slug>/{meta,skeleton.v,testbench.v,reference.v}
│   ├── verify.sh, seed.sh
├── docs/                       # 기획서·API 명세·트러블슈팅
└── src/
    ├── main/java/com/kmj5004/hdljudge/
    │   ├── auth/        # signup·login·refresh·logout
    │   ├── challenge/   # CRUD + 필터링
    │   ├── submission/  # 비동기 채점 endpoint
    │   ├── playground/  # 자유 simulate / synthesize
    │   ├── leaderboard/ # 랭킹
    │   ├── judge/       # 워커 풀, 어댑터, 출력 파서
    │   ├── security/    # JWT, 필터, SecurityConfig
    │   ├── domain/      # JPA 엔티티
    │   └── common/      # 응답 엔벨로프, 에러, BaseTimeEntity
    ├── main/resources/
    │   ├── application.yml      # local / docker / test 프로파일
    │   └── db/migration/V1~V3   # Flyway
    └── test/java/com/kmj5004/hdljudge/...
```

## REST API 요약

자세한 명세·요청·응답·에러는 **[docs/FUNCTIONAL-SPEC.md](docs/FUNCTIONAL-SPEC.md)**.

| Method | Path | 권한 | 설명 |
|---|---|---|---|
| POST | `/api/auth/signup` | Public | 회원가입 |
| POST | `/api/auth/login` | Public | 로그인 (Access + Refresh) |
| POST | `/api/auth/refresh` | Public | Access 재발급 (Refresh 회전) |
| POST | `/api/auth/logout` | USER | 모든 Refresh 폐기 |
| GET | `/api/challenges` | Public | 챌린지 목록 (`?difficulty=&tag=&page=&size=`) |
| GET | `/api/challenges/{slug}` | Public | 챌린지 상세 (hidden testbench 미노출) |
| POST | `/api/challenges` | ADMIN | 챌린지 등록 |
| PUT | `/api/challenges/{id}` | ADMIN | 챌린지 수정 |
| DELETE | `/api/challenges/{id}` | ADMIN | Soft delete |
| GET | `/api/challenges/{slug}/leaderboard` | Public | 챌린지별 상위 50 |
| POST | `/api/submissions` | USER | 코드 제출 (즉시 PENDING 응답, 폴링) |
| GET | `/api/submissions/{id}` | USER (자기) / ADMIN | 제출 상세 + 벡터별 결과 |
| GET | `/api/submissions/me` | USER | 본인 제출 이력 |
| POST | `/api/playground/simulate` | USER | 자유 시뮬레이션 |
| POST | `/api/playground/synthesize` | USER | 자유 합성 + 시각화 (Verilog, 코드 해시 캐시) |
| GET | `/api/paths` | Public | 학습 경로 목록 |
| GET | `/api/paths/{slug}` | Public/Auth optional | 학습 경로 상세 + 진행도 |
| GET | `/api/me/progress` | USER | 사용자 풀이/학습 경로 진행도 |
| GET | `/actuator/health` | Public | 헬스체크 |

## 일정

- **W0** (5/6): 계획서 + 부트스트랩 ✅
- **W1** (5/7~5/12): 도메인·인증·Challenge CRUD + 시뮬레이션 PoC ✅
- **W2** (5/13~5/19): HdlAdapter + 채점 파이프라인 + 콘텐츠 + Testcontainers ✅
- **W3** (5/20~5/26): Yosys 시각화 + Playground + 리더보드 + 프론트엔드 ✅
- **제출 전** (5/27~5/28): 문서 정리 + 최종 검증 + GitHub Public 전환

## 라이선스

학습용 프로젝트. 미정.
Updated at 2026-05-14 21:22:31
Updated at 2026-05-14 20:31:57
Updated at 2026-05-14 18:5:43
Updated at 2026-05-14 17:12:40
Updated at 2026-05-15 17:50:14
Updated at 2026-05-16 13:17:56
Updated at 2026-05-16 9:32:25
Updated at 2026-05-16 20:36:1
Updated at 2026-05-17 18:17:35
Updated at 2026-05-18 15:58:2
Updated at 2026-05-18 12:15:28
Updated at 2026-05-19 22:30:31
Updated at 2026-05-19 9:32:22
Updated at 2026-05-19 20:40:1
Updated at 2026-05-19 18:31:49
Updated at 2026-05-20 14:36:56
Updated at 2026-05-20 12:19:48
Updated at 2026-05-20 14:49:20
Updated at 2026-05-21 11:44:43
Updated at 2026-05-21 13:27:58
Updated at 2026-05-21 14:41:54
Updated at 2026-05-21 21:31:47
Updated at 2026-05-22 13:20:54
Updated at 2026-05-22 22:59:57
Updated at 2026-05-23 18:53:45
Updated at 2026-05-23 9:7:31
Updated at 2026-05-23 13:35:29
Updated at 2026-05-23 17:35:38
Updated at 2026-05-24 23:7:12
Updated at 2026-05-24 16:52:46
Updated at 2026-05-24 12:53:50
Updated at 2026-05-25 17:12:41
Updated at 2026-05-26 23:17:6
Updated at 2026-05-26 10:14:54
Updated at 2026-05-26 9:18:9
Updated at 2026-05-27 15:43:4
Updated at 2026-05-27 17:38:10
Updated at 2026-05-27 14:19:38
Updated at 2026-05-27 10:54:8
Updated at 2026-05-28 17:38:55
Updated at 2026-05-28 11:1:58
