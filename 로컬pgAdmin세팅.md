# 로컬 개발 환경 세팅 가이드

> 팀원 전원이 로컬에서 독립적으로 개발/테스트할 수 있는 환경을 구성합니다.

---

## 사전 준비

| 도구 | 설치 확인 | 비고 |
|---|---|---|
| Docker Desktop | `docker --version` | [설치 링크](https://www.docker.com/products/docker-desktop/) |
| Git | `git --version` | |
| JDK 21 | `java --version` | [Adoptium](https://adoptium.net/) 추천 |
| IntelliJ IDEA | | Ultimate 또는 Community |
| Android Studio | | Jetpack Compose 개발용 |

---

## 1단계: Docker Compose로 DB + pgAdmin 띄우기

### 1-1. 파일 생성

프로젝트 루트에 `docker-compose-local.yml` 생성:

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
      - ./db/init.sql:/docker-entrypoint-initdb.d/init.sql  # 초기 스키마
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

### 1-2. 실행

```bash
# 프로젝트 루트에서
docker compose -f docker-compose-local.yml up -d
```

### 1-3. 실행 확인

```bash
docker ps
```

3개 컨테이너가 뜨면 성공:
```
wye-postgres   postgres:16       0.0.0.0:5432->5432
wye-redis      redis:7-alpine    0.0.0.0:6379->6379
wye-pgadmin    dpage/pgadmin4    0.0.0.0:5050->80
```

---

## 2단계: pgAdmin 접속 및 DB 연결

### 2-1. pgAdmin 접속

브라우저에서 `http://localhost:5050` 접속

- Email: `admin@wye.com`
- Password: `admin1234`

### 2-2. 서버 등록 (최초 1회)

1. 좌측 패널 → **Servers** 우클릭 → **Register** → **Server...**
2. **General** 탭:
   - Name: `로컬 DB` (아무 이름)
3. **Connection** 탭:
   - Host name: `postgres` (Docker 네트워크 내부 이름)
   - Port: `5432`
   - Username: `wye`
   - Password: `wye1234`
   - Save password 체크
4. **Save** 클릭

> **주의**: Host name에 `localhost`가 아닌 `postgres`를 입력해야 합니다.
> pgAdmin도 Docker 안에서 동작하므로, 같은 Docker 네트워크의 컨테이너명으로 접근합니다.

### 2-3. 주요 기능

| 기능 | 위치 |
|---|---|
| SQL 실행 | 서버 → DB 선택 → 우클릭 → **Query Tool** |
| 테이블 조회 | Schemas → Tables → 테이블 우클릭 → **View/Edit Data** |
| 인덱스 확인 | 테이블 → Indexes 펼치기 |
| 실행계획 | Query Tool에서 SQL 작성 → **Explain Analyze** (F7) |

---

## 3단계: Spring Boot 연결

### 3-1. application-local.yml

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/whatsyouretf
    username: wye
    password: wye1234
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
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

### 3-2. 프로필 지정하여 실행

```bash
# CLI
./gradlew bootRun --args='--spring.profiles.active=local'

# IntelliJ
# Run Configuration → VM options에 추가:
# -Dspring.profiles.active=local
```

---

## 4단계: 자주 쓰는 명령어

### Docker

```bash
# 전체 시작
docker compose -f docker-compose-local.yml up -d

# 전체 중지
docker compose -f docker-compose-local.yml down

# DB 데이터까지 완전 삭제 (초기화)
docker compose -f docker-compose-local.yml down -v

# 로그 확인
docker logs wye-postgres
docker logs wye-redis
```

### DB 초기화 (스키마 재생성)

```bash
# 방법 1: 볼륨 삭제 후 재시작 (깨끗하게)
docker compose -f docker-compose-local.yml down -v
docker compose -f docker-compose-local.yml up -d

# 방법 2: pgAdmin Query Tool에서 직접 DROP + CREATE
```

### Redis 확인

```bash
# Redis CLI 접속
docker exec -it wye-redis redis-cli

# 키 목록
keys *

# 특정 키 조회
get etf:price:069500
```

---

## 5단계: EC2 pgAdmin 접속 (공유 DB)

EC2에도 동일하게 pgAdmin이 Docker로 띄워져 있습니다.

```
브라우저 → http://EC2_IP:5050
Email: admin@wye.com
Password: (팀 공유)
```

EC2 DB 서버 등록 시 Connection:
- Host name: `postgres`
- Port: `5432`
- Username / Password: (팀 공유)

---

## 환경 구분 정리

| 환경 | DB | pgAdmin | Spring Boot | 용도 |
|---|---|---|---|---|
| **로컬** | localhost:5432 | localhost:5050 | IDE에서 실행 | 개발 + 테스트 |
| **EC2** | EC2 Docker | EC2_IP:5050 | Docker | 배포 + 통합 테스트 |

- 로컬에서 마음대로 테이블 날리고 테스트
- 완성되면 git push → EC2에 배포
- EC2 DB는 함부로 DROP하지 말 것 (팀 공유)

---

## 트러블슈팅

### 포트 충돌 (5432 already in use)

로컬에 PostgreSQL이 이미 설치되어 있는 경우:
```bash
# 기존 PostgreSQL 서비스 중지
# Windows: 서비스 → postgresql-x64-16 → 중지
# 또는 docker-compose에서 포트 변경: "5433:5432"
```

### pgAdmin에서 postgres 연결 안 됨

- Host name이 `localhost`가 아닌 `postgres`인지 확인
- `docker ps`로 postgres 컨테이너가 running인지 확인
- `docker logs wye-postgres`로 에러 확인

### Docker Desktop이 안 뜸 (Windows)

- BIOS에서 가상화(VT-x) 활성화 확인
- WSL2 설치 확인: `wsl --install`
- Docker Desktop → Settings → General → Use the WSL 2 based engine 체크
