# Android Development Guideline — TOEICSpace Mobile

## 1. Giới thiệu

Tài liệu này quy định cách tổ chức code, quy trình làm việc và convention áp dụng cho `toeic-space-mobile`. Mọi thành viên tham gia phát triển (kể cả người chưa quen mobile) cần đọc và tuân theo tài liệu này trước khi đóng góp code.

**Công nghệ sử dụng:** Kotlin, Jetpack Compose, Clean Architecture + MVVM, Hilt, Retrofit, Coroutines/Flow.

## 2. Kiến trúc dự án

Dự án áp dụng **Clean Architecture** kết hợp **MVVM**, tổ chức theo **package-by-feature**.

### Ba lớp (layer) trong mỗi feature

- `data/` — Lấy dữ liệu thật (API, local DB)
- `domain/` — Business logic thuần Kotlin, không phụ thuộc Android
- `presentation/` — UI (Compose) + ViewModel

| Layer | Vai trò | Được phép biết layer nào |
|---|---|---|
| `presentation` | Hiển thị UI, nhận thao tác người dùng, gọi UseCase | `domain` |
| `domain` | Chứa logic nghiệp vụ (UseCase), Model, Repository interface | Không biết `data` hay `presentation` |
| `data` | Gọi API/DB thật, implement Repository interface, map DTO → Domain Model | `domain` |

**Quy tắc bắt buộc (Dependency Rule):** `presentation` phụ thuộc `domain`, `data` cũng phụ thuộc `domain` (để implement interface) — nhưng `domain` không được import bất cứ thứ gì từ `data` hoặc `presentation`. Đây là nguyên tắc cốt lõi giúp test logic nghiệp vụ dễ dàng mà không cần chạy Android, và đổi API/UI không ảnh hưởng logic tính điểm/chấm bài.

## 3. Cấu trúc thư mục

### 3.1 Cấu trúc repo (root level)

```
toeic-space-mobile/
├── app/                          # Android project (xem chi tiết ở Mục 3.2)
├── docs/
│   ├── api-integration.md        # Nhóm API, response format, xử lý lỗi mạng
│   ├── screens-flow.md           # Luồng màn hình, phân quyền theo role
│   └── release.md                # Hướng dẫn build & phân phối APK
├── .editorconfig
├── .gitignore
├── GUIDELINE_ANDROID.md
├── LICENSE
└── README.md
```

### 3.2 Cấu trúc package (`app/src/main/java/com/toeicspace/android/`)

```
com.toeicspace.android/
├── core/
│   ├── database/
│   ├── datastore/
│   ├── di/
│   ├── navigation/
│   ├── network/
│   ├── ui/
│   │   ├── components/
│   │   └── theme/
│   └── util/
│       └── Constants.kt
├── feature/
│   ├── auth/
│   │   ├── data/
│   │   │   ├── local/
│   │   │   ├── remote/
│   │   │   └── repository/
│   │   ├── domain/
│   │   │   ├── model/
│   │   │   ├── repository/
│   │   │   └── usecase/
│   │   └── presentation/
│   │       ├── login/
│   │       ├── register/
│   │       └── onboarding/
│   ├── home/
│   ├── explore/
│   ├── course/
│   ├── vocabulary/
│   ├── practice/
│   ├── test/
│   ├── result/
│   ├── mistake/
│   ├── roadmap/
│   ├── assignment/
│   ├── schedule/
│   ├── notification/
│   └── profile/
├── ToeicSpaceApp.kt
└── MainActivity.kt
```

`auth` được để làm ví dụ minh hoạ đầy đủ 3 layer (`data/domain/presentation`) theo đúng quy tắc ở Mục 2.1. Các feature còn lại (`home`, `explore`, `course`...) áp dụng cùng cấu trúc 3 layer này khi triển khai — không liệt kê lặp lại ở đây để bảng gọn, nhưng bắt buộc tuân theo cùng khuôn.

Feature nào chưa cần `data` (ví dụ chỉ là màn hình tĩnh, không gọi API/DB) thì có thể bỏ layer đó, không bắt buộc tạo folder rỗng.

## 4. Git Workflow

### 4.1 Branch

