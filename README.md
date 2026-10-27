# Runshot

Java 17 · Spring Boot 4.1.1 · Spring Security/JWT · Spring Data JPA · MySQL로 만드는 개인 러닝 저널입니다. `src/main/resources/static`의 화면을 Spring Boot가 같은 출처에서 제공합니다.

## 구현한 사용자 흐름

- 회원가입 → BCrypt 암호화 → 로그인/JWT 발급 → 본인 기록 조회
- 거리·시간·날짜·메모 추가/수정/삭제, 기간/메모 검색, 총 거리·횟수·평균 페이스
- 새로고침 시 JWT로 `/api/auth/me`를 확인하고 세션 복원. 로그아웃 시 이 탭의 토큰 제거
- JWT 만료 시 다시 로그인. 로그아웃은 이미 발급한 JWT를 서버에서 폐기하지 않으며 기본 만료 시간은 15분
- 모든 러닝 기록 API는 인증 사용자만의 데이터를 처리. 클라이언트의 `userId`로 소유자를 정하지 않음

사진 업로드는 이번 변경에 포함하지 않았습니다. 현재 요청 범위인 로그인과 기록 CRUD 화면을 연결했습니다.

## 실행

새 MySQL 데이터베이스에 `deploy/schema.sql`을 적용하고 `.env.example`의 환경 변수 이름을 참고해 실행 환경에 값을 설정합니다. Spring Boot는 `.env`를 자동으로 읽지 않으므로 IntelliJ 환경 변수 또는 운영 런타임에 등록해야 합니다.

필수 값: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`(32바이트 이상 키를 Base64로 인코딩).

```bash
./gradlew test bootJar
java -jar build/libs/runshot-0.0.1-SNAPSHOT.jar
```

Windows에서는 `gradlew.bat test bootJar`를 사용합니다. `http://localhost:8080`에서 화면에 접속합니다. 포트를 바꾸려면 `PORT`를 설정합니다.

기존에 로컬 `application.properties`가 있다면 같은 이름의 설정이 YAML보다 우선할 수 있습니다. 기존 비밀 값은 커밋하지 말고 실행 환경 설정을 확인하세요.

## 새 서버에서 Docker Compose로 실행

1. `.env.example`을 `.env`로 복사하고 모든 비밀번호·JWT 키·게이트웨이 키를 직접 설정합니다.
2. `docker compose up --build -d`로 MySQL과 백엔드를 실행합니다.
3. 외부 HTTPS 리버스 프록시가 로컬 `127.0.0.1:8080`으로 연결되도록 구성합니다. MySQL은 외부 포트를 열지 않습니다.

운영 프로필 `prod`에서는 32자 이상의 `RUNSHOT_GATEWAY_KEY`가 필수입니다. Sites의 서버 측 API 중계가 같은 키를 `X-Runshot-Gateway-Key` 헤더에 넣습니다. 키 없이 백엔드 API에 직접 요청하면 403을 반환합니다. 브라우저 코드에 이 키나 DB 비밀번호를 넣지 마세요.

Compose의 DB 초기화 SQL은 새 볼륨에서만 실행됩니다. 기존 데이터가 있는 서버에는 새 스키마 파일을 덮어쓰지 마세요.

## 기존 데이터베이스

먼저 백업하고 `deploy/existing-db-review.sql`의 읽기 전용 쿼리로 실제 테이블을 확인하세요. 기존 `RunningRecord.create()`에는 사용자 연결 누락이 있어 `user_id IS NULL`인 행이 생겼을 수 있습니다. 실제 소유자를 확인한 뒤 복구해야 하며, 이 변경은 과거 행을 임의로 수정하거나 삭제하지 않습니다.

`content` 컬럼은 500자, `user_id`는 NOT NULL이 필요합니다. `ddl-auto=validate`로 운영 DB를 자동 변경하지 않습니다. 현재 스키마를 확인하고 필요한 ALTER만 수동으로 적용하세요.

## Sites 비공개 배포

Sites에는 Java 서버를 직접 올릴 수 없습니다. Sites의 정적 화면/API 중계 Worker와 별도로 HTTPS Spring Boot 백엔드 및 MySQL이 필요합니다. Sites 소스에는 실제 화면 자산과 API 중계가 준비되어 있습니다.

운영 백엔드가 준비되면 Sites 환경 변수에 다음을 설정하고 저장된 버전을 **owner-private**으로 배포합니다.

- `RUNSHOT_API_URL`: 백엔드 HTTPS origin. 경로·쿼리·인증 정보 없이 설정
- `RUNSHOT_GATEWAY_KEY`: 백엔드와 동일한 비밀 값

백엔드가 미설정된 Worker의 API는 503을 반환합니다. 이 상태를 정상 작동하는 서비스로 간주하지 않습니다. 사이트 공개 전환은 요청 범위에 포함하지 않습니다.

## API

| 경로 | 동작 | 인증 |
| --- | --- | --- |
| `POST /api/users` | 회원가입 (201, 중복 409, 입력 오류 400) | JWT 불필요 |
| `POST /api/auth/login` | 로그인/JWT 발급, 실패 401 | JWT 불필요 |
| `GET /api/auth/me` | 현재 사용자 (비밀번호 미포함) | 필요 |
| `GET /api/runs` | 본인 기록 목록, 최신 날짜순 | 필요 |
| `POST /api/runs` | 본인 기록 생성 (201) | 필요 |
| `GET /api/runs/{id}` | 본인 기록 상세 | 필요 |
| `PUT /api/runs/{id}` | 본인 기록 전체 수정 | 필요 |
| `DELETE /api/runs/{id}` | 본인 기록 삭제 (204) | 필요 |

운영 프로필의 API는 위 JWT 규칙과 별도로 게이트웨이 키도 필요합니다. 타인 소유/없는 기록은 404, 타인 `userId` 지정은 403입니다. 엔티티 대신 DTO를 반환하므로 비밀번호나 JPA 프록시가 응답에 포함되지 않습니다.

```json
{"distance":6.5,"runningTime":2400,"runningDate":"2026-10-26","content":"오늘의 러닝"}
```

## 검증

`./gradlew test`는 H2(MySQL mode)에서 실제 HTTP 서버를 띄우고 회원가입·로그인·소유자별 CRUD를 검증합니다. H2 통과만으로 실제 MySQL 호환성을 보장하지는 않습니다.

전용 테스트 DB를 사용하는 실행 서버에는 아래 스크립트로 같은 흐름을 확인할 수 있습니다. 테스트 계정을 생성하므로 운영 데이터에 실행하지 마세요. 테스트 중 생성한 러닝 기록은 마지막에 삭제합니다.

```bash
RUNSHOT_TEST_API_URL=http://127.0.0.1:8080 python scripts/verify-api.py
```

운영 프로필을 테스트할 때는 `RUNSHOT_GATEWAY_KEY`도 환경 변수로 전달합니다. 실제 실행 결과와 미검증 항목은 `docs/verification.md`에 기록합니다.
