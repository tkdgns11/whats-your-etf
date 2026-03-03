# 알림 API (Alerts)

## 기본 정보
- Base URL: `/api/v1/alerts`
- 인증: JWT 필요

---

## 엔드포인트 목록

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | `/` | 알림 목록 조회 | O |
| GET | `/unread/count` | 읽지 않은 알림 수 | O |
| PUT | `/{alertId}/read` | 알림 읽음 처리 | O |
| PUT | `/read-all` | 모든 알림 읽음 처리 | O |
| DELETE | `/{alertId}` | 알림 삭제 | O |
| DELETE | `/` | 읽은 알림 전체 삭제 | O |
| POST | `/fcm/token` | FCM 토큰 등록 | O |
| DELETE | `/fcm/token` | FCM 토큰 삭제 | O |

---

## API 상세

### 1. 알림 목록 조회

**Request**
```
GET /api/v1/alerts?page=0&size=20&type=all
Authorization: Bearer {accessToken}
```

| Parameter | Type | 필수 | 설명 |
|-----------|------|------|------|
| page | int | X | 페이지 번호 (기본 0) |
| size | int | X | 페이지 크기 (기본 20) |
| type | string | X | 알림 유형 필터 (all/LISTING/DELISTING/PRICE_CHANGE) |

**Response**
```json
{
  "success": true,
  "data": {
    "alerts": [
      {
        "id": 1,
        "alertType": "LISTING",
        "etfTicker": "475090",
        "title": "신규 ETF 상장 알림",
        "message": "TIGER AI코리아그로스 ETF가 2025년 1월 20일 상장 예정입니다.",
        "isRead": false,
        "createdAt": "2025-01-17T09:00:00Z"
      },
      {
        "id": 2,
        "alertType": "DELISTING",
        "etfTicker": "123456",
        "title": "ETF 상장폐지 알림",
        "message": "관심 목록에 있는 'XXX ETF'가 2025년 2월 1일 상장폐지 예정입니다.",
        "isRead": false,
        "createdAt": "2025-01-16T14:00:00Z"
      },
      {
        "id": 3,
        "alertType": "PRICE_CHANGE",
        "etfTicker": "069500",
        "title": "ETF 가격 변동 알림",
        "message": "KODEX 200의 가격이 전일 대비 5% 이상 변동했습니다.",
        "isRead": true,
        "createdAt": "2025-01-15T10:00:00Z"
      }
    ],
    "page": 0,
    "totalPages": 5,
    "totalElements": 45,
    "unreadCount": 2
  }
}
```

---

### 2. 읽지 않은 알림 수

**Request**
```
GET /api/v1/alerts/unread/count
Authorization: Bearer {accessToken}
```

**Response**
```json
{
  "success": true,
  "data": {
    "unreadCount": 5
  }
}
```

---

### 3. 알림 읽음 처리

**Request**
```
PUT /api/v1/alerts/1/read
Authorization: Bearer {accessToken}
```

**Response**
```json
{
  "success": true,
  "message": "알림을 읽음 처리했습니다."
}
```

---

### 4. 모든 알림 읽음 처리

**Request**
```
PUT /api/v1/alerts/read-all
Authorization: Bearer {accessToken}
```

**Response**
```json
{
  "success": true,
  "message": "모든 알림을 읽음 처리했습니다.",
  "data": {
    "updatedCount": 5
  }
}
```

---

### 5. 알림 삭제

**Request**
```
DELETE /api/v1/alerts/1
Authorization: Bearer {accessToken}
```

**Response**
```json
{
  "success": true,
  "message": "알림이 삭제되었습니다."
}
```

---

### 6. 읽은 알림 전체 삭제

**Request**
```
DELETE /api/v1/alerts
Authorization: Bearer {accessToken}
```

**Response**
```json
{
  "success": true,
  "message": "읽은 알림이 모두 삭제되었습니다.",
  "data": {
    "deletedCount": 10
  }
}
```

---

### 7. FCM 토큰 등록
푸시 알림을 위한 FCM 토큰을 등록합니다.

**Request**
```
POST /api/v1/alerts/fcm/token
Authorization: Bearer {accessToken}
Content-Type: application/json
```
```json
{
  "token": "fcm_token_string_here",
  "deviceType": "ANDROID"
}
```

| Field | Type | 필수 | 설명 |
|-------|------|------|------|
| token | string | O | FCM 토큰 |
| deviceType | string | O | ANDROID / IOS / WEB |

**Response**
```json
{
  "success": true,
  "message": "FCM 토큰이 등록되었습니다."
}
```

---

### 8. FCM 토큰 삭제
로그아웃 시 FCM 토큰을 삭제합니다.

**Request**
```
DELETE /api/v1/alerts/fcm/token
Authorization: Bearer {accessToken}
Content-Type: application/json
```
```json
{
  "token": "fcm_token_string_here"
}
```

**Response**
```json
{
  "success": true,
  "message": "FCM 토큰이 삭제되었습니다."
}
```

---

## 알림 유형

| Type | 설명 | 발송 조건 |
|------|------|----------|
| LISTING | ETF 신규 상장 | 매일 상장 예정 ETF 확인 후 발송 |
| DELISTING | ETF 상장폐지 | 관심 ETF 또는 포트폴리오 내 ETF 상장폐지 시 |
| PRICE_CHANGE | 가격 변동 | 종가 대비 5%/10% 변동 시 (1일 1회) |

---

## 푸시 알림 시나리오

### ETF 상장 알림
```json
{
  "title": "신규 ETF 상장 알림",
  "body": "TIGER AI코리아그로스 ETF가 내일 상장됩니다.",
  "data": {
    "alertType": "LISTING",
    "etfTicker": "475090"
  }
}
```

### ETF 상장폐지 알림
```json
{
  "title": "ETF 상장폐지 알림",
  "body": "관심 ETF 'XXX'가 상장폐지 예정입니다.",
  "data": {
    "alertType": "DELISTING",
    "etfTicker": "123456"
  }
}
```

### 포트폴리오 수익률 알림
```json
{
  "title": "포트폴리오 수익률 알림",
  "body": "내 포트폴리오 수익률이 +5%를 달성했습니다!",
  "data": {
    "alertType": "PRICE_CHANGE",
    "portfolioId": "123"
  }
}
```

---

## 에러 코드

| 코드 | 에러명 | 설명 |
|------|--------|------|
| ALERT001 | ALERT_NOT_FOUND | 알림을 찾을 수 없음 |
| ALERT002 | FCM_TOKEN_INVALID | 유효하지 않은 FCM 토큰 |
| ALERT003 | FCM_TOKEN_ALREADY_EXISTS | 이미 등록된 FCM 토큰 |
