# 인증 API (Auth)

## 기본 정보
- Base URL: `/api/v1/auth`
- 인증: 일부 엔드포인트 제외하고 JWT 필요

---

## 엔드포인트 목록

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | `/oauth/kakao` | 카카오 로그인 URL 요청 | X |
| POST | `/oauth/kakao/callback` | 카카오 로그인 콜백 처리 | X |
| POST | `/signup` | 이메일 회원가입 | X |
| POST | `/signup/verify` | 이메일 인증 확인 | X |
| POST | `/signup/resend` | 인증 이메일 재발송 | X |
| POST | `/login` | 이메일+비밀번호 로그인 | X |
| POST | `/token/refresh` | Access Token 갱신 | X |
| POST | `/logout` | 로그아웃 | O |
| POST | `/password/reset/request` | 비밀번호 재설정 요청 | X |
| POST | `/password/reset/verify` | 비밀번호 재설정 토큰 검증 | X |
| POST | `/password/reset` | 비밀번호 재설정 | X |

---

## API 상세

### 1. 카카오 로그인 URL 요청
카카오 로그인 인증 페이지로 리다이렉트할 URL을 반환합니다.

**Request**
```
GET /api/v1/auth/oauth/kakao
```

**Response**
```json
{
  "success": true,
  "data": {
    "authUrl": "https://kauth.kakao.com/oauth/authorize?client_id=...&redirect_uri=...&response_type=code"
  }
}
```

---

### 2. 카카오 로그인 콜백 처리
카카오 로그인 인증 후 콜백을 처리하고 JWT 토큰을 발급합니다.

**Request**
```
POST /api/v1/auth/oauth/kakao/callback
Content-Type: application/json
```
```json
{
  "code": "authorization_code_here"
}
```

**Response - 기존 회원**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
    "expiresIn": 3600,
    "isNewUser": false,
    "user": {
      "id": 1,
      "email": "hong@kakao.com",
      "nickname": "홍길동",
      "profileImage": "https://...",
      "loginProvider": "KAKAO"
    }
  }
}
```

**Response - 신규 회원**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
    "expiresIn": 3600,
    "isNewUser": true,
    "user": {
      "id": 1,
      "email": "hong@kakao.com",
      "nickname": "hong@kakao.com",
      "profileImage": "https://...",
      "loginProvider": "KAKAO"
    }
  }
}
```

> 신규 회원의 경우 닉네임이 이메일로 설정됩니다. 마이페이지에서 변경 가능합니다.

---

### 3. 이메일 회원가입
이메일 인증을 통한 회원가입을 요청합니다.

**Request**
```
POST /api/v1/auth/signup
Content-Type: application/json
```
```json
{
  "email": "hong@gmail.com",
  "password": "password123!",
  "passwordConfirm": "password123!",
  "nickname": "홍길동"
}
```

| Field | Type | 필수 | 설명 |
|-------|------|------|------|
| email | string | O | 이메일 주소 |
| password | string | O | 비밀번호 (8자 이상, 영문+숫자+특수문자) |
| passwordConfirm | string | O | 비밀번호 확인 |
| nickname | string | O | 닉네임 (2~20자) |

**Response**
```json
{
  "success": true,
  "message": "인증 이메일이 발송되었습니다. 이메일을 확인해주세요.",
  "data": {
    "email": "hong@gmail.com"
  }
}
```

**Error Response**
```json
{
  "success": false,
  "error": {
    "code": "EMAIL_ALREADY_EXISTS",
    "message": "이미 가입된 이메일입니다."
  }
}
```

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

### 4. 이메일 인증 확인
이메일로 발송된 인증 토큰을 확인합니다.

**Request**
```
POST /api/v1/auth/signup/verify
Content-Type: application/json
```
```json
{
  "email": "hong@gmail.com",
  "token": "123456"
}
```

**Response**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
    "expiresIn": 3600,
    "user": {
      "id": 1,
      "email": "hong@gmail.com",
      "nickname": "홍길동",
      "profileImage": null,
      "loginProvider": "EMAIL"
    }
  },
  "message": "회원가입이 완료되었습니다."
}
```

**Error Response**
```json
{
  "success": false,
  "error": {
    "code": "INVALID_TOKEN",
    "message": "유효하지 않은 인증 코드입니다."
  }
}
```

```json
{
  "success": false,
  "error": {
    "code": "TOKEN_EXPIRED",
    "message": "인증 코드가 만료되었습니다. 재발송을 요청해주세요."
  }
}
```

---

### 5. 인증 이메일 재발송

**Request**
```
POST /api/v1/auth/signup/resend
Content-Type: application/json
```
```json
{
  "email": "hong@gmail.com"
}
```

**Response**
```json
{
  "success": true,
  "message": "인증 이메일이 재발송되었습니다."
}
```

---

### 6. 이메일+비밀번호 로그인

**Request**
```
POST /api/v1/auth/login
Content-Type: application/json
```
```json
{
  "email": "hong@gmail.com",
  "password": "password123!"
}
```

**Response**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
    "expiresIn": 3600,
    "user": {
      "id": 1,
      "email": "hong@gmail.com",
      "nickname": "홍길동",
      "profileImage": "https://...",
      "loginProvider": "EMAIL"
    }
  }
}
```

