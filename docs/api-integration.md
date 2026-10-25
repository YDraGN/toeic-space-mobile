# API integration

> Đối chiếu với `toeic-space-be` nhánh `develop` tại commit `31dec9f` (25/10/2026). Khi backend thay đổi hợp đồng API, cập nhật file này trong cùng PR tích hợp.

## 1. Kết nối tới backend

Backend là hệ microservices ASP.NET Core ([toeic-space-be](https://github.com/bnguien/toeic-space-be)). App **chỉ gọi qua API Gateway** (YARP), không gọi trực tiếp từng service.

```text
App ──▶ API Gateway (:5050) ──▶ /identity/*   Identity   (auth, hồ sơ, giáo viên)
                            ├─▶ /assessment/* Assessment (đề thi, practice set, ngân hàng câu hỏi)
                            └─▶ /classroom/*  Classroom  (lớp học)
```

| Môi trường | `API_BASE_URL` |
|---|---|
| Emulator → máy dev | `http://10.0.2.2:5050/` |
| Máy thật → máy dev | `http://<IP-LAN>:5050/` |
| Staging / production | `https://<domain>/` (bắt buộc HTTPS) |

- `API_BASE_URL` khai báo trong `local.properties`, đưa vào code qua `BuildConfig.API_BASE_URL`.
- HTTP thường (cleartext) chỉ được bật cho build **debug**, qua `src/debug/res/xml/network_security_config.xml`.
- Chạy backend local: `docker compose --profile identity up -d` (hoặc `assessment`, `classroom`, `full`). Xem README của repo backend.
- Mỗi `XxxApiService` dùng đường dẫn **đầy đủ kèm tiền tố gateway**, ví dụ `@POST("identity/api/auth/login")`.

## 2. Định dạng response

Backend **không** bọc response trong envelope chung.

- **Thành công:** body chính là đối tượng kết quả.
- **Danh sách:** phân trang qua query `page` và `pageSize` (tối đa 100).
- **Lỗi:** dạng `application/problem+json` (RFC 7807), có thêm `code` và `errors`:

```json
{
  "status": 400,
  "title": "Bad Request",
  "detail": "One or more validation errors occurred.",
  "instance": "/api/auth/register",
  "code": "VALIDATION_ERROR",
  "errors": { "Email": ["Email is invalid."] }
}
```

App dựa vào `code` để chọn chuỗi hiển thị. Không hiển thị thẳng `title` hay `detail` cho người dùng.

> **Mã lỗi chưa đồng nhất giữa các service:** Identity dùng `UPPER_SNAKE` (`AUTH_INVALID_CREDENTIALS`), còn Classroom dùng `lower_snake` (`class_not_found`). Mapper của app so sánh không phân biệt hoa thường. Đây là điểm nên đề xuất backend thống nhất.

## 3. Vai trò người dùng

| Role (`AuthenticatedUser.role`) | Là ai | Dùng app mobile? |
|---|---|---|
| `User` | Học viên | **Có**, đối tượng chính |
| `Teacher` | Giáo viên | Chưa có màn hình riêng. Sau này có thể thêm tab "Lớp tôi dạy" (`GET /classroom/api/v1/classes/me`) |
| `Admin` | Quản trị | Không. Việc quản trị làm trên web |

## 4. Xác thực & tài khoản (Identity)

### 4.1 Endpoint

| Mục đích | Method & đường dẫn (sau `/identity`) | Body / ghi chú |
|---|---|---|
| Đăng ký | `POST /api/auth/register` | `fullName`, `email`, `phone?` (số di động Việt Nam), `password`, `confirmPassword`. Trả về `challengeId` dùng cho bước xác thực email |
| Xác thực email | `POST /api/auth/verify-email` | `challengeId`, `otp` |
| Gửi lại OTP | `POST /api/auth/resend-verification` | |
| Đăng nhập | `POST /api/auth/login` | `email`, `password` → `AuthResponse` |
| Refresh | `POST /api/auth/refresh` | Header `X-CSRF-Protection: 1`, refresh token đi qua cookie (xem 4.3) |
| Đăng xuất | `POST /api/auth/logout` | Header `X-CSRF-Protection: 1` |
| Phiên hiện tại | `GET /api/auth/me` | |
| Hồ sơ | `GET` / `PUT /api/v1/users/me` | `fullName`, `phone?`, `avatarUrl?`, `dateOfBirth?`, `gender?`, `biography?`, `targetScore?`, `currentLevel?` |
| Ảnh đại diện | `POST /api/v1/users/me/avatar` | Body `{ "contentType": "image/jpeg" }`, trả về presigned URL. App `PUT` ảnh thẳng lên Cloudflare R2, sau đó lưu `avatarUrl` qua `PUT /api/v1/users/me` |
| Quên mật khẩu: xin OTP | `POST /api/v1/auth/password-reset/otp` | `email` → `cooldownSeconds` |
| Quên mật khẩu: xác thực OTP | `POST /api/v1/auth/password-reset/verify` | `email`, `otp`; server đặt cookie `__Secure-ts_pr` |
| Quên mật khẩu: đặt mật khẩu mới | `POST /api/v1/auth/password-reset/confirm` | `newPassword`, `confirmPassword`; cần cookie `__Secure-ts_pr` |
| Đổi mật khẩu: xin OTP | `POST /api/v1/auth/change-password/otp` | `currentPassword`; cần Bearer |
| Đổi mật khẩu | `PUT /api/v1/auth/change-password` | `currentPassword`, `newPassword`, `confirmPassword`, `otp`; cần Bearer |

`AuthResponse` gồm `accessToken`, `tokenType`, `expiresIn`, `expiresAt`, `user { id, fullName, email, role }`.

Quy tắc mật khẩu mới (đặt lại, đổi mật khẩu): 8–128 ký tự, có ít nhất một chữ in hoa A–Z và một ký tự không phải chữ, số hay khoảng trắng. App kiểm tra trước ở client, nhưng backend vẫn là nơi quyết định cuối cùng.

### 4.2 Token

| | Access token | Refresh token |
|---|---|---|
| Dạng | JWT ES256, sống 10 phút | Chuỗi opaque, sống tối đa 7 ngày, hết hạn sau 24 giờ không dùng |
| Lưu ở app | Trong bộ nhớ | `TokenStorage` (mã hoá bằng Tink, khoá trong Android Keystore), không được backup |
| Gửi đi | `Authorization: Bearer`, do `AuthInterceptor` gắn | Chỉ gửi khi refresh |

- Refresh token **chỉ dùng được một lần**. Nếu gửi lại một token đã dùng, backend thu hồi mọi phiên của tài khoản. Vì vậy `TokenAuthenticator` phải dùng `Mutex`, chỉ một request được refresh tại một thời điểm.
- Refresh thất bại thì xoá session, app quay về màn Sign in.
- Đặt lại hoặc đổi mật khẩu thành công sẽ thu hồi mọi refresh token cũ. App phải xoá session và yêu cầu đăng nhập lại.

### 4.3 ⚠️ Việc cần backend hỗ trợ: cookie và client native

Ba luồng hiện **chỉ** hoạt động qua cookie `HttpOnly; Secure; SameSite=Strict` kèm header CSRF, vốn được thiết kế cho trình duyệt:

| Luồng | Cookie |
|---|---|
| Refresh, logout | `__Secure-ts_rt` (path `/identity/api/auth`) |
| Đặt lại mật khẩu | `__Secure-ts_pr` (path `/identity/api/v1/auth/password-reset`) |

Hệ quả cho mobile:

1. Cookie có cờ `Secure` nên OkHttp không gửi nó qua `http://`. Khi dev trên emulator với backend local, refresh và đặt lại mật khẩu **sẽ không hoạt động**.
2. App phải tự viết `CookieJar` lưu cookie đã mã hoá và tự quản lý đúng path của từng cookie.

**Đề xuất gửi team backend:** thêm chế độ client native qua header `X-Client-Type: mobile`. Khi có header này:

- `login` và `refresh` trả `refreshToken` trong body; `refresh` và `logout` nhận `refreshToken` trong body.
- `password-reset/verify` trả `resetToken` trong body; `password-reset/confirm` nhận `resetToken` trong body.
- Bỏ kiểm tra CSRF, vì client native không có cookie bị gửi tự động.

**Tạm thời,** trước khi backend hỗ trợ: dùng `CookieJar` lưu trong bộ nhớ, và chỉ test các luồng trên với backend chạy HTTPS (staging hoặc reverse proxy local có chứng chỉ).

## 5. Đăng nhập bằng Google

**Trạng thái backend:** đã có `OAuthProvider.Google`, bảng `UserOAuthAccounts`, mã lỗi `OAUTH_ALREADY_LINKED` và `OAUTH_EMAIL_MISMATCH`. **Chưa có endpoint.** Hợp đồng đề xuất:

```text
POST /identity/api/auth/google
{ "idToken": "<Google ID token>", "nonce": "<nonce do app sinh>" }
→ 200 AuthResponse
```

Backend kiểm tra chữ ký của ID token, kiểm tra `aud` bằng **Web client ID**, kiểm tra `iss`, `exp`, `nonce` và `email_verified`. Sau đó tìm hoặc tạo user và liên kết với `UserOAuthAccounts`.

**Mobile cần chuẩn bị:**

| Hạng mục | Chi tiết |
|---|---|
| Google Cloud Console | OAuth client loại **Web** (backend dùng làm `aud`), cộng với OAuth client loại **Android** cho package `com.toeicspace.android` kèm SHA-1 của keystore debug và release (`./gradlew signingReport`) |
| Thư viện | `androidx.credentials:credentials`, `credentials-play-services-auth`, `googleid` |
| Cấu hình | `GOOGLE_WEB_CLIENT_ID` trong `local.properties`, đưa vào `BuildConfig`. Không phải secret |
| Luồng | Sinh nonce → `GetSignInWithGoogleOption` → lấy `GoogleIdTokenCredential.idToken` → gọi backend |
| Đăng xuất | Gọi thêm `CredentialManager.clearCredentialState()` |
| UI | Nút theo [Google branding guidelines](https://developers.google.com/identity/branding-guidelines) |
| Lỗi | Người dùng huỷ, máy không có tài khoản Google, không có Play Services, `OAUTH_EMAIL_MISMATCH` |

## 6. Nội dung học (Assessment)

| Endpoint (sau `/assessment`) | Đăng nhập? | Dùng cho |
|---|---|---|
| `GET /api/v1/practice-sets` | Không | Danh sách bài luyện (tab Luyện tập, Khám phá). Khách xem được |
| `GET /api/v1/practice-sets/{id}` | Không | Chi tiết bài luyện |
| `GET /api/v1/practice-sets/{id}/content` | Không | Nội dung câu hỏi để làm bài |
| `GET /api/v1/tests`, `/tests/categories`, `/tests/{id}` | Không | Danh sách và thông tin đề thi |
| `GET /api/v1/tests/{id}/full` | **Có** | Toàn bộ đề để thi thử |

Các endpoint quản trị (questions, passages, bank, tạo/sửa đề) chỉ dành cho Admin/Teacher, app không gọi.

**Chưa có trên backend:** nộp bài, chấm điểm, lịch sử làm bài, giải thích đáp án, Mistake Notebook.

## 7. Lớp học (Classroom)

Hiện chỉ có endpoint cho **Admin** (quản lý lớp) và **Teacher** (`GET /api/v1/classes/me`, `GET /api/v1/classes/{id}`). Học viên **chưa có** endpoint nào: chưa có ghi danh, tham gia lớp bằng mã, lịch học hay bài tập.

## 8. Mockup cần gì, backend có gì

| Phần giao diện (Stitch) | Backend | Cách làm ở mobile |
|---|---|---|
| Đăng ký, xác thực email, đăng nhập, quên/đổi mật khẩu, hồ sơ, avatar | ✅ Có | Làm thật (lưu ý mục 4.3) |
| Đăng nhập Google | ⏳ Mới có schema | Giấu nút sau feature flag cho tới khi có endpoint |
| Tab Luyện tập, Khám phá (bài luyện, đề thi) | ✅ Có (xem được khi chưa đăng nhập) | Làm thật, kể cả cho khách |
| Thi thử đầy đủ | ✅ Lấy đề / ❌ nộp bài và chấm | Lấy đề thật, phần nộp bài để sau |
| Chuỗi streak, nhiệm vụ hằng ngày, "Bài học dở dang" | ❌ Chưa có | `FakeRepository`, ẩn khỏi bản build release |
| Bài kiểm tra đầu vào (placement test), lộ trình | ❌ Chưa có | Hiện card CTA, bấm vào thì báo "Sắp ra mắt" |
| "Khoá học của tôi", "Nhập mã lớp", lịch học, deadline | ❌ Chưa có cho học viên | Ẩn cho tới khi backend có API ghi danh |
| Khoá học, từ vựng, flashcard (Content service) | ❌ Mới có khung (scaffold) | `FakeRepository` |
| Thông báo (chuông) | ❌ Chưa có | Ẩn badge số lượng |

Quy ước: mọi dữ liệu giả nằm trong `feature/<x>/data/fake/`. Hilt chọn bản giả hay thật qua `@Binds` trong source set `debug` hoặc qua cờ `BuildConfig`. Không trộn dữ liệu giả vào implementation thật.

## 9. OkHttp & Retrofit

```text
OkHttpClient
├── AuthInterceptor          gắn Bearer token, bỏ qua các endpoint auth công khai
├── CsrfHeaderInterceptor    thêm X-CSRF-Protection: 1 cho refresh, logout, password-reset
├── HttpLoggingInterceptor   chỉ ở debug, redactHeader("Authorization")
└── TokenAuthenticator       nhận 401 → refresh (Mutex) → gửi lại request một lần
```

JSON dùng `kotlinx.serialization` với `ignoreUnknownKeys = true` và `explicitNulls = false`.

## 10. Bảng mã lỗi

| HTTP / `code` | `AppError` | String key |
|---|---|---|
| Không có mạng, timeout | `Network` | `common_error_network` |
| 400 `VALIDATION_ERROR` | `Validation(fields)` | Hiện lỗi ngay dưới từng field |
| 401 / 400 `AUTH_INVALID_CREDENTIALS` | `Server` | `auth_error_invalid_credentials` |
| `AUTH_EMAIL_NOT_VERIFIED` | `Server` | Chuyển sang màn Verify email |
| `AUTH_ACCOUNT_LOCKED` | `Server` | `auth_error_account_locked` |
| `AUTH_LOGIN_TEMPORARILY_LOCKED`, 429 | `Server` | `auth_error_login_temporarily_locked` |
| `AUTH_PASSWORD_SAME_AS_OLD` | `Server` | `auth_error_password_same_as_old` |
| `OTP_INVALID_OR_EXPIRED` | `Server` | `auth_error_otp_invalid` |
| `OTP_RATE_LIMIT_EXCEEDED` | `Server` | `auth_error_otp_rate_limited` |
| `USER_EMAIL_ALREADY_EXISTS` / `USER_PHONE_ALREADY_EXISTS` | `Server` | `auth_error_email_exists` / `auth_error_phone_exists` |
| `OAUTH_ALREADY_LINKED` / `OAUTH_EMAIL_MISMATCH` | `Server` | `auth_error_google_already_linked` / `auth_error_google_email_mismatch` |
| `TOKEN_*` khi refresh | `Unauthorized` | Xoá session, quay về Sign in |
| `TOKEN_INVALID` khi đặt lại mật khẩu | `Server` | `auth_error_reset_session_expired`, quay lại bước nhập email |
| 5xx | `Server` | `common_error_server` |

Timer của Mock Test **không** dừng khi mất mạng. Câu trả lời được lưu local và đồng bộ khi có mạng trở lại.
