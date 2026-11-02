# API integration

> Đối chiếu với `toeic-space-be` nhánh `develop` tại commit `70ee748` (30/10/2026), cộng với chế độ client native ở nhánh `feature/mobile-auth-session` (mục 4.3). Khi backend thay đổi hợp đồng API, cập nhật file này trong cùng PR tích hợp.

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

`AuthResponse` gồm `accessToken`, `tokenType`, `expiresIn`, `expiresAt`, `user { id, fullName, email, role }`. Với header `X-Client-Type: mobile` có thêm `refreshToken`, `refreshTokenExpiresAt` (xem 4.3).

Quy tắc mật khẩu. App kiểm tra trước ở `AuthValidator` để không tốn request, backend vẫn là nơi quyết định cuối cùng:

| Luồng | Quy tắc |
|---|---|
| Đăng ký | 8–128 ký tự, có chữ in hoa, **chữ số** và ký tự đặc biệt |
| Đặt lại, đổi mật khẩu | 8–128 ký tự, có chữ in hoa A–Z và ký tự không phải chữ, số hay khoảng trắng |
| Đăng nhập | Chỉ kiểm tra có nhập và tối đa 128 ký tự, để không gợi ý chính sách mật khẩu |

Số điện thoại: di động Việt Nam `0[35789]xxxxxxxx`, chấp nhận `+84`, `0084`, dấu cách, chấm, gạch như backend.

### 4.2 Token

| | Access token | Refresh token |
|---|---|---|
| Dạng | JWT ES256, sống 10 phút | Chuỗi opaque, sống tối đa 7 ngày, hết hạn sau 24 giờ không dùng |
| Lưu ở app | Trong bộ nhớ | `SessionTokenStore`: AES-256-GCM với khoá nằm trong Android Keystore (`KeystoreTokenCipher`), DataStore `toeic_space_session`, không được backup |
| Gửi đi | `Authorization: Bearer`, do `AuthInterceptor` gắn | Chỉ gửi khi refresh |

- Refresh token **chỉ dùng được một lần**. Nếu gửi lại một token đã dùng, backend thu hồi mọi phiên của tài khoản. Vì vậy `TokenRefresher` dùng `Mutex`: chỉ một request được refresh tại một thời điểm, các request đang chờ dùng lại token vừa nhận.
- Refresh bị từ chối (401) thì xoá session, app quay về màn Sign in. Mất mạng hoặc lỗi 5xx thì **giữ** session, để người dùng vẫn ở trong app khi offline.
- Access token không được lưu xuống đĩa. Sau khi mở lại app, `AuthInterceptor` refresh trước request cần đăng nhập đầu tiên, thay vì gửi đi rồi nhận 401. Refresh token đã hết hạn theo đồng hồ máy thì xoá luôn, không gọi server.
- Đặt lại hoặc đổi mật khẩu thành công sẽ thu hồi mọi refresh token cũ. App phải xoá session và yêu cầu đăng nhập lại.

### 4.3 Chế độ client native (`X-Client-Type: mobile`)

Trình duyệt dùng cookie `HttpOnly; Secure` cho refresh token và reset token. App không dùng được cookie này: cờ `Secure` chặn cookie qua `http://` khi dev. Vì vậy backend có chế độ native, bật bằng header `X-Client-Type: mobile` (nhánh `feature/mobile-auth-session` của backend, mô tả ở `docs/AUTHENTICATION.md` mục 2.1):

| Endpoint | Khác với trình duyệt |
|---|---|
| `login`, `refresh` | Body trả thêm `refreshToken`, `refreshTokenExpiresAt`; không đặt cookie |
| `refresh`, `logout` | Đọc `{ "refreshToken": "..." }` trong body |
| `password-reset/verify` | Body trả thêm `resetToken`; không đặt cookie |
| `password-reset/confirm` | Đọc `resetToken` trong body |

- `ClientHeadersInterceptor` gắn `X-Client-Type: mobile` và `X-CSRF-Protection: 1` cho mọi request.
- Ở chế độ native, server không đọc hay ghi cookie, nên app không cần `CookieJar`.
- Nếu đăng nhập trả về mà thiếu `refreshToken` (backend chưa có chế độ native), app coi là lỗi, vì session sẽ không sống qua lần mở app sau.

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
├── AuthInterceptor          gắn Bearer token; refresh trước nếu token thiếu hoặc sắp hết hạn
├── ClientHeadersInterceptor X-Client-Type: mobile, X-CSRF-Protection: 1
├── HttpLoggingInterceptor   chỉ ở debug, mức BASIC (không log body), redactHeader("Authorization")
└── TokenAuthenticator       nhận 401 → refresh (Mutex) → gửi lại request một lần
```

- Endpoint công khai (đăng nhập, đăng ký, OTP) được đánh dấu `@PublicEndpoint`: không gắn token và không kích hoạt refresh.
- `SessionApiService` (refresh, logout) dùng client riêng không có `AuthInterceptor`, để refresh không gọi lại chính nó.

JSON dùng `kotlinx.serialization` với `ignoreUnknownKeys = true` và `explicitNulls = false`.

## 10. Bảng mã lỗi

| HTTP / `code` | `AppError` | String key |
|---|---|---|
| Máy không có mạng (`ConnectivityInterceptor` chặn trước khi gửi) | `Network` | `common_error_network` |
| Có mạng nhưng máy chủ không phản hồi: bị từ chối, timeout, lỗi DNS | `ServerUnreachable` | `common_error_server_unreachable` |
| 400 `VALIDATION_ERROR` | `Validation(fields)` | Hiện lỗi ngay dưới từng field |
| 401 / 400 `AUTH_INVALID_CREDENTIALS` | `Server` | `auth_error_invalid_credentials` |
| 403 `AUTH_EMAIL_NOT_VERIFIED` | `Server` | Gọi `resend-verification` lấy `challengeId` mới, rồi mở màn Verify email |
| `AUTH_ACCOUNT_LOCKED` | `Server` | `auth_error_account_locked` |
| `AUTH_LOGIN_TEMPORARILY_LOCKED`, 429 | `Server` | `auth_error_login_temporarily_locked` |
| `AUTH_PASSWORD_SAME_AS_OLD` | `Server` | `auth_error_password_same_as_old` |
| `OTP_INVALID_OR_EXPIRED` | `Server` | `auth_error_otp_invalid` |
| `OTP_RATE_LIMIT_EXCEEDED` | `Server` | `auth_error_otp_rate_limited` |
| 409 `REGISTRATION_COULD_NOT_BE_COMPLETED` (email hoặc số điện thoại đã được dùng; backend cố ý không nói rõ) | `Server` | `auth_error_registration_failed` |
| `OAUTH_ALREADY_LINKED` / `OAUTH_EMAIL_MISMATCH` | `Server` | `auth_error_google_already_linked` / `auth_error_google_email_mismatch` |
| `TOKEN_*` khi refresh | `Unauthorized` | Xoá session, quay về Sign in |
| `TOKEN_*` khi đặt lại mật khẩu | `Server` | `auth_error_reset_session_expired`, quay lại bước nhập email |
| 5xx | `Server` | `common_error_server` |

Timer của Mock Test **không** dừng khi mất mạng. Câu trả lời được lưu local và đồng bộ khi có mạng trở lại.
