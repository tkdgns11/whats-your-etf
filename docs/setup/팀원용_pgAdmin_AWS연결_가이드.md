# 팀원용 pgAdmin + AWS PostgreSQL 연결 가이드

> 로컬에 pgAdmin만 Docker로 설치하고, AWS EC2에 있는 공용 PostgreSQL에 연결하는 방법입니다.

---

## 1. 사전 준비

### 1-1. Docker Desktop 설치

1. [Docker Desktop 다운로드](https://www.docker.com/products/docker-desktop/) 페이지 접속
2. **Download for Windows** 클릭하여 설치 파일 다운로드
3. 설치 파일 실행 → 설치 진행 (기본 옵션 그대로)
4. 설치 완료 후 **재부팅** 필요할 수 있음
5. 재부팅 후 Docker Desktop 실행

### 1-2. Docker 설치 확인

터미널(PowerShell 또는 Git Bash)에서:

```bash
docker --version
```

아래처럼 버전이 나오면 성공:
```
Docker version 29.x.x, build xxxxx
```

---

## 2. pgAdmin만 Docker로 실행

### 2-1. 프로젝트 폴더로 이동

```bash
cd C:\SSAFY\project2team
```

### 2-2. pgAdmin 전용 Docker Compose 파일 생성

프로젝트 루트에 `docker-compose-pgadmin.yml` 파일이 없으면 생성:

```yaml
version: "3.8"

services:
  pgadmin:
    image: dpage/pgadmin4
    container_name: wye-pgadmin
    ports:
      - "5050:80"
    environment:
      PGADMIN_DEFAULT_EMAIL: admin@wye.com
      PGADMIN_DEFAULT_PASSWORD: admin1234
    restart: unless-stopped
```

### 2-3. pgAdmin 실행

```bash
docker compose -f docker-compose-pgadmin.yml up -d
```

처음 실행 시 이미지 다운로드로 1~2분 소요.

### 2-4. 실행 확인

```bash
docker ps
```

아래처럼 `wye-pgadmin`이 보이면 성공:
```
CONTAINER ID   IMAGE            ...   PORTS                  NAMES
xxxxxxxxxxxx   dpage/pgadmin4   ...   0.0.0.0:5050->80/tcp   wye-pgadmin
```

---

## 3. pgAdmin 접속

### 3-1. 브라우저에서 접속

```
http://localhost:5050
```

### 3-2. 로그인

| 항목 | 값 |
|------|-----|
| Email | `admin@wye.com` |
| Password | `admin1234` |

로그인하면 pgAdmin 대시보드가 나옴.

---

## 4. AWS PostgreSQL 서버 등록

### 4-1. 서버 등록 시작

두 가지 방법 중 하나:
- **방법 A**: 화면 중앙 **Quick Links** → **Add New Server** 클릭
- **방법 B**: 좌측 **Servers** 우클릭 → **Register** → **Server...**

### 4-2. General 탭

| 항목 | 값 |
|------|-----|
| Name | `AWS DB` (원하는 이름) |

### 4-3. Connection 탭 (중요!)

| 항목 | 값 | 설명 |
|------|-----|------|
| Host name/address | `[EC2 퍼블릭 IP]` | 팀장에게 확인 |
| Port | `5432` | PostgreSQL 기본 포트 |
| Maintenance database | `whatsyouretf` | 우리 프로젝트 DB명 |
| Username | `wye` | DB 사용자 |
| Password | `[팀 공유 비밀번호]` | 팀장에게 확인 |
| Save password | ✅ 체크 | 매번 입력 안 해도 됨 |

### 4-4. 저장

**Save** 버튼 클릭.

연결 성공하면 좌측에 **AWS DB** 서버가 추가됨.

---

## 5. 연결 확인 및 쿼리 실행

### 5-1. 데이터베이스 탐색

좌측 트리에서:
```
AWS DB
  └── Databases
        └── whatsyouretf
              └── Schemas
                    └── public
                          └── Tables
```

**Tables** 펼치면 우리 프로젝트 테이블들이 보임:
- `users`
- `etf_list`
- `etf_prices`
- `news_articles`
- ... (총 15개)

### 5-2. Query Tool 열기

1. **whatsyouretf** 데이터베이스 우클릭
2. **Query Tool** 클릭

### 5-3. 쿼리 실행

Query Tool 창에서 SQL 입력 후:
- **F5** 키 또는
- 상단 **▶ (Execute)** 버튼 클릭

**테스트 쿼리:**
```sql
-- 테이블 목록 확인
SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'public';

-- 뉴스 데이터 확인
SELECT * FROM news_articles LIMIT 10;

-- ETF 목록 확인
SELECT * FROM etf_list LIMIT 10;
```

---

## 6. 자주 쓰는 명령어

### Docker 명령어

```bash
# pgAdmin 시작
docker compose -f docker-compose-pgadmin.yml up -d

# pgAdmin 중지
docker compose -f docker-compose-pgadmin.yml down

# pgAdmin 상태 확인
docker ps

# pgAdmin 로그 보기
docker logs wye-pgadmin
```

### pgAdmin 단축키

| 단축키 | 기능 |
|--------|------|
| F5 | 쿼리 실행 |
| F7 | Explain Analyze (실행계획) |
| Ctrl+Space | 자동완성 |
| Ctrl+/ | 주석 토글 |

---

## 7. 트러블슈팅

### "Docker Desktop is not running"

Docker Desktop 앱을 실행해주세요.
Windows 작업표시줄 우측 트레이에서 Docker 아이콘이 "running" 상태인지 확인.

### 포트 5050 충돌

다른 프로그램이 5050 포트를 쓰고 있으면:

```yaml
# docker-compose-pgadmin.yml에서 포트 변경
ports:
  - "5051:80"  # 5050 → 5051로 변경
```

변경 후 `http://localhost:5051`로 접속.

### AWS DB 연결 실패 - "connection refused"

1. **EC2 보안그룹** 확인: 5432 포트가 내 IP에 열려있는지
2. **EC2 퍼블릭 IP** 확인: 팀장에게 최신 IP 확인
3. **PostgreSQL 설정** 확인: `pg_hba.conf`에 외부 접속 허용되어 있는지

### AWS DB 연결 실패 - "password authentication failed"

Username과 Password가 정확한지 팀장에게 다시 확인.

### 테이블이 안 보임

1. 좌측 트리에서 **Schemas** → **public** → **Tables** 우클릭
2. **Refresh** 클릭

---

## 8. 참고: 로컬 개발용 전체 환경 (선택)

AWS가 아닌 **로컬에서 PostgreSQL + Redis까지 전부 띄우고 싶으면**:

```bash
docker compose -f docker-compose-local.yml up -d
```

이 경우 서버 등록 시:
- Host name: `postgres` (localhost 아님!)
- 나머지 동일

---

## 환경 요약

| 구분 | pgAdmin 접속 | DB Host | 용도 |
|------|-------------|---------|------|
| **이 가이드 (AWS 연결)** | localhost:5050 | EC2 퍼블릭 IP | 팀 공용 DB 조회 |
| **로컬 전체 환경** | localhost:5050 | `postgres` | 개인 개발/테스트 |

---

## 팀 공유 정보 (팀장이 채워주세요)

```
EC2 퍼블릭 IP: _______________
DB 비밀번호:   _______________
```
