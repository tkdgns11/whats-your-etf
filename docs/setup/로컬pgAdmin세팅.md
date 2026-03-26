# 로컬 개발 환경 세팅 가이드

> 팀원 전원이 로컬에서 독립적으로 개발/테스트할 수 있는 환경을 구성합니다.

---

## 빠른 시작 (요약)

```bash
# 1. Docker Desktop 설치 후
# 2. 프로젝트 루트에서 실행
docker compose -f docker-compose-local.yml up -d

# 3. 브라우저에서 pgAdmin 접속
http://localhost:5050
# Email: admin@wye.com / Password: admin1234

# 4. 서버 등록 (Host: postgres, User: wye, PW: wye1234)
# 5. 끝!
```

---

## 0단계: Docker Desktop 설치 (Windows)

### 0-1. 시스템 요구사항 확인

- Windows 10 64-bit: Pro, Enterprise, Education (Build 19041+)
- Windows 11 64-bit
- BIOS에서 가상화(VT-x) 활성화 필요

### 0-2. Docker Desktop 설치

1. [Docker Desktop 다운로드](https://www.docker.com/products/docker-desktop/)
2. 설치 파일 실행 → 기본 옵션으로 설치
3. 설치 완료 후 **재부팅**
4. Docker Desktop 실행
5. 우측 하단 트레이에서 Docker 아이콘이 **초록색**이면 정상

### 0-4. 설치 확인

```bash
# 터미널(CMD, PowerShell, Git Bash)에서
docker --version
# Docker version 24.x.x 출력되면 성공

docker compose version
# Docker Compose version v2.x.x 출력되면 성공
```

---

## 사전 준비 체크리스트

| 도구 | 설치 확인 명령어 | 설치 링크 |
|---|---|---|
| Docker Desktop | `docker --version` | [설치](https://www.docker.com/products/docker-desktop/) |
| Git | `git --version` | [설치](https://git-scm.com/downloads) |
| JDK 21 | `java --version` | [Adoptium](https://adoptium.net/) |
| IntelliJ IDEA | - | Ultimate 또는 Community |
| Android Studio | - | Jetpack Compose 개발용 |

---

## 1단계: 프로젝트 클론

```bash
# 원하는 폴더에서
git clone https://lab.ssafy.com/s14-fintech-finance-sub1/S14P21D102.git

cd S14P21D102
```

---

## 2단계: Docker Compose로 DB + pgAdmin 띄우기

### 2-1. docker-compose-local.yml 확인

프로젝트 루트에 이미 `docker-compose-local.yml` 파일이 있습니다.

```yaml
version: "3.8"

services:
  postgres:
    image: postgres:16
    container_name: wye-postgres
    ports:
      - "5432:5432"
    environment:
      POSTGRES_DB: whatsyouretf
      POSTGRES_USER: wye
      POSTGRES_PASSWORD: wye1234
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./db/init.sql:/docker-entrypoint-initdb.d/init.sql
    restart: unless-stopped

  redis:
    image: redis:7-alpine
    container_name: wye-redis
    ports:
      - "6379:6379"
    restart: unless-stopped

  pgadmin:
    image: dpage/pgadmin4
    container_name: wye-pgadmin
    ports:
      - "5050:80"
    environment:
      PGADMIN_DEFAULT_EMAIL: admin@wye.com
      PGADMIN_DEFAULT_PASSWORD: admin1234
    depends_on:
      - postgres
    restart: unless-stopped

volumes:
  postgres_data:
```

### 2-2. 실행

```bash
# 프로젝트 루트(S14P21D102)에서
docker compose -f docker-compose-local.yml up -d
```

**첫 실행 시** 이미지 다운로드로 3~5분 소요될 수 있습니다.

### 2-3. 실행 확인

```bash
docker ps
```

3개 컨테이너가 모두 `Up` 상태면 성공:

```
CONTAINER ID   IMAGE              STATUS    PORTS                    NAMES
xxx            postgres:16        Up        0.0.0.0:5432->5432/tcp   wye-postgres
xxx            redis:7-alpine     Up        0.0.0.0:6379->6379/tcp   wye-redis
xxx            dpage/pgadmin4     Up        0.0.0.0:5050->80/tcp     wye-pgadmin
```

> **에러 발생 시**: `docker logs wye-postgres` 로 로그 확인

---

## 3단계: pgAdmin 접속 및 DB 연결

### 3-1. pgAdmin 접속

브라우저에서 열기: **http://localhost:5050**

로그인 정보:
- **Email**: `admin@wye.com`
- **Password**: `admin1234`

### 3-2. 서버 등록 (최초 1회만)

1. 좌측 패널 → **Servers** 우클릭 → **Register** → **Server...**

2. **General** 탭:
   - Name: `로컬 DB` (아무 이름)

3. **Connection** 탭:
   | 항목 | 값 |
   |---|---|
   | Host name/address | `postgres` |
   | Port | `5432` |
   | Maintenance database | `whatsyouretf` |
   | Username | `wye` |
   | Password | `wye1234` |
   | Save password | ✅ 체크 |

4. **Save** 클릭

> ⚠️ **중요**: Host name에 `localhost`가 아닌 **`postgres`** 입력!
> pgAdmin도 Docker 안에서 동작하므로, Docker 네트워크 내부 이름 사용

### 3-3. 연결 확인

1. 좌측 패널에서: Servers → 로컬 DB → Databases → **whatsyouretf** 클릭
2. Schemas → public → **Tables** 펼치기
3. 테이블 목록이 보이면 성공! (예: `user`, `etf`, `portfolios` 등)

---

## 4단계: 초기 데이터 시딩 (선택)

### 4-1. 산업분류코드 데이터 넣기

1. pgAdmin에서 **whatsyouretf** DB 선택
2. 상단 메뉴 → **Tools** → **Query Tool** 클릭
3. `docs/sql/industry_classification_seed.sql` 파일 내용 복사 → 붙여넣기
4. **▶ Execute (F5)** 클릭

### 4-2. 데이터 확인

Query Tool에서:

```sql
SELECT * FROM industry_classification LIMIT 10;
```

---

## 5단계: Spring Boot 연결

### 5-1. application-local.yml

`backend/*/src/main/resources/application-local.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/whatsyouretf
    username: wye
    password: wye1234
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
    show-sql: true

  data:
    redis:
      host: localhost
      port: 6379
```

### 5-2. 프로필 지정하여 실행

**IntelliJ:**
1. Run Configuration 열기
2. VM options에 추가: `-Dspring.profiles.active=local`
3. Run

**CLI:**
```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

---

## 6단계: 자주 쓰는 명령어

### Docker 컨테이너 관리

```bash
# 시작
docker compose -f docker-compose-local.yml up -d

# 중지
docker compose -f docker-compose-local.yml down

# 상태 확인
docker ps

# 로그 보기
docker logs wye-postgres
docker logs wye-redis
docker logs wye-pgadmin

# 로그 실시간 보기
docker logs -f wye-postgres
```

### DB 완전 초기화 (스키마 + 데이터 전부 삭제)

```bash
# 볼륨까지 삭제 후 재시작
docker compose -f docker-compose-local.yml down -v
docker compose -f docker-compose-local.yml up -d
```

> init.sql이 다시 실행되어 스키마가 재생성됩니다.

### Redis CLI

```bash
# Redis 접속
docker exec -it wye-redis redis-cli

# 키 목록 보기
keys *

# 종료
exit
```

### PostgreSQL CLI (psql)

```bash
# PostgreSQL 접속
docker exec -it wye-postgres psql -U wye -d whatsyouretf

# 테이블 목록
\dt

# 테이블 구조 보기
\d user

# SQL 실행
SELECT COUNT(*) FROM etf;

# 종료
\q
```

---

## pgAdmin 주요 기능

| 기능 | 방법 |
|---|---|
| SQL 실행 | DB 선택 → 우클릭 → **Query Tool** (또는 상단 Tools 메뉴) |
| 테이블 데이터 조회 | Tables → 테이블 우클릭 → **View/Edit Data** → All Rows |
| 테이블 구조 보기 | 테이블 클릭 → 우측 **Properties** 탭 |
| 인덱스 확인 | 테이블 → **Indexes** 펼치기 |
| 실행계획 분석 | Query Tool → SQL 작성 → **Explain Analyze (F7)** |
| ERD 보기 | DB 우클릭 → **ERD For Database** |

---

## 환경 구분 정리

| 환경 | PostgreSQL | pgAdmin | Redis | Spring Boot |
|---|---|---|---|---|
| **로컬** | localhost:5432 | localhost:5050 | localhost:6379 | IDE에서 실행 |
| **EC2** | EC2 Docker | EC2_IP:5050 | EC2 Docker | Docker |

- **로컬**: 마음대로 테스트, 테이블 DROP 가능
- **EC2**: 팀 공유 환경, 함부로 DROP 금지!

---

## 트러블슈팅

### 1. 포트 충돌 (5432 already in use)

로컬에 PostgreSQL이 이미 설치된 경우:

**Windows:**
1. `Win + R` → `services.msc`
2. `postgresql-x64-16` 찾아서 **중지**

또는 docker-compose-local.yml에서 포트 변경:
```yaml
ports:
  - "5433:5432"  # 호스트 포트를 5433으로 변경
```

### 2. pgAdmin에서 postgres 연결 실패

- Host name이 `localhost`가 아닌 **`postgres`**인지 확인
- `docker ps`로 컨테이너가 실행 중인지 확인
- `docker logs wye-postgres`로 에러 확인

### 3. Docker Desktop 실행 안 됨 (Windows)

BIOS에서 **가상화(VT-x, AMD-V)** 활성화 필요:
- 재부팅 → BIOS 진입 (F2, Del 등)
- Virtualization Technology → **Enabled**
- 저장 후 재부팅 → Docker Desktop 다시 실행

### 4. init.sql 실행 안 됨

init.sql은 **최초 볼륨 생성 시에만** 실행됩니다.

스키마 변경 후 적용하려면:
```bash
# 볼륨 삭제 후 재시작
docker compose -f docker-compose-local.yml down -v
docker compose -f docker-compose-local.yml up -d
```

### 5. 컨테이너가 계속 재시작됨

```bash
# 로그 확인
docker logs wye-postgres

# 흔한 원인: init.sql 문법 오류
# → init.sql 수정 후 볼륨 삭제하고 재시작
```

---

## 유용한 SQL 쿼리

### 테이블 목록 확인
```sql
SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'public';
```

### 테이블 row 수 확인
```sql
SELECT
    schemaname,
    relname as table_name,
    n_live_tup as row_count
FROM pg_stat_user_tables
ORDER BY n_live_tup DESC;
```

### 외래키 관계 확인
```sql
SELECT
    tc.table_name,
    kcu.column_name,
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name
FROM information_schema.table_constraints AS tc
JOIN information_schema.key_column_usage AS kcu
    ON tc.constraint_name = kcu.constraint_name
JOIN information_schema.constraint_column_usage AS ccu
    ON ccu.constraint_name = tc.constraint_name
WHERE constraint_type = 'FOREIGN KEY';
```

---

## 다음 단계

1. ✅ Docker 환경 구성 완료
2. ✅ pgAdmin으로 DB 접속 확인
3. ⬜ Spring Boot 연결 테스트
4. ⬜ 초기 데이터 시딩
5. ⬜ API 개발 시작!
