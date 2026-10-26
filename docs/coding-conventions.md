# Coding conventions

Định dạng code được kiểm tra tự động bằng `./gradlew ktlintCheck` (cấu hình trong `.editorconfig`). Trước khi push, chạy `./gradlew ktlintFormat`.

## 1. Kotlin

| Đối tượng | Quy tắc | Ví dụ |
|---|---|---|
| Class, interface, object | PascalCase | `SignInViewModel`, `AuthRepository` |
| Function, property | camelCase | `refreshSession()`, `isLoading` |
| Hằng số `const val` | UPPER_SNAKE_CASE | `NETWORK_TIMEOUT_SECONDS` |
| Package | chữ thường, không gạch dưới | `com.toeicspace.android.feature.auth` |
| Boolean | tiền tố `is`, `has`, `can`, `should` | `isOnboardingCompleted` |
| Flow, StateFlow | danh từ, không thêm hậu tố `Flow` | `val session: Flow<SessionState>` |

- Giới hạn 100 ký tự mỗi dòng. Dùng trailing comma ở danh sách tham số nhiều dòng.
- Không dùng `!!`. Dùng `?:`, `requireNotNull(...)` kèm thông điệp, hoặc thiết kế lại để không có null.
- `data class` cho state bất biến. `sealed interface` cho tập trạng thái hoặc kết quả.
- Mặc định `private` hoặc `internal`. Chỉ `public` những gì feature khác thật sự cần.
- KDoc cho API public của `core` viết bằng tiếng Anh. Comment giải thích trong code có thể viết tiếng Việt.

## 2. Hậu tố theo vai trò

| Vai trò | Hậu tố | Ví dụ |
|---|---|---|
| Route type-safe | `Destination` | `SignInDestination` |
| Composable có ViewModel | `Route` | `SignInRoute` |
| Composable stateless của một màn hình | `Screen` | `SignInScreen` |
| ViewModel / UI state | `ViewModel` / `UiState` | `SignInViewModel`, `SignInUiState` |
| Retrofit interface | `ApiService` | `AuthApiService` |
| DTO | `Request`, `Response`, `Dto` | `SignInRequest`, `AuthResponse`, `UserDto` |
| Interface / implementation của Repository | `Repository` / `RepositoryImpl` | `AuthRepository`, `AuthRepositoryImpl` |
| Data source | `DataSource` | `UserPreferencesDataSource` |
| Use case | động từ + `UseCase` | `ResolveStartDestinationUseCase` |
| Hilt module | `Module` | `NetworkModule` |
| Mapper | hàm mở rộng `toDomain()`, `toDto()` | `fun UserDto.toDomain(): User` |

**Một khái niệm, một thuật ngữ:** dùng `SignIn` / `SignUp` / `SignOut` ở mọi nơi (code, route, string key, icon). Không trộn với `Login` / `Register`.

## 3. Jetpack Compose

- Composable trả về `Unit` đặt tên PascalCase theo danh từ: `CourseCard`. Composable trả về giá trị đặt tên camelCase: `rememberPagerState()`. Chỉ dùng tiền tố `remember` khi hàm thật sự gọi `remember`.
- Thứ tự tham số: tham số bắt buộc, `modifier: Modifier = Modifier`, tham số tùy chọn, cuối cùng là trailing lambda `content`.
- `modifier` chỉ được áp dụng cho phần tử **ngoài cùng** của composable.
- Composable trong design system có tiền tố `Toeic` (`ToeicButton`, `ToeicIconButton`) để không trùng với Material 3.
- Composable con nhận state và callback qua tham số (state hoisting). Chỉ `XxxRoute` được gọi `hiltViewModel()`.
- Callback đặt tên `onXxx` theo sự kiện của người dùng: `onSignInClick`, `onEmailChange`.
- Animation chạy liên tục thì đọc state trong `graphicsLayer {}` hoặc `drawBehind {}`, không đọc trong `offset(x.dp)`, để tránh recompose mỗi frame.
- Không hardcode màu, cỡ chữ hay khoảng cách. Quy tắc dùng token xem ở [design-system.md](design-system.md).

### Preview

- Mọi `XxxScreen` và component dùng chung có ít nhất một `@Preview`, đặt ở cuối file, khai báo `private`.
- Tên preview theo dạng `<TênComposable><Biến thể>Preview`, ví dụ `SignInScreenErrorPreview`.
- Màn hình có nhiều trạng thái thì có một preview cho mỗi trạng thái (Loading, Content, Error, Empty).
- Preview không gọi `hiltViewModel()`. Dữ liệu giả để ở cuối file hoặc trong `PreviewData.kt`.
- Màn hình chính nên có thêm `@PreviewScreenSizes` và `@PreviewFontScale`.

## 4. Hằng số

