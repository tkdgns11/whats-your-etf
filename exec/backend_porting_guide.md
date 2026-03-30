# Backend 포팅 가이드

## 1. 개발 환경

### 1.1 사용 기술 스택

| 구분 | 기술 | 버전 |
|------|------|------|
| **Language** | Java | 21 (Amazon Corretto) |
| **Framework** | Spring Boot | 3.2.2 |
| **Build Tool** | Gradle | 8.7 |
| **IDE** | IntelliJ IDEA | 2024.1+ 권장 |
| **Database** | PostgreSQL | 16 (Alpine) |
| **Cache** | Redis | 7 (Alpine) |
| **Message Queue** | RabbitMQ | 3 (Management Alpine) |
| **Web Server** | Nginx | 1.27 (Alpine) |
| **Container** | Docker | 24+ |

### 1.2 user-service (Spring Boot)

| 라이브러리 | 버전 | 용도 |
|------------|------|------|
| Spring Boot Starter Web | 3.2.2 | REST API |
| Spring Boot Starter WebFlux | 3.2.2 | WebClient (LLM API 호출) |
| Spring Boot Starter Data JPA | 3.2.2 | ORM |
| Spring Boot Starter Security | 3.2.2 | 인증/인가 |
| Spring Boot Starter Data Redis | 3.2.2 | 캐시 |
| Spring Boot Starter Mail | 3.2.2 | 이메일 발송 |
| Spring Boot Starter AMQP | 3.2.2 | RabbitMQ |
| QueryDSL | 5.0.0 | 동적 쿼리 |
| PostgreSQL Driver | 42.7.x | DB 연결 |
| java-jwt (Auth0) | 4.4.0 | JWT 토큰 |
| Firebase Admin SDK | 9.2.0 | FCM 푸시 알림 |
| SpringDoc OpenAPI | 2.3.0 | Swagger |
| Lombok | - | 보일러플레이트 코드 제거 |

### 1.3 data-service (FastAPI)

| 라이브러리 | 버전 | 용도 |
|------------|------|------|
| Python | 3.14 | 언어 |
| FastAPI | 0.115.0 | REST API |
| Uvicorn | 0.30.6 | ASGI 서버 |
| SQLAlchemy | 2.0.35 | ORM |
| psycopg | 3.2.10+ | PostgreSQL 드라이버 |
| httpx | 0.27.2 | HTTP 클라이언트 |
| BeautifulSoup4 | 4.12.3 | 웹 스크래핑 |
| APScheduler | 3.10.4 | 스케줄러 |
| aio-pika | 9.0.0+ | RabbitMQ |
| redis | 7.3.0 | Redis 클라이언트 |
| pykrx | - | KRX 주가 데이터 |
| yfinance | - | 해외 주가 데이터 |

---

## 2. 빌드 및 실행

### 2.1 로컬 인프라 실행 (Docker Compose)

```bash
# 프로젝트 루트에서 실행
docker-compose -f docker-compose-local.yml up -d
```

실행되는 컨테이너:
- **wye-postgres**: PostgreSQL (포트 5432)
- **wye-redis**: Redis (포트 6379)
- **wye-rabbitmq**: RabbitMQ (포트 5672, 관리 UI 15672)
- **wye-pgadmin**: pgAdmin (포트 5050)

### 2.2 user-service 빌드 및 실행

```bash
cd backend/user-service

# 빌드
./gradlew clean build -x test

# 실행
./gradlew bootRun

# 또는 JAR 직접 실행
java -jar build/libs/user-service-0.0.1-SNAPSHOT.jar
```

### 2.3 data-service 빌드 및 실행

```bash
cd backend/data-service

# 가상환경 생성 및 활성화
python -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate

# 의존성 설치
pip install -r requirements.txt

# 실행
uvicorn app.main:app --reload --port 8000
```

### 2.4 Docker 이미지 빌드

```bash
# user-service
cd backend/user-service
docker build -t wye-user-service .

# data-service
cd backend/data-service
docker build -t wye-data-service .
```

---

## 3. 환경 변수

### 3.1 운영 환경 변수 (.env)