- `main` — code ổn định, chỉ merge từ `develop` khi release.
- `develop` — nhánh tích hợp chính.
- Feature branch: `tên-dev/feature-name`, ví dụ `nganntb/login-screen`, `nganntb/fix-timer-bug`.

### 4.2 Commit Message (Conventional Commits)

Format: `<type>: <mô tả ngắn>`

| Type | Dùng khi |
|---|---|
| `feat` | Thêm tính năng mới |
| `fix` | Sửa lỗi |
| `refactor` | Tái cấu trúc code, không đổi behavior |
| `docs` | Sửa tài liệu |
| `chore` | Việc lặt vặt (update dependency, format code) |
| `test` | Thêm/sửa test |

Ví dụ: `feat: add login screen with email validation`, `fix: crash when submit mock test offline`.

### 4.3 Pull Request

1. Push branch, tạo PR vào `develop`.
2. Gắn label `review_request`.
3. Reviewer comment nếu cần sửa → đổi label `changes_request`.
4. Dev sửa xong, commit tiếp, đổi lại `review_request`.
5. Merge khi được approve.

### 4.4 PR Checklist

- [ ] Build thành công (`./gradlew build`)
- [ ] `./gradlew ktlintCheck` không lỗi
- [ ] Không còn `TODO` chưa xử lý, không còn `println()`/`Log.d` debug thừa
- [ ] Đã test trên ít nhất 1 emulator/device thật
- [ ] Compose Preview hiển thị đúng (xem Mục 8)

## 5. Coding Convention

### 5.1 Naming chung

| Đối tượng | Quy tắc | Ví dụ |
|---|---|---|
| Class, Interface | PascalCase | `LoginViewModel`, `AuthRepository` |
| Function, Property | camelCase | `getUserProfile()`, `isLoading` |
| Constant | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT` |
| Package | lowercase, không gạch dưới | `com.toeicspace.android.feature.auth` |
| File resource | lowercase_underscore | `ic_star.png`, `error_invalid_email` |

### 5.2 Composable Naming

| Loại | Quy tắc | Ví dụ |
|---|---|---|
| Màn hình đầy đủ | Hậu tố `Screen` | `LoginScreen`, `CourseDetailScreen` |
| Thành phần tái sử dụng | Tên mô tả rõ vai trò | `PrimaryButton`, `CourseCard` |

Mỗi Composable chỉ nên nhận data qua parameter, không tự gọi ViewModel/Repository bên trong (trừ Screen-level Composable — xem Mục 5.3).

### 5.3 ViewModel & UI State

Mỗi màn hình có 1 `data class XxxUiState` (immutable):

```kotlin
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
```

ViewModel expose state qua `StateFlow`, không dùng `LiveData`:

```kotlin
private val _uiState = MutableStateFlow(LoginUiState())
val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

