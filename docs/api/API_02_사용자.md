# 사용자 API (User)

## 기본 정보
- Base URL: `/api/v1/users`
- 인증: JWT 필요

---

## 엔드포인트 목록

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | `/me` | 내 정보 조회 | O |
| PATCH | `/me` | 내 정보 수정 | O |
| PUT | `/me/password` | 비밀번호 변경 | O |
| DELETE | `/me` | 회원 탈퇴 | O |
| GET | `/check-nickname` | 닉네임 중복 체크 | X |
| GET | `/{userId}` | 특정 사용자 조회 | X |
| GET | `/me/favorites` | 관심 ETF 목록 조회 | O |
| POST | `/me/favorites/{etfId}` | 관심 ETF 추가 | O |
| DELETE | `/me/favorites/{etfId}` | 관심 ETF 삭제 | O |
| GET | `/me/favorites/{etfId}/check` | 관심 ETF 여부 확인 | O |
| GET | `/me/holdings` | 보유 ETF 목록 조회 (마이데이터) | O |
| POST | `/me/holdings/sync` | 마이데이터 동기화 | O |

---

## API 상세

### 1. 내 정보 조회

**Request**
```
GET /api/v1/users/me
Authorization: Bearer {accessToken}
```

**Response**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "email": "hong@gmail.com",
    "nickname": "홍길동",
    "loginProvider": "KAKAO",
    "hasPassword": false,
    "createdAt": "2025-01-10T10:00:00Z"
  }
}
```

---

### 2. 내 정보 수정

**Request**
```
PATCH /api/v1/users/me
Authorization: Bearer {accessToken}
Content-Type: application/json
```
```json
{
  "nickname": "길동이"
}
```

| Field | Type | 필수 | 설명 |
|-------|------|------|------|
| nickname | string | X | 닉네임 (2~20자, 한글/영문/숫자만 허용, 정규식: `^[가-힣a-zA-Z0-9]{2,20}$`) |

**Response**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "email": "hong@gmail.com",
    "nickname": "길동이",
    "loginProvider": "KAKAO"
  },
  "message": "프로필이 수정되었습니다."
}
```

**Error Response**
```json
{
  "success": false,
  "error": {
    "code": "NICKNAME_ALREADY_EXISTS",
    "message": "이미 사용 중인 닉네임입니다."
  }
}
```

---

### 3. 닉네임 중복 체크

**Request**
```
GET /api/v1/users/check-nickname?nickname=홍길동
```

| Parameter | Type | 필수 | 설명 |
|-----------|------|------|------|
| nickname | string | O | 확인할 닉네임 (2~20자) |

**Response (사용 가능)**
```json
{
  "success": true,
  "data": true
}
```

**Response (중복)**
```json
{
  "success": true,
  "data": false
}
```

---

### 4. 특정 사용자 조회

**Request**
```
GET /api/v1/users/{userId}
```

| Parameter | Type | 필수 | 설명 |
|-----------|------|------|------|
| userId | number | O | 사용자 ID (양수 정수) |

