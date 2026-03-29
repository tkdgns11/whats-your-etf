# Android 포팅 가이드

## 1. 개발 환경

### 1.1 필수 설치 항목

| 구분 | 버전 | 비고 |
|------|------|------|
| **Android Studio** | Ladybug (2024.2.1+) | 최신 버전 권장 |
| **JDK** | 17+ | Android Studio 내장 사용 가능 |
| **Android SDK** | API 35 (compileSdk) | SDK Manager에서 설치 |
| **Kotlin** | 2.1.0 | 프로젝트에서 자동 관리 |
| **Gradle** | 8.7+ | Wrapper 사용 |

### 1.2 SDK 요구사항

| 항목 | 값 |
|------|-----|
| compileSdk | 35 |
| minSdk | 33 (Android 13) |
| targetSdk | 35 |

---

## 2. 프로젝트 구조

```
frontend/WYE/
├── app/
│   ├── src/main/
│   │   ├── java/com/d102/wye/    # 소스 코드
│   │   └── res/                   # 리소스
│   ├── keystore/
│   │   └── debug.keystore         # 디버그 키스토어
│   ├── google-services.json       # Firebase 설정
│   └── build.gradle.kts           # 앱 빌드 설정
├── gradle/
│   └── libs.versions.toml         # 버전 카탈로그
├── local.properties               # 로컬 설정 (Git 제외)
└── build.gradle.kts               # 루트 빌드 설정
```

---

## 3. 빌드 환경 설정

### 3.1 local.properties 설정

프로젝트 루트(`frontend/WYE/`)에 `local.properties` 파일 생성:

```properties
sdk.dir=C\:\\Users\\{사용자명}\\AppData\\Local\\Android\\Sdk
KAKAO_NATIVE_APP_KEY={KAKAO_NATIVE_APP_KEY}
```

> **주의**: `sdk.dir` 경로는 본인 PC의 Android SDK 경로로 수정

### 3.2 필수 파일 배치

| 파일 | 위치 | 설명 |
|------|------|------|
| `debug.keystore` | `app/keystore/` | 디버그 서명 키 |
| `google-services.json` | `app/` | Firebase 설정 |
| `local.properties` | 프로젝트 루트 | SDK 경로, API 키 |

### 3.3 환경 변수

| 변수명 | 위치 | 설명 |
|--------|------|------|
| `KAKAO_NATIVE_APP_KEY` | local.properties | 카카오 Native App Key |
| `sdk.dir` | local.properties | Android SDK 경로 |

---

## 4. 사용 라이브러리

### 4.1 Android/Kotlin 핵심

| 라이브러리 | 버전 | 용도 |
|------------|------|------|
| Kotlin | 2.1.0 | 언어 |
| Kotlin Coroutines | 1.7.3 | 비동기 처리 |
| AndroidX Core KTX | 1.15.0 | Kotlin 확장 |
| Lifecycle | 2.10.0 | 생명주기 관리 |
| Activity Compose | 1.10.0 | Compose Activity |

### 4.2 Jetpack Compose UI

| 라이브러리 | 버전 | 용도 |
|------------|------|------|
| Compose BOM | 2025.05.01 | Compose 버전 관리 |
| Material3 | BOM 관리 | UI 컴포넌트 |
| Navigation Compose | 2.9.0 | 화면 이동 |
| Material Icons Extended | 1.6.1 | 아이콘 |

### 4.3 네트워크

| 라이브러리 | 버전 | 용도 |
|------------|------|------|
| Retrofit | 2.11.0 | HTTP 클라이언트 |
| Retrofit Gson Converter | 2.11.0 | JSON 파싱 |
| OkHttp | 4.12.0 | HTTP 엔진 |
| OkHttp Logging | 4.12.0 | 네트워크 로깅 |
| Gson | 2.10.1 | JSON 직렬화 |

### 4.4 DI & 데이터

| 라이브러리 | 버전 | 용도 |
|------------|------|------|
| Hilt | 2.56.2 | 의존성 주입 |
| Hilt Navigation Compose | 1.2.0 | Hilt + Navigation |
| Room | 2.7.1 | 로컬 DB |
| DataStore | 1.1.4 | 설정 저장 |