fun onEmailChanged(value: String) {
    _uiState.update { it.copy(email = value) }
}
```

Chỉ Screen-level Composable được inject ViewModel qua `hiltViewModel()`; Composable con chỉ nhận state + callback qua parameter (state hoisting).

### 5.4 Coroutine & Flow

- Luôn chạy coroutine trong `viewModelScope`, không dùng `GlobalScope`.
- Dispatcher inject qua Hilt (`@IoDispatcher`), không hardcode `Dispatchers.IO` trực tiếp, để dễ viết Unit Test.
- Luôn xử lý lỗi Flow bằng `.catch { }`, không để exception làm crash app.

### 5.5 Networking & Data Layer

- Interface Retrofit hậu tố `ApiService`: `CourseApiService`.
- DTO hậu tố rõ vai trò: `LoginRequest`, `LoginResponse`, `CourseDto`.
- Bắt buộc có Mapper chuyển DTO → Domain Model.
- Kết quả trả về dùng chung 1 wrapper:

```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
    object Loading : Result<Nothing>()
}
```

### 5.6 Dependency Injection (Hilt)

- Module hậu tố `Module`: `NetworkModule`, `RepositoryModule`.
- Đối tượng dùng chung toàn app (Retrofit, Room) scope `@Singleton`.
- Repository interface `@Binds` trong `RepositoryModule`, implement đặt ở `data/repository/`.

### 5.7 Error Handling

Định nghĩa 1 kiểu lỗi chun# Android Development Guideline — TOEICSpace Mobile

## 1. Giới thiệu

Tài liệu này quy định cách tổ chức code, quy trình làm việc và convention áp dụng cho `toeic-space-mobile`. Mọi thành viên tham gia phát triển (kể cả người chưa quen mobile) cần đọc và tuân theo tài liệu này trước khi đóng góp code.

**Công nghệ sử dụng:** Kotlin, Jetpack Compose, Clean Architecture + MVVM, Hilt, Retrofit, Coroutines/Flow.

## 2. Kiến trúc dự án

Dự án áp dụng **Clean Architecture** kết hợp **MVVM**, tổ chức theo **package-by-feature**.

### 2.1 Ba lớp (layer) trong mỗi feature

- `data/` — Lấy dữ liệu thật (API, local DB)
- `domain/` — Business logic thuần Kotlin, không phụ thuộc Android
- `presentation/` — UI (Compose) + ViewModel

| Layer | Vai trò | Được phép biết layer nào |
|---|---|---|
| `presentation` | Hiển thị UI, nhận thao tác người dùng, gọi UseCase | `domain` |
| `domain` | Chứa logic nghiệp vụ (UseCase), Model, Repository interface | Không biết `data` hay `presentation` |
| `data` | Gọi API/DB thật, implement Repository interface, map DTO → Domain Model | `domain` |

**Quy tắc bắt buộc (Dependency Rule):** `presentation` phụ thuộc `domain`, `data` cũng phụ thuộc `domain` (để implement interface) — nhưng `domain` không được import bất cứ thứ gì từ `data` hoặc `presentation`. Đây là nguyên tắc cốt lõi giúp test logic nghiệp vụ dễ dàng mà không cần chạy Android, và đổi API/UI không ảnh hưởng logic tính điểm/chấm bài.

### 2.2 Vì sao không tách Gradle module

Dự án dùng single-module (`app/`) vì team mobile hiện chỉ 1 người, quy mô đồ án chưa cần lợi ích build-song-song của multi-module. Ranh giới rõ ràng vẫn được giữ nhờ tổ chức package nghiêm ngặt — nếu sau này cần tách module thật, việc convert sẽ dễ vì code đã có ranh giới sẵn.

## 3. Cấu trúc thư mục

### 3.1 Cấu trúc repo (root level)

```
toeic-space-mobile/
├── app/                          # Android project (xem chi tiết ở Mục 3.2)
├── docs/
│   ├── api-integration.md        # Nhóm API, response format, xử lý lỗi mạng
│   ├── screens-flow.md           # Luồng màn hình, phân quyền theo role
│   └── release.md                # Hướng dẫn build & phân phối APK
├── .editorconfig
├── .gitignore
├── GUIDELINE_ANDROID.md
├── LICENSE
└── README.md
```

### 3.2 Cấu trúc package (`app/src/main/java/com/toeicspace/android/`)

```
com.toeicspace.android/
├── core/
│   ├── database/
│   ├── datastore/
│   ├── di/
│   ├── navigation/
│   ├── network/
│   ├── ui/
│   │   ├── components/
│   │   └── theme/
│   └── util/
│       └── Constants.kt
├── feature/
│   ├── auth/
│   │   ├── data/
│   │   │   ├── local/
│   │   │   ├── remote/
│   │   │   └── repository/
│   │   ├── domain/
│   │   │   ├── model/
│   │   │   ├── repository/
│   │   │   └── usecase/
│   │   └── presentation/
│   │       ├── login/
│   │       ├── register/
│   │       └── onboarding/
│   ├── home/
│   ├── explore/
│   ├── course/
│   ├── vocabulary/
│   ├── practice/
│   ├── test/
│   ├── result/
│   ├── mistake/
│   ├── roadmap/
│   ├── assignment/
│   ├── schedule/
│   ├── notification/
│   └── profile/
├── ToeicSpaceApp.kt
└── MainActivity.kt
```

`auth` được để làm ví dụ minh hoạ đầy đủ 3 layer (`data/domain/presentation`) theo đúng quy tắc ở Mục 2.1. Các feature còn lại (`home`, `explore`, `course`...) áp dụng cùng cấu trúc 3 layer này khi triển khai — không liệt kê lặp lại ở đây để bảng gọn, nhưng bắt buộc tuân theo cùng khuôn.

Feature nào chưa cần `data` (ví dụ chỉ là màn hình tĩnh, không gọi API/DB) thì có thể bỏ layer đó, không bắt buộc tạo folder rỗng.

## 4. Git Workflow

### 4.1 Branch

- `main` — code ổn định, chỉ merge từ `develop` khi release.
- `develop` — nhánh tích hợp chính.
- Feature branch: `tên-dev/feature-name`, ví dụ `giang/login-screen`, `giang/fix-timer-bug`.

### 4.2 Commit Message (Conventional Commits)

Format: `<type>: <mô tả ngắn>`

| Type | Dùng khi |
|---|---|
| `feat` | Thêm tính năng mới |
| `fix` | Sửa lỗi |
| `refactor` | Tái cấu trúc code, không đổi behavior |
| `docs` | Sửa tài liệu |
| `chore` | Việc lặt vặt (update dependency, format code) |
| `test` | Thêm/sửa test |

Ví dụ: `feat: add login screen with email validation`, `fix: crash when submit mock test offline`.

### 4.3 Pull Request

1. Push branch, tạo PR vào `develop`.
2. Gắn label `review_request`.
3. Reviewer comment nếu cần sửa → đổi label `changes_request`.
4. Dev sửa xong, commit tiếp, đổi lại `review_request`.
5. Merge khi được approve.

### 4.4 PR Checklist

- [ ] Build thành công (`./gradlew build`)
- [ ] `./gradlew ktlintCheck` không lỗi
- [ ] Không còn `TODO` chưa xử lý, không còn `println()`/`Log.d` debug thừa
- [ ] Đã test trên ít nhất 1 emulator/device thật
- [ ] Compose Preview hiển thị đúng (xem Mục 8)

## 5. Coding Convention

### 5.1 Naming chung

| Đối tượng | Quy tắc | Ví dụ |
|---|---|---|
| Class, Interface | PascalCase | `LoginViewModel`, `AuthRepository` |
| Function, Property | camelCase | `getUserProfile()`, `isLoading` |
| Constant | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT` |
| Package | lowercase, không gạch dưới | `com.toeicspace.android.feature.auth` |
| File resource | lowercase_underscore | `ic_star.png`, `error_invalid_email` |