**Response**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "email": "hong@gmail.com",
    "nickname": "홍길동",
    "loginProvider": "KAKAO",
    "createdAt": "2025-01-10T10:00:00Z"
  }
}
```

**Error Response**
```json
{
  "success": false,
  "error": {
    "code": "USER_NOT_FOUND",
    "message": "사용자를 찾을 수 없습니다."
  }
}
```

---

### 5. 비밀번호 변경 (미구현)
이메일 로그인 사용자 또는 비밀번호를 설정한 소셜 로그인 사용자용

**Request**
```
PUT /api/v1/users/me/password
Authorization: Bearer {accessToken}
Content-Type: application/json
```
```json
{
  "currentPassword": "oldPassword123!",
  "newPassword": "newPassword456!",
  "newPasswordConfirm": "newPassword456!"
}
```

| Field | Type | 필수 | 설명 |
|-------|------|------|------|
| currentPassword | string | O | 현재 비밀번호 (평문 전송, 최대 72자) |
| newPassword | string | O | 새 비밀번호 (인증 API 비밀번호 규칙 참조) |
| newPasswordConfirm | string | O | 새 비밀번호 확인 |

**Response**
```json
{
  "success": true,
  "message": "비밀번호가 변경되었습니다."
}
```

**Error Response**
```json
{
  "success": false,
  "error": {
    "code": "INVALID_PASSWORD",
    "message": "현재 비밀번호가 일치하지 않습니다."
  }
}
```

---

### 6. 회원 탈퇴

**Request**
```
DELETE /api/v1/users/me
Authorization: Bearer {accessToken}
Content-Type: application/json
```
```json
{
  "password": "password123!",
  "reason": "서비스를 더 이상 사용하지 않습니다."
}
```

| Field | Type | 필수 | 설명 |
|-------|------|------|------|
| password | string | 조건부 | 비밀번호 (이메일 로그인 시 필수, 평문 전송, 최대 72자) |
| reason | string | X | 탈퇴 사유 (최대 500자) |

**Response**
```json
{
  "success": true,
  "message": "회원 탈퇴가 완료되었습니다."
}
```

---

### 7. 관심 ETF 목록 조회

**Request**
```
GET /api/v1/users/me/favorites
Authorization: Bearer {accessToken}
```

**Response**
```json
{
  "success": true,
  "data": {
    "favorites": [
      {
        "ticker": "069500",
        "name": "KODEX 200",
        "currentPrice": 35420,
        "changeRate": 1.25,
        "addedAt": "2025-01-15T10:00:00Z"
      },
      {
        "ticker": "102110",
        "name": "TIGER 200",
        "currentPrice": 35890,
        "changeRate": -0.32,
        "addedAt": "2025-01-16T14:30:00Z"
      }
    ],
    "totalCount": 2
  }
}
```

---

### 8. 관심 ETF 추가 (좋아요)

**Request**
```
POST /api/v1/users/me/favorites/{etfId}
Authorization: Bearer {accessToken}
```

| Parameter | Type | 필수 | 설명 |
|-----------|------|------|------|
| etfId | number | O | ETF ID (양수 정수) |

**Response**
```json
{
  "success": true,
  "message": "관심 ETF에 추가되었습니다.",
  "data": {
    "etfId": 1,
    "ticker": "069500",
    "name": "KODEX 200"
  }
}
```

**Error Response**
```json
{
  "success": false,
  "error": {
    "code": "ALREADY_FAVORITE",
    "message": "이미 관심 ETF에 추가되어 있습니다."
  }
}
```

---

### 9. 관심 ETF 삭제

**Request**
```
DELETE /api/v1/users/me/favorites/{etfId}
Authorization: Bearer {accessToken}
```

**Response**
```json
{
  "success": true,
  "message": "관심 ETF에서 삭제되었습니다."
}
```

---

### 10. 관심 ETF 여부 확인

**Request**
```
GET /api/v1/users/me/favorites/{etfId}/check
Authorization: Bearer {accessToken}
```

| Parameter | Type | 필수 | 설명 |
|-----------|------|------|------|
| etfId | number | O | ETF ID (양수 정수) |

**Response (관심 등록됨)**
```json
{
  "success": true,
  "data": true
}
```

**Response (미등록)**
```json
{
  "success": true,
  "data": false
}
```

---

### 11. 보유 ETF 목록 조회 (마이데이터)

**Request**
```
GET /api/v1/users/me/holdings
Authorization: Bearer {accessToken}
```

**Response**
```json
{
  "success": true,
  "data": {
    "holdings": [
      {
        "ticker": "069500",
        "name": "KODEX 200",
        "quantity": 100,
        "avgPrice": 34500,
        "currentPrice": 35420,
        "totalValue": 3542000,
        "profitLoss": 92000,
        "profitLossRate": 2.67
      },
      {
        "ticker": "102110",
        "name": "TIGER 200",
        "quantity": 50,
        "avgPrice": 36000,
        "currentPrice": 35890,
        "totalValue": 1794500,
        "profitLoss": -5500,
        "profitLossRate": -0.31
      }
    ],
    "totalValue": 5336500,
    "totalProfitLoss": 86500,
    "lastSyncedAt": "2025-01-17T09:00:00Z"
  }
}
```

---

### 12. 마이데이터 동기화

**Request**
```
POST /api/v1/users/me/holdings/sync
Authorization: Bearer {accessToken}
```

**Response**
```json
{
  "success": true,
  "message": "마이데이터 동기화가 완료되었습니다.",
  "data": {
    "syncedAt": "2025-01-17T10:00:00Z",
    "updatedCount": 3
  }
}
```

**Error Response**
```json
{
  "success": false,
  "error": {
    "code": "MYDATA_NOT_CONNECTED",
    "message": "마이데이터 연동이 필요합니다."
  }
}
```

---

## 에러 코드

| 코드 | 에러명 | 설명 |
|------|--------|------|
| USER001 | USER_NOT_FOUND | 사용자를 찾을 수 없음 |
| USER003 | DUPLICATE_NICKNAME | 이미 사용 중인 닉네임 |
| USER004 | INVALID_PASSWORD | 현재 비밀번호 불일치 |
| USER005 | ALREADY_FAVORITE | 이미 관심 ETF에 추가됨 |
| USER006 | FAVORITE_NOT_FOUND | 관심 ETF를 찾을 수 없음 |
| USER007 | MYDATA_NOT_CONNECTED | 마이데이터 미연동 |
| USER008 | MYDATA_SYNC_FAILED | 마이데이터 동기화 실패 |
| ETF001 | ETF_NOT_FOUND | ETF를 찾을 수 없음 |
