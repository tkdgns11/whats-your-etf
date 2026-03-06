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
| GET | `/types` | 알림 유형 목록 조회 | O |
| GET | `/settings` | 알림 설정 조회 | O |
| PUT | `/settings` | 알림 설정 수정 | O |

---

## 데이터 모델

### 알림 유형 코드 (alert_type)
| code | name | category | 설명 |
|------|------|----------|------|
| ETF_LISTING | ETF 신규 상장 | ETF | 새로운 ETF 상장 |
| ETF_DELISTING_SCHEDULED | ETF 상장폐지 예정 | ETF | 상장폐지 공시 확정 |
| ETF_DELISTING_COMPLETED | ETF 상장폐지 완료 | ETF | 상장폐지 완료 |
| ETF_REBALANCING | ETF 리밸런싱 | ETF | 구성종목 변경 |
| PORTFOLIO_RETURN_5PCT | 포트폴리오 수익률 5% | PORTFOLIO | 수익률 +5% 도달 |
| PORTFOLIO_RETURN_10PCT | 포트폴리오 수익률 10% | PORTFOLIO | 수익률 +10% 도달 |
| PORTFOLIO_LOSS_5PCT | 포트폴리오 손실률 -5% | PORTFOLIO | 손실률 -5% 도달 |
| PORTFOLIO_LOSS_10PCT | 포트폴리오 손실률 -10% | PORTFOLIO | 손실률 -10% 도달 |
| NEWS_ETF_RELATED | 관심 ETF 뉴스 | NEWS | 관심 ETF 관련 뉴스 |
| NEWS_PORTFOLIO_RELATED | 포트폴리오 관련 뉴스 | NEWS | 보유 포트폴리오 관련 뉴스 |
| SYSTEM_ANNOUNCEMENT | 시스템 공지 | SYSTEM | 서비스 공지사항 |

### 참조 대상 유형 (reference_type)
| Type | 설명 | reference_id |
|------|------|--------------|
| ETF | ETF 참조 | etf.id |
| PORTFOLIO | 포트폴리오 참조 | portfolios.id |
| NEWS | 뉴스 참조 | news_article.id |
| DISCLOSURE | 공시 참조 | etf_disclosure.id |

---

## API 상세

### 1. 알림 목록 조회

**Request**
```
GET /api/v1/alerts?page=0&size=20&category=all
Authorization: Bearer {accessToken}
```

| Parameter | Type | 필수 | 설명 |
|-----------|------|------|------|
| page | int | X | 페이지 번호 (기본 0) |
| size | int | X | 페이지 크기 (기본 20) |
| category | string | X | 카테고리 필터 (all/ETF/PORTFOLIO/NEWS/SYSTEM) |