### 4.5 기타

| 라이브러리 | 버전 | 용도 |
|------------|------|------|
| Coil | 2.5.0 | 이미지 로딩 |
| Coil GIF | 2.5.0 | GIF 지원 |
| Lottie | 6.3.0 | 애니메이션 |
| Timber | 5.0.1 | 로깅 |
| Firebase BOM | 32.7.2 | Firebase 버전 관리 |
| Firebase Messaging | BOM 관리 | FCM 푸시 알림 |

---

## 5. 빌드 및 실행

### 5.1 Android Studio에서 빌드

1. Android Studio에서 `frontend/WYE` 폴더 열기
2. Gradle Sync 완료 대기
3. `app` 모듈 선택 후 Run (Shift+F10)

### 5.2 명령줄 빌드

```bash
cd frontend/WYE

# Debug APK 빌드
./gradlew assembleDebug

# Release APK 빌드
./gradlew assembleRelease

# 빌드 결과물 위치
# app/build/outputs/apk/debug/app-debug.apk
```

### 5.3 빌드 에러 해결

| 에러 | 해결 방법 |
|------|----------|
| SDK not found | `local.properties`에 `sdk.dir` 설정 |
| Keystore not found | `app/keystore/debug.keystore` 파일 확인 |
| google-services.json missing | `app/google-services.json` 파일 확인 |
| Gradle sync failed | File > Invalidate Caches 후 재시작 |

---

## 6. 외부 서비스

### 6.1 카카오 로그인

| 항목 | 값 |
|------|-----|
| 서비스 | Kakao Developers |
| 용도 | 소셜 로그인 |
| Native App Key | `{KAKAO_NATIVE_APP_KEY}` |
| 설정 위치 | `local.properties` |
| 콘솔 URL | https://developers.kakao.com |

**카카오 콘솔 설정 필요사항:**
- 플랫폼 > Android 등록
- 패키지명: `com.d102.wye`
- 키 해시 등록 (debug.keystore 기준)

### 6.2 Firebase (FCM)

| 항목 | 값 |
|------|-----|
| 서비스 | Firebase Cloud Messaging |
| 용도 | 푸시 알림 |
| 설정 파일 | `google-services.json` |
| 파일 위치 | `app/` |
| 콘솔 URL | https://console.firebase.google.com |

**Firebase 콘솔 설정:**
- 프로젝트 설정 > 일반 > Android 앱 추가
- 패키지명: `com.d102.wye`
- `google-services.json` 다운로드 후 `app/` 폴더에 배치

---

## 7. 배포 특이사항

### 7.1 서명 설정

**Debug 빌드:**
- `app/keystore/debug.keystore` 사용
- `build.gradle.kts`에 자동 설정됨

**Release 빌드:**
- 별도 Release Keystore 필요
- Play Store 배포 시 필수

### 7.2 API 서버 설정

앱 내 API Base URL 설정 위치:
- `app/src/main/java/.../di/NetworkModule.kt` (또는 유사 파일)

| 환경 | Base URL |
|------|----------|
| 로컬 | http://10.0.2.2:8080 (에뮬레이터) |
| 운영 | https://j14d102.p.ssafy.io |

### 7.3 ProGuard

- `app/proguard-rules.pro`에 난독화 규칙 정의
- Release 빌드 시 `isMinifyEnabled = false` (현재 비활성화)

### 7.4 테스트 기기 요구사항

- Android 13 (API 33) 이상
- Google Play Services 설치 (FCM 필수)
- 인터넷 연결

---

## 8. 버전 관리

### 8.1 버전 카탈로그

`gradle/libs.versions.toml` 파일에서 모든 의존성 버전 중앙 관리:

```toml
[versions]
agp = "8.7.3"
kotlin = "2.1.0"
composeBom = "2025.05.01"
hilt = "2.56.2"
...
```

### 8.2 앱 버전

`app/build.gradle.kts`:
```kotlin
versionCode = 1
versionName = "1.0"
```