### 5.2 Composable Naming

| Loại | Quy tắc | Ví dụ |
|---|---|---|
| Màn hình đầy đủ | Hậu tố `Screen` | `LoginScreen`, `CourseDetailScreen` |
| Thành phần tái sử dụng | Tên mô tả rõ vai trò | `PrimaryButton`, `CourseCard` |

Mỗi Composable chỉ nên nhận data qua parameter, không tự gọi ViewModel/Repository bên trong (trừ Screen-level Composable — xem Mục 5.3).

### 5.3 ViewModel & UI State

Mỗi màn hình có 1 `data class XxxUiState` (immutable):

```kotlin
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
```

ViewModel expose state qua `StateFlow`, không dùng `LiveData`:

```kotlin
private val _uiState = MutableStateFlow(LoginUiState())
val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

fun onEmailChanged(value: String) {
    _uiState.update { it.copy(email = value) }
}
```

Chỉ Screen-level Composable được inject ViewModel qua `hiltViewModel()`; Composable con chỉ nhận state + callback qua parameter (state hoisting).

### 5.4 Coroutine & Flow

- Luôn chạy coroutine trong `viewModelScope`, không dùng `GlobalScope`.
- Dispatcher inject qua Hilt (`@IoDispatcher`), không hardcode `Dispatchers.IO` trực tiếp, để dễ viết Unit Test.
- Luôn xử lý lỗi Flow bằng `.catch { }`, không để exception làm crash app.

### 5.5 Networking & Data Layer

- Interface Retrofit hậu tố `ApiService`: `CourseApiService`.
- DTO hậu tố rõ vai trò: `LoginRequest`, `LoginResponse`, `CourseDto`.
- Bắt buộc có Mapper chuyển DTO → Domain Model.
- Kết quả trả về dùng chung 1 wrapper:

```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
    object Loading : Result<Nothing>()
}
```

### 5.6 Dependency Injection (Hilt)

- Module hậu tố `Module`: `NetworkModule`, `RepositoryModule`.
- Đối tượng dùng chung toàn app (Retrofit, Room) scope `@Singleton`.
- Repository interface `@Binds` trong `RepositoryModule`, implement đặt ở `data/repository/`.

### 5.7 Error Handling

Định nghĩa 1 kiểu lỗi chung (`AppError`) để mọi feature xử lý lỗi nhất quán, tránh mỗi màn hình tự hiển thị lỗi theo 1 kiểu khác nhau.

### 5.8 Line length & Line-wrapping

Giới hạn 100 ký tự/dòng. Vượt giới hạn: ưu tiên extract biến/hàm; nếu wrap, ngắt trước dấu `.` khi chain nhiều method. Ngoại lệ: `import`, `package`, URL trong comment.

### 5.9 Constants.kt

Toàn bộ hằng số dùng chung của app (key cho `SharedPreferences`/`DataStore`, tên route Navigation, giá trị cấu hình cố định như timeout, page size...) được khai báo **trực tiếp bằng `const val` trong một file `Constants.kt` duy nhất** ở `core/util/`, **không bọc trong `object`**:

```kotlin
// core/util/Constants.kt

const val PREF_TOKEN = "PREF_TOKEN"
const val PREF_USER_ID = "PREF_USER_ID"

const val NETWORK_TIMEOUT_SECONDS = 30L
const val DEFAULT_PAGE_SIZE = 20

const val ROUTE_LOGIN = "login"
const val ROUTE_HOME = "home"
```

Gọi trực tiếp qua import, không cần tiền tố `object`:

```kotlin
import com.toeicspace.android.core.util.PREF_TOKEN
import com.toeicspace.android.core.util.NETWORK_TIMEOUT_SECONDS

dataStore.edit { it[stringPreferencesKey(PREF_TOKEN)] = token }
```

Mỗi hằng số phải có prefix rõ loại để tránh trùng lặp/nhầm lẫn khi file dài dần theo thời gian:

| Prefix | Dùng cho |
|---|---|
| `PREF_` | Key lưu trong DataStore/SharedPreferences |
| `ROUTE_` | Tên route trong Navigation Compose |
| `ARGUMENT_` | Tên argument truyền qua Navigation route |
| `API_` | Hằng số liên quan cấu hình network (path, header name...) |
| Không prefix, viết rõ nghĩa | Hằng số cấu hình chung (`NETWORK_TIMEOUT_SECONDS`, `DEFAULT_PAGE_SIZE`) |

## 6. Resource Naming

### 6.1 Drawable

| Loại | Prefix | Ví dụ |
|---|---|---|
| Icon | `ic_` | `ic_star.png` |
| Button | `btn_` | `btn_send_pressed.png` |
| Background | `bg_` | `bg_card_rounded.png` |

### 6.2 Strings

| Prefix | Dùng khi |
|---|---|
| `error_` | Thông báo lỗi |
| `msg_` | Thông báo thông thường |
| `title_` | Tiêu đề màn hình/dialog |
| `action_` | Nhãn hành động |

### 6.3 Color & Typography

Định nghĩa tập trung trong `core/ui/theme/`. Không hardcode màu/font trực tiếp trong Composable, luôn dùng `MaterialTheme.colorScheme` / `MaterialTheme.typography`.

## 7. Testing Guideline

Unit test cho `domain` (UseCase) là bắt buộc. Đặt tên test rõ ràng:

```kotlin
@Test
fun `login should return error when email is invalid`() { ... }
```

UI test Compose dùng `createComposeRule()`, ưu tiên test luồng quan trọng (login, submit test) hơn toàn bộ UI.

## 8. Compose Preview

Preview giúp xem giao diện ngay trong Android Studio mà **không cần build và chạy app trên emulator** — bắt buộc dùng cho mọi Composable UI để tăng tốc độ phát triển và để reviewer xem trước giao diện khi review PR.