**Error Response**
```json
{
  "success": false,
  "error": {
    "code": "INVALID_CREDENTIALS",
    "message": "이메일 또는 비밀번호가 일치하지 않습니다."
  }
}
```

```json
{
  "success": false,
  "error": {
    "code": "EMAIL_NOT_VERIFIED",
    "message": "이메일 인증이 완료되지 않았습니다."
  }
}
```

---

### 7. Access Token 갱신

**Request**
```
POST /api/v1/auth/token/refresh
Content-Type: application/json
```
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
}
```

**Response**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "expiresIn": 3600
  }
}
```

**Error Response**
```json
{
  "success": false,
  "error": {
    "code": "TOKEN_EXPIRED",
    "message": "Refresh token has expired"
  }
}
```

---

### 8. 로그아웃

**Request**
```
POST /api/v1/auth/logout
Authorization: Bearer {accessToken}
```

**Response**
```json
{
  "success": true,
  "message": "로그아웃되었습니다."
}
```

---

### 9. 비밀번호 재설정 요청
비밀번호 재설정 링크를 이메일로 발송합니다.

**Request**
```
POST /api/v1/auth/password/reset/request
Content-Type: application/json
```
```json
{
  "email": "hong@gmail.com"
}
```

**Response**
```json
{
  "success": true,
  "message": "비밀번호 재설정 이메일이 발송되었습니다."
}
```

> 보안상 존재하지 않는 이메일이어도 동일한 응답을 반환합니다.

---

### 10. 비밀번호 재설정 토큰 검증

**Request**
```
POST /api/v1/auth/password/reset/verify
Content-Type: application/json
```
```json
{
  "email": "hong@gmail.com",
  "token": "reset_token_here"
}
```

**Response**
```json
{
  "success": true,
  "data": {
    "valid": true
  }
}
```

---

### 11. 비밀번호 재설정

**Request**
```
POST /api/v1/auth/password/reset
Content-Type: application/json
```
```json
{
  "email": "hong@gmail.com",
  "token": "reset_token_here",
  "newPassword": "newPassword456!",
  "newPasswordConfirm": "newPassword456!"
}
```

**Response**
```json
{
  "success": true,
  "message": "비밀번호가 재설정되었습니다. 새 비밀번호로 로그인해주세요."
}
```

**Error Response**
```json
{
  "success": false,
  "error": {
    "code": "INVALID_TOKEN",
    "message": "유효하지 않거나 만료된 토큰입니다."
  }
}
```

---

## 카카오 OAuth 플로우

```
1. 프론트엔드: GET /api/v1/auth/oauth/kakao 호출
2. 백엔드: 카카오 인증 URL 반환
3. 프론트엔드: 사용자를 카카오 로그인 페이지로 리다이렉트
4. 사용자: 카카오 로그인 완료
5. 카카오: redirect_uri로 authorization code 전달
6. 프론트엔드: POST /api/v1/auth/oauth/kakao/callback 호출 (code 포함)
7. 백엔드: 카카오에서 access_token 획득 → 사용자 정보 조회
8. 백엔드: 사용자 생성/조회 → JWT 토큰 발급
9. 프론트엔드: JWT 토큰 저장 → 로그인 완료
```

---

## JWT 토큰 정보

| 토큰 | 유효기간 | 용도 |
|------|----------|------|
| Access Token | 1시간 | API 인증 |
| Refresh Token | 7일 | Access Token 갱신 |

---

## 에러 코드

| 코드 | 에러명 | 설명 |
|------|--------|------|
| AUTH001 | UNAUTHORIZED | 인증이 필요합니다 |
| AUTH002 | INVALID_TOKEN | 유효하지 않은 토큰 |
| AUTH003 | EXPIRED_TOKEN | 토큰 만료 |
| AUTH004 | OAUTH_FAILED | OAuth 인증 실패 |
| AUTH005 | INVALID_CREDENTIALS | 이메일 또는 비밀번호 불일치 |
| AUTH006 | EMAIL_NOT_VERIFIED | 이메일 인증 미완료 |
| AUTH007 | PASSWORD_MISMATCH | 비밀번호 확인 불일치 |
| AUTH008 | WEAK_PASSWORD | 비밀번호 조건 미충족 |
| USER002 | DUPLICATE_EMAIL | 이미 가입된 이메일 |
| USER003 | DUPLICATE_NICKNAME | 이미 사용 중인 닉네임 |