**Response**
```json
{
  "success": true,
  "data": {
    "alerts": [
      {
        "id": 1,
        "alertTypeCode": "ETF_LISTING",
        "alertTypeName": "ETF 신규 상장",
        "category": "ETF",
        "referenceType": "ETF",
        "referenceId": 123,
        "title": "신규 ETF 상장 알림",
        "message": "TIGER AI코리아그로스 ETF가 2025년 1월 20일 상장 예정입니다.",
        "isRead": false,
        "createdAt": "2025-01-17T09:00:00Z"
      },
      {
        "id": 2,
        "alertTypeCode": "ETF_DELISTING_SCHEDULED",
        "alertTypeName": "ETF 상장폐지 예정",
        "category": "ETF",
        "referenceType": "DISCLOSURE",
        "referenceId": 456,
        "title": "ETF 상장폐지 알림",
        "message": "관심 목록에 있는 'XXX ETF'가 2025년 2월 1일 상장폐지 예정입니다.",
        "isRead": false,
        "createdAt": "2025-01-16T14:00:00Z"
      },
      {
        "id": 3,
        "alertTypeCode": "PORTFOLIO_RETURN_5PCT",
        "alertTypeName": "포트폴리오 수익률 5%",
        "category": "PORTFOLIO",
        "referenceType": "PORTFOLIO",
        "referenceId": 789,
        "title": "포트폴리오 수익률 알림",
        "message": "내 포트폴리오 '성장형 전략' 수익률이 +5%를 달성했습니다!",
        "isRead": true,
        "createdAt": "2025-01-15T10:00:00Z"
      },
      {
        "id": 4,
        "alertTypeCode": "NEWS_ETF_RELATED",
        "alertTypeName": "관심 ETF 뉴스",
        "category": "NEWS",
        "referenceType": "NEWS",
        "referenceId": 1001,
        "title": "관심 ETF 뉴스",
        "message": "관심 ETF 'KODEX 반도체' 관련 뉴스가 있습니다: 반도체 업황 회복 신호",
        "isRead": false,
        "createdAt": "2025-01-14T14:30:00Z"
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
| token | string | O | FCM 토큰 (최대 500자) |
| deviceType | string | O | 기기 타입: `ANDROID` / `IOS` / `WEB` |

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

### 9. 알림 유형 목록 조회
사용 가능한 알림 유형 목록을 조회합니다. 알림 설정 화면에서 사용합니다.

**Request**
```
GET /api/v1/alerts/types
Authorization: Bearer {accessToken}
```

**Response**
```json
{
  "success": true,
  "data": {
    "alertTypes": [
      {
        "code": "ETF_LISTING",
        "name": "ETF 신규 상장",
        "category": "ETF",
        "description": "새로운 ETF가 상장되었습니다"
      },
      {
        "code": "ETF_DELISTING_SCHEDULED",
        "name": "ETF 상장폐지 예정",
        "category": "ETF",
        "description": "ETF 상장폐지가 예정되어 있습니다"
      },
      {
        "code": "ETF_DELISTING_COMPLETED",
        "name": "ETF 상장폐지 완료",
        "category": "ETF",
        "description": "ETF 상장폐지가 완료되었습니다"
      },
      {
        "code": "ETF_REBALANCING",
        "name": "ETF 리밸런싱",
        "category": "ETF",
        "description": "ETF 구성종목이 변경되었습니다"
      },
      {
        "code": "PORTFOLIO_RETURN_5PCT",
        "name": "포트폴리오 수익률 5%",
        "category": "PORTFOLIO",
        "description": "포트폴리오 수익률이 5%에 도달했습니다"
      },
      {
        "code": "PORTFOLIO_RETURN_10PCT",
        "name": "포트폴리오 수익률 10%",
        "category": "PORTFOLIO",
        "description": "포트폴리오 수익률이 10%에 도달했습니다"
      },
      {
        "code": "PORTFOLIO_LOSS_5PCT",
        "name": "포트폴리오 손실률 -5%",
        "category": "PORTFOLIO",
        "description": "포트폴리오 손실률이 -5%에 도달했습니다"
      },
      {
        "code": "PORTFOLIO_LOSS_10PCT",
        "name": "포트폴리오 손실률 -10%",
        "category": "PORTFOLIO",
        "description": "포트폴리오 손실률이 -10%에 도달했습니다"
      },
      {
        "code": "NEWS_ETF_RELATED",
        "name": "관심 ETF 뉴스",
        "category": "NEWS",
        "description": "관심 ETF와 관련된 뉴스가 있습니다"
      },
      {
        "code": "NEWS_PORTFOLIO_RELATED",
        "name": "포트폴리오 관련 뉴스",
        "category": "NEWS",
        "description": "보유 포트폴리오와 관련된 뉴스가 있습니다"
      },
      {
        "code": "SYSTEM_ANNOUNCEMENT",
        "name": "시스템 공지",
        "category": "SYSTEM",
        "description": "서비스 공지사항"
      }
    ]
  }
}
```

---

### 10. 알림 설정 조회

**Request**
```
GET /api/v1/alerts/settings
Authorization: Bearer {accessToken}
```

**Response**
```json
{
  "success": true,
  "data": {
    "settings": [
      {
        "alertTypeCode": "ETF_LISTING",
        "alertTypeName": "ETF 신규 상장",
        "category": "ETF",
        "isEnabled": true
      },
      {
        "alertTypeCode": "ETF_DELISTING_SCHEDULED",
        "alertTypeName": "ETF 상장폐지 예정",
        "category": "ETF",
        "isEnabled": true
      },
      {
        "alertTypeCode": "ETF_DELISTING_COMPLETED",
        "alertTypeName": "ETF 상장폐지 완료",
        "category": "ETF",
        "isEnabled": true
      },
      {
        "alertTypeCode": "ETF_REBALANCING",
        "alertTypeName": "ETF 리밸런싱",
        "category": "ETF",
        "isEnabled": true
      },
      {
        "alertTypeCode": "PORTFOLIO_RETURN_5PCT",
        "alertTypeName": "포트폴리오 수익률 5%",
        "category": "PORTFOLIO",
        "isEnabled": true
      },
      {
        "alertTypeCode": "PORTFOLIO_RETURN_10PCT",
        "alertTypeName": "포트폴리오 수익률 10%",
        "category": "PORTFOLIO",
        "isEnabled": true
      },
      {
        "alertTypeCode": "PORTFOLIO_LOSS_5PCT",
        "alertTypeName": "포트폴리오 손실률 -5%",
        "category": "PORTFOLIO",
        "isEnabled": false
      },
      {
        "alertTypeCode": "PORTFOLIO_LOSS_10PCT",
        "alertTypeName": "포트폴리오 손실률 -10%",
        "category": "PORTFOLIO",
        "isEnabled": false
      },
      {
        "alertTypeCode": "NEWS_ETF_RELATED",
        "alertTypeName": "관심 ETF 뉴스",
        "category": "NEWS",
        "isEnabled": true
      },
      {
        "alertTypeCode": "NEWS_PORTFOLIO_RELATED",
        "alertTypeName": "포트폴리오 관련 뉴스",
        "category": "NEWS",
        "isEnabled": false
      },
      {
        "alertTypeCode": "SYSTEM_ANNOUNCEMENT",
        "alertTypeName": "시스템 공지",
        "category": "SYSTEM",
        "isEnabled": true
      }
    ]
  }
}
```

---

### 11. 알림 설정 수정

**Request**
```
PUT /api/v1/alerts/settings
Authorization: Bearer {accessToken}
Content-Type: application/json
```
```json
{
  "settings": [
    {
      "alertTypeCode": "ETF_LISTING",
      "isEnabled": true
    },
    {
      "alertTypeCode": "ETF_DELISTING_SCHEDULED",
      "isEnabled": true
    },
    {
      "alertTypeCode": "PORTFOLIO_RETURN_5PCT",
      "isEnabled": false
    },
    {
      "alertTypeCode": "NEWS_ETF_RELATED",
      "isEnabled": false
    }
  ]
}
```

| Field | Type | 필수 | 설명 |
|-------|------|------|------|
| settings | array | O | 설정 변경할 알림 유형 목록 (최소 1개) |
| settings[].alertTypeCode | string | O | 알림 유형 코드 (최대 30자, alert_type 테이블 참조) |
| settings[].isEnabled | boolean | O | 활성화 여부: `true` / `false` |

**알림 유형 코드 (alertTypeCode)**
| 코드 | 설명 |
|------|------|
| ETF_LISTING | ETF 신규 상장 |
| ETF_DELISTING_SCHEDULED | ETF 상장폐지 예정 |
| ETF_DELISTING_COMPLETED | ETF 상장폐지 완료 |
| ETF_REBALANCING | ETF 리밸런싱 |
| PORTFOLIO_RETURN_5PCT | 포트폴리오 수익률 +5% |
| PORTFOLIO_RETURN_10PCT | 포트폴리오 수익률 +10% |
| PORTFOLIO_LOSS_5PCT | 포트폴리오 손실률 -5% |
| PORTFOLIO_LOSS_10PCT | 포트폴리오 손실률 -10% |
| NEWS_ETF_RELATED | 관심 ETF 관련 뉴스 |
| NEWS_PORTFOLIO_RELATED | 포트폴리오 관련 뉴스 |
| SYSTEM_ANNOUNCEMENT | 시스템 공지 |

**Response**
```json
{
  "success": true,
  "message": "알림 설정이 변경되었습니다.",
  "data": {
    "updatedCount": 4
  }
}
```

---

## 푸시 알림 시나리오

### ETF 상장 알림
```json
{
  "title": "신규 ETF 상장 알림",
  "body": "TIGER AI코리아그로스 ETF가 내일 상장됩니다.",
  "data": {
    "alertTypeCode": "ETF_LISTING",
    "referenceType": "ETF",
    "referenceId": "123"
  }
}
```

### ETF 상장폐지 알림
```json
{
  "title": "ETF 상장폐지 알림",
  "body": "관심 ETF 'XXX'가 상장폐지 예정입니다.",
  "data": {
    "alertTypeCode": "ETF_DELISTING_SCHEDULED",
    "referenceType": "DISCLOSURE",
    "referenceId": "456"
  }
}
```

### 포트폴리오 수익률 알림
```json
{
  "title": "포트폴리오 수익률 알림",
  "body": "내 포트폴리오 '성장형 전략' 수익률이 +5%를 달성했습니다!",
  "data": {
    "alertTypeCode": "PORTFOLIO_RETURN_5PCT",
    "referenceType": "PORTFOLIO",
    "referenceId": "789"
  }
}
```

### 포트폴리오 손실률 알림
```json
{
  "title": "포트폴리오 손실률 알림",
  "body": "내 포트폴리오 '성장형 전략' 손실률이 -5%에 도달했습니다.",
  "data": {
    "alertTypeCode": "PORTFOLIO_LOSS_5PCT",
    "referenceType": "PORTFOLIO",
    "referenceId": "789"
  }
}
```

### 관련 뉴스 알림
```json
{
  "title": "관심 ETF 뉴스",
  "body": "KODEX 반도체 관련: 반도체 업황 회복 신호",
  "data": {
    "alertTypeCode": "NEWS_ETF_RELATED",
    "referenceType": "NEWS",
    "referenceId": "1001"
  }
}
```

### ETF 리밸런싱 알림
```json
{
  "title": "ETF 리밸런싱 완료",
  "body": "관심 ETF 'TIGER 반도체' 구성종목이 변경되었습니다.",
  "data": {
    "alertTypeCode": "ETF_REBALANCING",
    "referenceType": "ETF",
    "referenceId": "234"
  }
}
```

### 시스템 공지 알림
```json
{
  "title": "서비스 점검 안내",
  "body": "2025년 1월 20일 02:00~04:00 서비스 점검이 있습니다.",
  "data": {
    "alertTypeCode": "SYSTEM_ANNOUNCEMENT",
    "referenceType": null,
    "referenceId": null
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
| ALERT004 | INVALID_ALERT_TYPE_CODE | 유효하지 않은 알림 유형 코드 |