### 8.1 Quy tắc bắt buộc

- Mọi Composable màn hình (`XxxScreen`) và component tái sử dụng (`XxxCard`, `XxxButton`...) phải có ít nhất 1 hàm Preview đi kèm.
- Preview đặt cùng file, cuối file, hậu tố `Preview`: `LoginScreenPreview`, `CourseCardPreview`.
- Preview function luôn đánh dấu `private`, annotate `@Preview` + `@Composable`.

### 8.2 Cấu trúc chuẩn 1 Preview

```kotlin
@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    ToeicSpaceTheme {
        LoginScreen(
            uiState = LoginUiState(email = "user@example.com"),
            onEmailChanged = {},
            onPasswordChanged = {},
            onLoginClicked = {}
        )
    }
}
```

**Lưu ý quan trọng:** Preview không được gọi `hiltViewModel()` hay bất kỳ ViewModel/Repository thật nào (vì Preview chạy trong môi trường giả lập, không có Hilt Container, không có network). Vì vậy Composable Screen nên tách thành 2 phần theo pattern **Route/Screen**:

```kotlin
// Bản "thật" — được gọi từ NavGraph, có ViewModel
@Composable
fun LoginRoute(viewModel: LoginViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    LoginScreen(
        uiState = uiState,
        onEmailChanged = viewModel::onEmailChanged,
        onPasswordChanged = viewModel::onPasswordChanged,
        onLoginClicked = viewModel::onLoginClicked
    )
}

// Bản "stateless" — chỉ nhận state + callback, dùng được cho cả Preview lẫn UI test
@Composable
fun LoginScreen(
    uiState: LoginUiState,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onLoginClicked: () -> Unit
) { ... }
```

### 8.3 Preview cho nhiều trạng thái

Với những màn hình có nhiều trạng thái quan trọng (Loading, Success, Error, Empty), nên tạo nhiều Preview riêng để dễ kiểm tra từng case:

```kotlin
@Preview(name = "Loading", showBackground = true)
@Composable
private fun CourseListScreenLoadingPreview() {
    ToeicSpaceTheme {
        CourseListScreen(uiState = CourseListUiState(isLoading = true))
    }
}

@Preview(name = "Empty", showBackground = true)
@Composable
private fun CourseListScreenEmptyPreview() {
    ToeicSpaceTheme {
        CourseListScreen(uiState = CourseListUiState(courses = emptyList()))
    }
}

@Preview(name = "Success", showBackground = true)
@Composable
private fun CourseListScreenSuccessPreview() {
    ToeicSpaceTheme {
        CourseListScreen(uiState = CourseListUiState(courses = fakeCourseList))
    }
}
```

### 8.4 Preview cho Dark Mode & nhiều kích thước màn hình (khuyến khích, không bắt buộc)

```kotlin
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LoginScreenThemePreview() {
    ToeicSpaceTheme {
        LoginScreen(
            uiState = LoginUiState(),
            onEmailChanged = {},
            onPasswordChanged = {},
            onLoginClicked = {}
        )
    }
}
```

Android Studio hỗ trợ nhiều `@Preview` annotation chồng lên cùng 1 hàm (Multipreview) để xem đồng thời nhiều biến thể mà không cần viết nhiều hàm riêng.

### 8.5 Dữ liệu giả cho Preview

Với dữ liệu phức tạp (list Course, list Question...), tạo sẵn `fake data` dùng riêng cho Preview trong cùng file hoặc file `PreviewData.kt` trong `core/ui/`:

```kotlin
internal val fakeCourseList = listOf(
    Course(id = "1", title = "TOEIC Foundation", progress = 0.4f),
    Course(id = "2", title = "TOEIC Advanced", progress = 0.8f)
)
```

Không dùng data thật/gọi API trong Preview — Preview phải render được **ngay lập tức, offline, không phụ thuộc trạng thái máy chủ**.

## 9. Security Checklist

- Không hardcode API key/base URL trong code — khai báo trong `local.properties`, expose qua `BuildConfig`.
- Không commit: `google-services.json` thật, file keystore (`.jks`/`.keystore`), token thật trong code hay test.
- File `.gitignore` (template Android mặc định của GitHub) đã cover các mục trên — kiểm tra lại trước khi commit lần đầu nếu thêm file nhạy cảm mới.