Kotlin cho phép khai báo `const val` ở **top-level** file. Cách này hợp lệ và được khuyến khích (không cần bọc trong `object`). Điều cần chú ý là **đặt hằng số ở đâu**, chứ không phải cú pháp khai báo:

| Loại hằng số | Đặt ở đâu |
|---|---|
| Chỉ một file dùng | `private const val` ở đầu file đó |
| Key DataStore | `private` trong data source sở hữu nó (`UserPreferencesDataSource`) |
| Cấu hình mạng (timeout, header name) | `core/network/NetworkConstants.kt` |
| Route điều hướng | Không dùng hằng số. Dùng `@Serializable XxxDestination` |
| Giá trị cấu hình toàn app thật sự dùng chung | `core/common/AppConstants.kt` |

Không gom mọi hằng số vào một file `Constants.kt` chung. File đó sẽ phình to và ai cũng import được key của người khác, như key token chẳng hạn. Nhóm theo nơi sở hữu thì dễ tìm hơn và hạn chế được phạm vi truy cập.

## 5. String resources

### 5.1 Quy tắc đặt key

```text
<scope>_<element>_<description>
```

| Phần | Giá trị | Ghi chú |
|---|---|---|
| `scope` | tên feature (`splash`, `onboarding`, `auth`, `home`, ...) hoặc `common` | Đứng đầu để các key cùng màn hình nằm cạnh nhau khi sắp xếp |
| `element` | một giá trị trong bảng bên dưới | Cho biết text dùng ở đâu |
| `description` | vài từ tiếng Anh, snake_case | Mô tả **ý nghĩa**, không mô tả vị trí hay số thứ tự |

| `element` | Dùng cho | Ví dụ |
|---|---|---|
| `title` | tiêu đề màn hình, card, dialog | `auth_title_sign_in` |
| `subtitle` | dòng phụ dưới tiêu đề | `onboarding_subtitle_streak` |
| `body` | đoạn văn mô tả | `onboarding_body_streak` |
| `label` | nhãn tab, chip, field | `home_label_tab_practice` |
| `action` | chữ trên nút hoặc link | `common_action_retry` |
| `hint` | placeholder trong ô nhập | `auth_hint_email` |
| `error` | thông báo lỗi | `auth_error_password_too_short` |
| `msg` | thông báo, snackbar, toast | `auth_msg_verification_sent` |
| `cd` | `contentDescription` cho TalkBack | `common_cd_back` |

Ví dụ áp dụng cho các key hiện có:

| Key hiện tại | Key mới |
|---|---|
| `btn_skip` | `common_action_skip` |
| `btn_continue` / `btn_start` | `common_action_next` / `onboarding_action_get_started` |
| `btn_register_new` | `onboarding_action_sign_up` |
| `btn_login_existing` | `onboarding_action_sign_in` |
| `btn_guest_explore` | `onboarding_action_continue_as_guest` |
| `onboarding_title_1` | `onboarding_title_personal_journey` |
| `onboarding_tag_1_1` | `onboarding_label_streak` |
| `txt_greeting` | `onboarding_title_ready` |

### 5.2 Tổ chức file

Android tự gộp mọi file trong `values/`, nên có thể tách file theo feature cho dễ tìm:

```text
res/values/strings.xml              # app_name và các key common_*
res/values/strings_onboarding.xml
res/values/strings_auth.xml
res/values-en/strings*.xml          # bản dịch, cùng tên file
```

### 5.3 Quy tắc nội dung

- Không có text hiển thị nào được hardcode trong Kotlin, kể cả `contentDescription`.
- Số đếm dùng `<plurals>`. Chuỗi có tham số dùng placeholder có vị trí, ví dụ `%1$d`.
- Text cần in đậm hoặc tô màu một phần thì dùng HTML đơn giản (`<b>`) và đọc bằng `AnnotatedString.fromHtml(...)`.
- Chuỗi không dịch (tên thương hiệu) khai báo `translatable="false"`.
- Ngôn ngữ mặc định (`values/`) là **tiếng Việt**. Bản tiếng Anh nằm ở `values-en/`.

## 6. Resource khác

| Loại | Tiền tố | Ví dụ |
|---|---|---|
| Icon (vector) | `ic_` | `ic_bell.xml` |
| Ảnh minh hoạ, nền | `img_` | `img_splash_background.webp` |
| Mascot Oysteic | `img_mascot_` | `img_mascot_hello.webp` (hiện tại đang là `oy2_hello.webp`) |
| Shape, selector XML | `bg_` | `bg_card_rounded.xml` |
| Font | `<family>_<weight>` | `be_vietnam_pro_medium.ttf` |

Màu và cỡ chữ **không** khai báo trong `res/values/colors.xml` hay `dimens.xml`. Chúng thuộc theme Compose.