```bash
# ===== Docker Hub images =====
USER_SERVICE_IMAGE=docker.io/taekminkwon/wye-user-service:latest
DATA_SERVICE_IMAGE=docker.io/taekminkwon/wye-data-service:latest

# ===== Postgres =====
POSTGRES_DB=whatsyouretf
POSTGRES_USER=wye
POSTGRES_PASSWORD={DB_PASSWORD}

# ===== RabbitMQ =====
RABBITMQ_DEFAULT_USER=admin
RABBITMQ_DEFAULT_PASS={RABBITMQ_PASSWORD}

# ===== Redis =====
REDIS_HOST=localhost
REDIS_PORT=6379

# ===== JPA =====
JPA_DDL_AUTO=update
SHOW_SQL=false

# ===== Server =====
SERVER_PORT=8080

# ===== JWT =====
JWT_SECRET={JWT_SECRET_KEY}

# ===== Kakao OAuth2 =====
KAKAO_CLIENT_ID={KAKAO_REST_API_KEY}
KAKAO_CLIENT_SECRET={KAKAO_CLIENT_SECRET}

# ===== GMS (SSAFY LLM API) =====
GMS_API_URL=https://gms.ssafy.io/gmsapi/api.anthropic.com
GMS_API_KEY={GMS_API_KEY}

# ===== Mail (Gmail SMTP) =====
MAIL_USERNAME={GMAIL_ADDRESS}
MAIL_PASSWORD={GMAIL_APP_PASSWORD}

# ===== KRX 데이터 =====
KRX_ID={KRX_ID}
KRX_PW={KRX_PASSWORD}
DATA_PORTAL_COMPANY_SERVICE_KEY={DATA_PORTAL_KEY}

# ===== File Storage =====
FILE_UPLOAD_DIR=/app/uploads
FILE_BASE_URL=https://j14d102.p.ssafy.io

# ===== External AI APIs =====
ANTHROPIC_API_KEY={ANTHROPIC_API_KEY}
OPENAI_API_KEY={OPENAI_API_KEY}

# ===== 한국투자증권 API =====
KIS_APP_KEY={KIS_APP_KEY}
KIS_APP_SECRET={KIS_APP_SECRET}
```

### 3.2 프로퍼티 파일 목록

| 파일 | 경로 | 설명 | Git |
|------|------|------|-----|
| `application.yml` | `user-service/src/main/resources/` | 메인 설정 | O |
| `.env` | `/home/ubuntu/wye/` (운영) | 환경 변수 | X |
| `firebase-service-account.json` | `/home/ubuntu/config/` (운영) | Firebase 인증 | X |
| `docker-compose.yml` | `/home/ubuntu/wye/` (운영) | 운영 Docker 설정 | X |

---

## 4. 데이터베이스

### 4.1 접속 정보

| 환경 | 호스트 | 포트 | DB명 | 사용자 | 비밀번호 |
|------|--------|------|------|--------|----------|
| 로컬 | localhost | 5432 | whatsyouretf | wye | wye1234 |
| 운영 | postgres (Docker 내부) | 5432 | whatsyouretf | wye | {DB_PASSWORD} |

### 4.2 JDBC 연결 문자열

```
# 로컬
jdbc:postgresql://localhost:5432/whatsyouretf?options=-c%20timezone=Asia/Seoul

# 운영
jdbc:postgresql://postgres:5432/whatsyouretf?options=-c%20timezone=Asia/Seoul
```

### 4.3 DB 설정 프로퍼티 파일

| 파일 | 경로 | 설명 |
|------|------|------|
| `application.yml` | `backend/user-service/src/main/resources/` | DB 연결 설정 (환경변수 참조) |
| `.env` | `backend/user-service/` (로컬) | 로컬 환경변수 |
| `.env` | `/home/ubuntu/wye/` (운영) | 운영 환경변수 |
| `docker-compose-local.yml` | 프로젝트 루트 | 로컬 PostgreSQL 컨테이너 설정 |

### 4.4 application.yml DB 설정

```yaml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:whatsyouretf}?options=-c%20timezone=Asia/Seoul
    username: ${DB_USERNAME:wye}
    password: ${DB_PASSWORD:wye1234}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000

  jpa:
    hibernate:
      ddl-auto: ${JPA_DDL_AUTO:update}
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        jdbc:
          time_zone: Asia/Seoul
    show-sql: ${SHOW_SQL:true}
```

### 4.5 DB 관련 환경변수

| 변수명 | 로컬 기본값 | 운영 값 | 설명 |
|--------|-------------|---------|------|
| `DB_HOST` | localhost | postgres | DB 호스트 |
| `DB_PORT` | 5432 | 5432 | DB 포트 |
| `DB_NAME` | whatsyouretf | whatsyouretf | 데이터베이스명 |
| `DB_USERNAME` | wye | wye | DB 사용자 |
| `DB_PASSWORD` | wye1234 | {DB_PASSWORD} | DB 비밀번호 |
| `JPA_DDL_AUTO` | update | update | 스키마 자동 생성 모드 |
| `SHOW_SQL` | true | false | SQL 로그 출력 |

### 4.6 pgAdmin 접속 (로컬)

- URL: http://localhost:5050
- Email: admin@wye.com
- Password: admin1234

### 4.7 Redis 접속 정보

| 환경 | 호스트 | 포트 | 비밀번호 |
|------|--------|------|----------|
| 로컬 | localhost | 6379 | (없음) |
| 운영 | redis | 6379 | (없음) |