## 10. FAQ cho người mới / non-mobile dev

**Q: Tôi muốn thêm 1 màn hình mới thì bắt đầu từ đâu?**
Xác định feature nó thuộc về (ví dụ màn hình mới thuộc `course`) → tạo Composable trong `feature/course/presentation/` → tạo ViewModel + UiState tương ứng → thêm route vào `core/navigation/NavGraph.kt`.

**Q: Tại sao không thấy code gọi API trực tiếp trong màn hình?**
Vì theo Clean Architecture, UI (`presentation`) không gọi API trực tiếp, mà gọi qua UseCase (`domain`) → UseCase gọi Repository (`domain` interface, `data` implement) → Repository mới thật sự gọi Retrofit.

**Q: Tôi sửa giao diện có cần chạy app trên điện thoại không?**
Không cần với thay đổi UI đơn giản — dùng Compose Preview (Mục 8) để xem ngay trong Android Studio, chỉ cần chạy app thật khi cần test luồng tương tác/network thật.g (`AppError`) để mọi feature xử lý lỗi nhất quán, tránh mỗi màn hình tự hiển thị lỗi theo 1 kiểu khác nhau.

### 5.8 Line length & Line-wrapping

Giới hạn 100 ký tự/dòng. Vượt giới hạn: ưu tiên extract biến/hàm; nếu wrap, ngắt trước dấu `.` khi chain nhiều method. Ngoại lệ: `import`, `package`, URL trong comment.

## 6. Resource Naming

### 6.1 Drawable

| Loại | Prefix | Ví dụ |
|---|---|---|
| Icon | `ic_` | `ic_star.png` |
| Button | `btn_` | `btn_send_pressed.png` |
| Background | `bg_` | `bg_card_rounded.png` |

### 6.2 Strings

| Prefix | Dùng khi |
|---|---|
| `error_` | Thông báo lỗi |
| `msg_` | Thông báo thông thường |
| `title_` | Tiêu đề màn hình/dialog |
| `action_` | Nhãn hành động |

### 6.3 Color & Typography

Định nghĩa tập trung trong `core/ui/theme/`. Không hardcode màu/font trực tiếp trong Composable, luôn dùng `MaterialTheme.colorScheme` / `MaterialTheme.typography`.

## 7. Testing Guideline

Unit test cho `domain` (UseCase) là bắt buộc. Đặt tên test rõ ràng:

```kotlin
@Test
fun `login should return error when email is invalid`() { ... }
```

UI test Compose dùng `createComposeRule()`, ưu tiên test luồng quan trọng (login, submit test) hơn toàn bộ UI.

## 8. Compose Preview

Preview giúp xem giao diện ngay trong Android Studio mà **không cần build và chạy app trên emulator** — bắt buộc dùng cho mọi Composable UI để tăng tốc độ phát triển và để reviewer xem trước giao diện khi review PR.

### 8.1 Quy tắc bắt buộc

- Mọi Composable màn hình (`XxxScreen`) và component tái sử dụng (`XxxCard`, `XxxButton`...) phải có ít nhất 1 hàm Preview đi kèm.
- Preview đặt cùng file, cuối file, hậu tố `Preview`: `LoginScreenPreview`, `CourseCardPreview`.
- Preview function luôn đánh dấu `private`, annotate `@Preview` + `@Composable`.

### 8.2 Cấu trúc chuẩn 1 Preview

```kotlin
@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    ToeicSpaceTheme {
        LoginScreen(
            uiState = LoginUiState(email = "user@example.com"),
            onEmailChanged = {},
            onPasswordChanged = {},
            onLoginClicked = {}
        )
    }
}
```

**Lưu ý quan trọng:** Preview không được gọi `hiltViewModel()` hay bất kỳ ViewModel/Repository thật nào (vì Preview chạy trong môi trường giả lập, không có Hilt Container, không có network). Vì vậy Composable Screen nên tách thành 2 phần theo pattern **Route/Screen**:

