# Kiến trúc

> Tài liệu mô tả kiến trúc **đích** của app. Phần nào code chưa theo kịp được đánh dấu **(chưa áp dụng)**. Khi refactor xong, xoá dấu đó.

## 1. Tổng quan

- **Pattern:** MVVM + Clean Architecture theo [Guide to app architecture](https://developer.android.com/topic/architecture) của Google.
- **Tổ chức code:** package-by-feature trong **một Gradle module** `:app`. Lý do và điều kiện tách module ở [ADR-0001](adr/0001-single-gradle-module.md).
- **Luồng dữ liệu một chiều (UDF):** state đi xuống (`StateFlow` → Composable), event đi lên (callback → ViewModel).
- **Single Source of Truth (SSOT):** mỗi loại dữ liệu có đúng một nơi sở hữu (Repository). Mọi màn hình chỉ quan sát nơi đó.

```text
┌──────────── presentation ────────────┐
│  XxxRoute ──▶ XxxScreen (stateless)  │
│     │  collectAsStateWithLifecycle   │
│     ▼                                │
│  XxxViewModel ── StateFlow<UiState>  │
└─────┬────────────────────────────────┘
      │ gọi
┌─────▼──────────── domain ────────────┐   Kotlin thuần, không import android.*
│  UseCase (khi cần) · Model           │
│  Repository interface                │
└─────▲────────────────────────────────┘
      │ implement
┌─────┴──────────── data ──────────────┐
│  RepositoryImpl                      │
│  RemoteDataSource (Retrofit ApiService, DTO, Mapper)
│  LocalDataSource (DataStore, Room)   │
└──────────────────────────────────────┘
```

## 2. Các lớp

| Lớp | Chứa | Được phụ thuộc vào |
|---|---|---|
| `presentation` | `XxxRoute`, `XxxScreen`, `XxxViewModel`, `XxxUiState`, component riêng của feature | `domain`, `core.designsystem` |
| `domain` | Model, `XxxRepository` (interface), `XxxUseCase` | Không phụ thuộc lớp nào khác |
| `data` | `XxxRepositoryImpl`, `XxxApiService`, `XxxDto`, mapper, local data source | `domain`, `core.network`, `core.datastore` |

Quy tắc:

1. ViewModel **không** biết Retrofit, DataStore hay Room. ViewModel chỉ gọi Repository hoặc UseCase.
2. **UseCase là tùy chọn.** Chỉ tạo UseCase khi logic được dùng lại ở nhiều nơi hoặc đủ phức tạp (tính điểm TOEIC, quyết định màn hình bắt đầu). Không tạo UseCase chỉ để chuyển tiếp lời gọi tới Repository.
3. DTO (`@Serializable`) chỉ nằm trong `data/remote`. UI không bao giờ nhận DTO.
4. Một feature chưa gọi API hay DB thì được bỏ lớp `data` và `domain`.

## 3. Cấu trúc package

```text
com.toeicspace.android/
├── ToeicSpaceApplication.kt          # @HiltAndroidApp              (chưa áp dụng: đang tên ToeicSpaceApp)
├── MainActivity.kt                   # installSplashScreen + setContent
├── MainActivityViewModel.kt          # quan sát SessionRepository   (chưa áp dụng)
├── ui/
│   ├── ToeicSpaceApp.kt              # Scaffold gốc, chọn BottomBar/NavigationRail theo kích thước cửa sổ
│   └── ToeicSpaceNavHost.kt          # ghép graph của các feature
├── core/
│   ├── common/                       # AppResult, AppError, UiText, Dispatcher qualifier
│   ├── designsystem/
│   │   ├── theme/                    # Color, Type, Shape, Spacing, Dimens, Theme
│   │   └── component/                # ToeicButton, ToeicIconButton, ToeicTopBar, ...
│   ├── datastore/                    # PreferencesDataSource, TokenStorage (mã hoá)
│   ├── network/                      # Retrofit, OkHttp, AuthInterceptor, TokenAuthenticator
│   ├── data/                         # NetworkMonitor, repository dùng chung
│   └── di/                           # NetworkModule, DataStoreModule, DispatchersModule
└── feature/
    ├── splash/        presentation/
    ├── onboarding/    navigation/ presentation/
    ├── auth/          data/ domain/ navigation/ presentation/{signin,signup,verifyemail,passwordreset}/
    ├── home/          data/ domain/ navigation/ presentation/   # biến thể Guest / Registered / Enrolled
    ├── explore/       # tab Khám phá       → Assessment (tests, practice-sets)
    ├── practice/      # tab Luyện tập      → Assessment
    ├── vocabulary/    # tab Từ vựng        → Content (chưa có API)
    └── profile/       # tab Cá nhân        → Identity (users/me, avatar, change-password)
```

> **Hiện trạng:** design system đang nằm ở `core/ui/{theme,components}`. Sẽ đổi tên thành `core/designsystem/{theme,component}` ở bước refactor 2.

## 4. Single Source of Truth cho phiên đăng nhập

Trạng thái phiên quyết định toàn bộ luồng Loading → Onboarding → Auth → Home, nên chỉ có một nguồn duy nhất:

```kotlin
sealed interface SessionState {
    data object Loading : SessionState
    data class Ready(
        val isOnboardingCompleted: Boolean,
        val user: AuthUser?, // null = khách
    ) : SessionState
}

data class AuthUser(
    val id: String,
    val fullName: String,
    val email: String,
    val role: UserRole, // User (học viên) | Teacher | Admin, theo Identity service
)

interface SessionRepository {
    val session: Flow<SessionState>
    suspend fun completeOnboarding()
    suspend fun signOut()
}
```

- `MainActivityViewModel` chuyển `session` thành `StateFlow` (dùng `stateIn(WhileSubscribed(5_000))`).
- System splash được giữ trên màn hình khi state còn là `Loading`.
- Start destination của `NavHost` được tính từ `Ready`.
- Khi refresh token thất bại hoặc người dùng đăng xuất, `session` phát giá trị mới và app tự quay về Auth. Màn hình con không tự điều hướng.

## 5. ViewModel & UI state

```kotlin
@HiltViewModel
class SignInViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SignInUiState())
    val uiState: StateFlow<SignInUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value) }
}
```

- Mỗi màn hình có một `XxxUiState`: dùng `data class` cho form, `sealed interface` cho các trạng thái loại trừ nhau (Loading / Content / Error).
- Composable thu state bằng `collectAsStateWithLifecycle()`, không dùng `collectAsState()`.
- **Event một lần** (điều hướng, snackbar) được ưu tiên mô hình hoá thành state. Điều hướng do người dùng bấm thì Screen gọi thẳng callback (`onNavigateToSignUp`), không cần đi vòng qua ViewModel.
- ViewModel không giữ `String` hiển thị. Text cho UI được trả về dạng `UiText` hoặc `@StringRes`.
- Coroutine chạy trong `viewModelScope`. Dispatcher được inject (`@Dispatcher(IO)`), không hardcode `Dispatchers.IO`.

## 6. Navigation

- Dùng Navigation Compose với **route type-safe** (`@Serializable`). Không dùng route dạng chuỗi.
- Mỗi feature tự khai báo trong `feature/<x>/navigation/XxxNavigation.kt`:

```kotlin
@Serializable data object OnboardingDestination

fun NavController.navigateToOnboarding(navOptions: NavOptions? = null) =
    navigate(OnboardingDestination, navOptions)

fun NavGraphBuilder.onboardingScreen(
    onNavigateToSignUp: () -> Unit,
    onNavigateToSignIn: () -> Unit,
    onContinueAsGuest: () -> Unit,
) {
    composable<OnboardingDestination> {
        OnboardingRoute(onNavigateToSignUp, onNavigateToSignIn, onContinueAsGuest)
    }
}
```

- `ui/ToeicSpaceNavHost.kt` chỉ làm việc ghép các hàm `xxxScreen(...)` lại với nhau.
- Đặt tên: `XxxDestination` cho object route, `XxxRoute` cho composable có ViewModel, `XxxScreen` cho composable stateless.

## 7. Dependency Injection (Hilt)

| Module | Provide |
|---|---|
| `NetworkModule` | `Json`, `OkHttpClient`, `Retrofit`, các `XxxApiService` |
| `DataStoreModule` | `DataStore<Preferences>`, `TokenStorage` |
| `DispatchersModule` | `@Dispatcher(IO)`, `@Dispatcher(Default)` |
| `RepositoryModule` (theo feature) | `@Binds XxxRepositoryImpl → XxxRepository` |

Các đối tượng dùng chung toàn app (Retrofit, DataStore, Room) có scope `@Singleton`. Dùng `hiltViewModel()` từ artifact `androidx.hilt:hilt-lifecycle-viewmodel-compose`.

## 8. Kết quả & lỗi

```kotlin
sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>
    data class Failure(val error: AppError) : AppResult<Nothing>
}

sealed interface AppError {
    data object Network : AppError                          // timeout, không có mạng
    data object Unauthorized : AppError                     // 401 sau khi refresh thất bại
    data class Validation(val fields: Map<String, List<String>>) : AppError
    data class Server(val code: String?, val status: Int) : AppError
    data object Unknown : AppError
}
```

- Không đặt tên `Result`, để tránh trùng với `kotlin.Result`.
- Loading **không** nằm trong `AppResult`. Loading là trạng thái của UI.
- Mapper ở lớp `data` chuyển `ProblemDetails` của backend thành `AppError`. Xem [api-integration.md](api-integration.md).

## 9. Dữ liệu giả khi backend chưa có API

Nhiều phần của mockup chưa có API (streak, nhiệm vụ hằng ngày, khoá học của học viên, từ vựng). Xem bảng ở [api-integration.md](api-integration.md#8-mockup-cần-gì-backend-có-gì). Quy ước:

```text
feature/home/data/
├── remote/                 # HomeApiService, DTO (khi backend có)
├── fake/FakeStudyProgressRepository.kt
└── repository/StudyProgressRepositoryImpl.kt
```

- Interface ở `domain` không thay đổi dù dùng bản giả hay bản thật. Khi backend xong, chỉ đổi phần `@Binds`.
- Bản giả chỉ được bind trong source set `debug`. Ở `release`, section tương ứng hiện `ComingSoonState` hoặc bị ẩn.
- Dữ liệu giả được dùng chung cho cả Preview và test.

## 10. Kiểm thử

| Loại | Công cụ | Bắt buộc |
|---|---|---|
| ViewModel, UseCase, Mapper | JUnit4, `kotlinx-coroutines-test`, Turbine, fake repository | Có |
| Repository | Fake API, MockWebServer | Khuyến khích |
| UI | `createComposeRule()`, chỉ cho luồng quan trọng (đăng nhập, nộp bài) | Khuyến khích |

Ưu tiên **fake** (implement interface trong `src/test`) hơn mock. Đặt tên test theo dạng `` `signIn emits error when password is empty`() ``.