### 4.8 RabbitMQ 접속 정보

| 환경 | 호스트 | 포트 | 사용자 | 비밀번호 |
|------|--------|------|--------|----------|
| 로컬 | localhost | 5672 | guest | guest |
| 운영 | rabbitmq | 5672 | admin | {RABBITMQ_PASSWORD} |

관리 UI: http://localhost:15672 (로컬)

---

## 5. 외부 서비스

### 5.1 카카오 로그인

| 항목 | 값 |
|------|-----|
| 서비스 | Kakao Developers |
| 용도 | 소셜 로그인 |
| 필요 키 | REST API 키, Client Secret |
| 환경 변수 | `KAKAO_CLIENT_ID`, `KAKAO_CLIENT_SECRET` |
| 콘솔 URL | https://developers.kakao.com |

### 5.2 Firebase (FCM)

| 항목 | 값 |
|------|-----|
| 서비스 | Firebase Cloud Messaging |
| 용도 | 푸시 알림 |
| 필요 파일 | `firebase-service-account.json` |
| 운영 경로 | `/home/ubuntu/config/` |
| 콘솔 URL | https://console.firebase.google.com |

### 5.3 Gmail SMTP

| 항목 | 값 |
|------|-----|
| 서비스 | Gmail SMTP |
| 용도 | 이메일 인증, 비밀번호 재설정 |
| 호스트 | smtp.gmail.com |
| 포트 | 587 (TLS) |
| 환경 변수 | `MAIL_USERNAME`, `MAIL_PASSWORD` |

### 5.4 SSAFY GMS (LLM API)

| 항목 | 값 |
|------|-----|
| 서비스 | SSAFY GMS (Anthropic Proxy) |
| 용도 | AI 포트폴리오 분석 |
| Base URL | https://gms.ssafy.io/gmsapi/api.anthropic.com |
| 모델 | claude-sonnet-4-20250514 |
| 환경 변수 | `GMS_API_KEY` |

### 5.5 KRX 데이터

| 항목 | 값 |
|------|-----|
| 서비스 | KRX 정보데이터시스템 |
| 용도 | ETF/주가 데이터 수집 |
| 환경 변수 | `KRX_ID`, `KRX_PW` |

### 5.6 한국투자증권 API

| 항목 | 값 |
|------|-----|
| 서비스 | 한국투자증권 OpenAPI |
| 용도 | 실시간 시세, ETF 정보 |
| 환경 변수 | `KIS_APP_KEY`, `KIS_APP_SECRET` |

---

## 6. 운영 배포 (Docker Compose)

### 6.1 운영 서버 구성

```
wye-nginx (80, 443)
    ├── wye-user-service (8080)
    ├── wye-data-service (80)
    ├── wye-postgres (5432)
    ├── wye-redis (6379)
    └── wye-rabbitmq (5672, 15672)
```

### 6.2 배포 명령

```bash
cd /home/ubuntu/wye

# 최신 이미지 Pull
docker-compose pull

# 서비스 재시작
docker-compose up -d

# 로그 확인
docker-compose logs -f user-service
```

### 6.3 Docker Hub 이미지

| 서비스 | 이미지 |
|--------|--------|
| user-service | `docker.io/taekminkwon/wye-user-service:latest` |
| data-service | `docker.io/taekminkwon/wye-data-service:latest` |

### 6.4 볼륨

| 볼륨 | 용도 |
|------|------|
| `postgres_data` | PostgreSQL 데이터 |
| `redis_data` | Redis 영속 데이터 |
| `rabbitmq_data` | RabbitMQ 데이터 |
| `uploads_data` | 업로드 파일 |

---

## 7. 배포 특이사항

### 7.1 타임존 설정

- 모든 서비스는 **KST (Asia/Seoul)** 사용
- Docker 환경변수: `TZ=Asia/Seoul`
- PostgreSQL: `PGTZ=Asia/Seoul`
- DB 연결: `?options=-c%20timezone=Asia/Seoul`

### 7.2 JVM 옵션 (user-service)

```
-Xms128m -Xmx512m -XX:MaxMetaspaceSize=192m -Duser.timezone=Asia/Seoul
```

### 7.3 Nginx + SSL

- Let's Encrypt 인증서 사용
- Certbot 자동 갱신
- 설정 파일: `/home/ubuntu/wye/nginx/nginx.conf`

### 7.4 Swagger UI

- URL: https://j14d102.p.ssafy.io/swagger-ui.html
- API Docs: https://j14d102.p.ssafy.io/api-docs

### 7.5 헬스체크

- PostgreSQL: `pg_isready` (5초 간격)
- 서비스 의존성: postgres healthy → redis/rabbitmq started → 서비스 시작