```kotlin
// Bản "thật" — được gọi từ NavGraph, có ViewModel
@Composable
fun LoginRoute(viewModel: LoginViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    LoginScreen(
        uiState = uiState,
        onEmailChanged = viewModel::onEmailChanged,
        onPasswordChanged = viewModel::onPasswordChanged,
        onLoginClicked = viewModel::onLoginClicked
    )
}

// Bản "stateless" — chỉ nhận state + callback, dùng được cho cả Preview lẫn UI test
@Composable
fun LoginScreen(
    uiState: LoginUiState,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onLoginClicked: () -> Unit
) { ... }
```

### 8.3 Preview cho nhiều trạng thái

Với những màn hình có nhiều trạng thái quan trọng (Loading, Success, Error, Empty), nên tạo nhiều Preview riêng để dễ kiểm tra từng case:

```kotlin
@Preview(name = "Loading", showBackground = true)
@Composable
private fun CourseListScreenLoadingPreview() {
    ToeicSpaceTheme {
        CourseListScreen(uiState = CourseListUiState(isLoading = true))
    }
}

@Preview(name = "Empty", showBackground = true)
@Composable
private fun CourseListScreenEmptyPreview() {
    ToeicSpaceTheme {
        CourseListScreen(uiState = CourseListUiState(courses = emptyList()))
    }
}

@Preview(name = "Success", showBackground = true)
@Composable
private fun CourseListScreenSuccessPreview() {
    ToeicSpaceTheme {
        CourseListScreen(uiState = CourseListUiState(courses = fakeCourseList))
    }
}
```

### 8.4 Preview cho Dark Mode & nhiều kích thước màn hình (khuyến khích, không bắt buộc)

```kotlin
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LoginScreenThemePreview() {
    ToeicSpaceTheme {
        LoginScreen(
            uiState = LoginUiState(),
            onEmailChanged = {},
            onPasswordChanged = {},
            onLoginClicked = {}
        )
    }
}
```

Android Studio hỗ trợ nhiều `@Preview` annotation chồng lên cùng 1 hàm (Multipreview) để xem đồng thời nhiều biến thể mà không cần viết nhiều hàm riêng.

### 8.5 Dữ liệu giả cho Preview

Với dữ liệu phức tạp (list Course, list Question...), tạo sẵn `fake data` dùng riêng cho Preview trong cùng file hoặc file `PreviewData.kt` trong `core/ui/`:

```kotlin
internal val fakeCourseList = listOf(
    Course(id = "1", title = "TOEIC Foundation", progress = 0.4f),
    Course(id = "2", title = "TOEIC Advanced", progress = 0.8f)
)
```

Không dùng data thật/gọi API trong Preview — Preview phải render được **ngay lập tức, offline, không phụ thuộc trạng thái máy chủ**.

## 9. Security Checklist

- Không hardcode API key/base URL trong code — khai báo trong `local.properties`, expose qua `BuildConfig`.
- Không commit: `google-services.json` thật, file keystore (`.jks`/`.keystore`), token thật trong code hay test.
- File `.gitignore` (template Android mặc định của GitHub) đã cover các mục trên — kiểm tra lại trước khi commit lần đầu nếu thêm file nhạy cảm mới.

## 10. FAQ cho người mới / non-mobile dev

**Q: Tôi muốn thêm 1 màn hình mới thì bắt đầu từ đâu?**
Xác định feature nó thuộc về (ví dụ màn hình mới thuộc `course`) → tạo Composable trong `feature/course/presentation/` → tạo ViewModel + UiState tương ứng → thêm route vào `core/navigation/NavGraph.kt`.

**Q: Tại sao không thấy code gọi API trực tiếp trong màn hình?**
Vì theo Clean Architecture, UI (`presentation`) không gọi API trực tiếp, mà gọi qua UseCase (`domain`) → UseCase gọi Repository (`domain` interface, `data` implement) → Repository mới thật sự gọi Retrofit.

**Q: Tôi sửa giao diện có cần chạy app trên điện thoại không?**
Không cần với thay đổi UI đơn giản — dùng Compose Preview (Mục 8) để xem ngay trong Android Studio, chỉ cần chạy app thật khi cần test luồng tương tác/network thật.
