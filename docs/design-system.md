# Design system

**Nguồn thiết kế:** bộ Stitch "Serene Ocean Journal". Token gốc nằm trong file `DESIGN.md` của bộ export Stitch, bản Figma (TOEICSpace UI-UX) được copy từ đó. Code theme nằm ở `core/designsystem/theme/`, component ở `core/designsystem/component/`. Hiện tại hai thư mục này vẫn ở `core/ui/` và sẽ được chuyển ở bước refactor 2.

Mockup Stitch là **tham khảo, không phải spec** ([ADR-0005](adr/0005-stitch-mockups-as-reference.md)). Token trong file này mới là chuẩn để code theo.

## 1. Nguyên tắc chung

1. Màn hình **chỉ dùng token**, không viết giá trị thô:

   | Loại | Dùng | Không dùng |
   |---|---|---|
   | Màu | `MaterialTheme.colorScheme.*`, `ToeicSpaceTheme.extendedColors.*` | `Color(0xFF...)`, `OceanicInk` |
   | Chữ | `MaterialTheme.typography.*`, `ToeicSpaceTheme.textStyles.*` | `fontSize = 14.sp`, `.copy(fontSize = ...)` |
   | Khoảng cách | `Spacing.*` | `padding(12.dp)` |
   | Kích thước | `Dimens.*`, `IconSize.*`, `XxxDefaults.*` | `size(36.dp)` |
   | Bo góc | `MaterialTheme.shapes.*`, `PillShape` | `RoundedCornerShape(24.dp)` |

2. Giá trị literal **chỉ** được viết ở ba chỗ:
   - trong `theme/`, nơi định nghĩa token;
   - chi tiết hình học riêng của một component, khai báo `private val` ở đầu file component và đặt tên rõ nghĩa;
   - trong hàm `@Preview`.

## 2. Màu

### 2.1 Palette thương hiệu (theo DESIGN.md)

| Token | Hex | Vai trò |
|---|---|---|
| `OceanicInk` | `#0A2045` | Chữ chính, icon |
| `DeepCerulean` | `#1467C2` | Primary, CTA, trạng thái đang chọn |
| `SkySoft` | `#7ED6FF` | Washi tape, chip phụ, trang trí |
| `SeaTurquoise` | `#22B5E1` | Viền focus của input, điểm nhấn |
| `CoralNacre` | `#FF9092` | Cột mốc, highlight, bookmark |
| `SunlitGold` | `#E3C23B` | Streak, thành tích |
| `Pearl` | `#F8EAC6` | Nền card streak |
| `JournalPaper` | `#FDFCF5` | Nền card dạng giấy, nền input |
| `MorningCanvas` | `#E7FAFF` | Nền gradient của trang |
| `MutedSlateInk` | `#304568` | Chữ phụ |
| `SubtleOutline` | `#8CB6DB` | Viền mảnh |
| `BadgeRed` | `#D01C29` | Chấm thông báo |

Các màu này là `internal` và chỉ được dùng trong `theme/`. Bên ngoài `theme/`, mọi nơi lấy màu qua hai tầng:

- **Vai trò M3** (`MaterialTheme.colorScheme`): `primary = DeepCerulean`, `onSurface = OceanicInk`, `onSurfaceVariant = MutedSlateInk`, `outlineVariant = SubtleOutline`, `error`, ...
- **Vai trò mở rộng** (`ToeicSpaceTheme.extendedColors`): `brandGradient`, `pageGradient`, `streak`, `streakContainer`, `badge`, `washi`, `paper`.

Màu riêng của một component (ví dụ nền nút secondary) nằm trong `ToeicButtonDefaults`, không khai báo ở `Color.kt`.

### 2.2 Chỗ DESIGN.md mâu thuẫn và cách chốt

| Mâu thuẫn | Đã chốt |
|---|---|
| Token `primary` trong YAML là `#004F9B`, nhưng phần mô tả ghi `#1467C2` | `primary = #1467C2`. `#004F9B` dùng làm `onPrimaryContainer` hoặc trạng thái pressed |
| Nút primary gradient: DESIGN.md ghi `#1467C2 → #C2A7F4` (lavender), onboarding vẽ blue → coral, Home vẽ blue → tím | **Một** `brandGradient`: `#1467C2 → #C2A7F4` (theo DESIGN.md). Coral chỉ dùng làm điểm nhấn. Cần designer xác nhận lại |
| Nền app: `surface #FAF9FF` trong YAML, `MorningCanvas #E7FAFF` trong phần mô tả | `background = #FAF9FF`. `MorningCanvas` chỉ dùng trong `pageGradient` ở phần đầu trang |
| Badge: `#D01C29` (shell) và coral (code cũ) | Chấm thông báo dùng `BadgeRed`. Coral không dùng cho cảnh báo |

### 2.3 Đồng bộ với web

Web (`toeic-space-fe`, 25/10) đang dùng `--ui-blue #4867A8`, `--ui-rose #9D4B70`, `--pearl`, font Baloo 2 + Plus Jakarta Sans. Mobile theo Stitch với `#1467C2`, Quicksand + Be Vietnam Pro. Hai bên **chưa cùng bộ nhận diện**. Mobile chưa thay đổi gì cho tới khi team chốt bộ token chung. Đề xuất: một file `design-tokens.json` dùng chung, sinh ra CSS variables cho web và `Color.kt` / `Type.kt` cho mobile.

## 3. Typography

Ánh xạ token Stitch sang đủ 15 slot của Material 3. **Phải khai báo đủ 15 slot**, slot nào thiếu sẽ rơi về Roboto.

| Slot M3 | Token Stitch | Font | Size / line height | Dùng cho |
|---|---|---|---|---|
| `displayLarge` | display-hero-mobile | Quicksand Bold | 36 / 44 | Điểm số lớn |
| `displayMedium` | — | Quicksand Bold | 32 / 40 | Số streak |
| `displaySmall` | — | Quicksand Bold | 28 / 36 | |
| `headlineLarge` | headline-lg-mobile | Quicksand Bold | 26 / 34 | Tiêu đề màn hình |
| `headlineMedium` | headline-md | Quicksand Bold | 24 / 32 | |
| `headlineSmall` | headline-sm | Quicksand Bold | 18 / 26 | Tiêu đề card |
| `titleLarge` | — | Quicksand Bold | 20 / 28 | Tên người dùng ở top bar |
| `titleMedium` | — | Quicksand Bold | 16 / 24 | Tiêu đề section, greeting |
| `titleSmall` | — | Quicksand Bold | 14 / 20 | Tên item trong danh sách |
| `bodyLarge` | body-large | Be Vietnam Pro Regular | 18 / 28 | Đoạn đọc TOEIC, transcript |
| `bodyMedium` | body-base | Be Vietnam Pro Regular | 16 / 26 | Nội dung mặc định |
| `bodySmall` | caption | Be Vietnam Pro Medium | 13 / 18 | Ghi chú, thời gian |
| `labelLarge` | body-bold | Be Vietnam Pro Bold | 15 / 24 | Chữ trên nút |
| `labelMedium` | body-medium | Be Vietnam Pro Medium | 13 / 18 | Chip, tag, pill |
| `labelSmall` | — | Be Vietnam Pro Medium | 11 / 16 | Nhãn tab dưới (tối thiểu 11) |

Style ngoài thang M3, khai báo trong `ToeicSpaceTheme.textStyles`:

| Style | Token Stitch | Font | Dùng cho |
|---|---|---|---|
| `kicker` | label-caps | Quicksand Bold 12/16, tracking 0.24em, viết hoa | Nhãn nhỏ phía trên tiêu đề |
| `editorial` | title-editorial | Playfair Display SemiBold 28/36 | Chỉ dùng cho logo hoặc chứng chỉ, hạn chế |
| `slogan` | — | **Không dùng font.** Slogan Dancing Script là ảnh `img_brand_slogan` | Splash |

Quy tắc:

- Chỉ dùng weight và style có file font tương ứng. Không ép `FontStyle.Italic` lên font không có bản italic.
- Màn hình không tự `.copy(fontSize = ...)`. Cần style mới thì thêm vào bảng trên.

## 4. Spacing & kích thước

DESIGN.md dùng `space-xs 4 · sm 8 · md 16 · lg 24 · xl 32 · 2xl 48 · 3xl 80`. Mobile thêm các bậc 2, 12, 20 để đủ thang 4pt:

```kotlin
object Spacing {            // CHỈ dùng cho khoảng cách
    val none = 0.dp
    val xxs = 2.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp          // = gutter, margin mobile
    val xl = 20.dp
    val xxl = 24.dp
    val xxxl = 32.dp
    val section = 48.dp
    val screenHorizontal = lg
}

object IconSize { val sm = 16.dp; val md = 20.dp; val lg = 24.dp }

object Dimens {
    val minTouchTarget = 48.dp
    val buttonHeight = 48.dp
    val topBarHeight = 56.dp          // theo spec shell, chưa tính status bar
    val bottomBarHeight = 64.dp
    val bottomBarCornerRadius = 26.dp
    val avatarTopBar = 40.dp
    val borderThin = 1.dp
    val contentMaxWidth = 600.dp
}
```

| Bạn đang viết... | Dùng |
|---|---|
| `padding`, `Spacer`, `Arrangement.spacedBy` | `Spacing.*` |
| Chiều cao nút, top bar, bottom bar | `Dimens.*` |
| Cỡ icon | `IconSize.*` |
| Cỡ avatar, mascot | tham số mặc định của component (`size: Dp = ToeicAvatarDefaults.Size`) |
| Offset badge, viền 1.5dp | `private val` ở đầu file component |

## 5. Hình dạng & độ nổi

| Token | Giá trị | Dùng cho |
|---|---|---|
| `shapes.extraSmall` | 8dp | Tag nhỏ |
| `shapes.small` | 16dp | Input, chip vuông |
| `shapes.medium` | 24dp | Card nội dung |
| `shapes.large` | 32dp | Card lớn, bottom sheet |
| `shapes.extraLarge` | 48dp | Hero |
| `PillShape` | 50% | Nút, badge, ô tìm kiếm |

Độ nổi theo DESIGN.md, dùng `Modifier.shadow` với `ambientColor` / `spotColor` = `OceanicInk`:

| Mức | Dùng cho | Elevation |
|---|---|---|
| 1 | Card dạng giấy | 0dp, chỉ có viền `SubtleOutline` 40% |
| 2 | Card thường | 4dp |
| 3 | Bottom bar nổi, dialog | 12dp |

## 6. Hiệu ứng kính mờ (glass) & trang trí

Mockup dùng nhiều `backdrop-blur` (kính mờ). Trên Android:

- **API 31+:** có thể làm mờ bằng `Modifier.blur` hoặc `RenderEffect`. Tuy vậy, làm mờ *nội dung phía sau* (backdrop) không có sẵn trong Compose và tốn GPU.
- **API 28–30 (gồm máy test API 29):** không hỗ trợ.

**Quy ước:** surface "kính" là màu trắng đục với alpha 85–92% kèm viền `SubtleOutline`, **không** blur. Chấm lưới 22px, washi tape và bong bóng là ảnh hoặc `drawBehind`, đặt sau cờ `enableDecorations` để tắt được trên máy yếu hoặc khi người dùng bật giảm chuyển động.

## 7. Accessibility & các máy phóng to giao diện

| Thiết lập của máy | Ảnh hưởng | Chính sách |
|---|---|---|
| Cỡ chữ (`fontScale`) | Đổi kích thước `sp` | **Giới hạn tối đa 1.3** cho UI chung. Màn đọc nội dung dài dùng fontScale đầy đủ |
| Cỡ hiển thị (`density`) | Đổi kích thước `dp` | **Không ghi đè.** Bố cục phải tự co giãn |

Lý do ở [ADR-0004](adr/0004-font-scale-cap.md).

```kotlin
private const val MAX_FONT_SCALE = 1.3f

@Composable
fun ToeicSpaceTheme(content: @Composable () -> Unit) {
    val density = LocalDensity.current
    val cappedDensity = remember(density) {
        Density(density.density, density.fontScale.coerceAtMost(MAX_FONT_SCALE))
    }
    CompositionLocalProvider(LocalDensity provides cappedDensity) {
        MaterialTheme(/* ... */, content = content)
    }
}
```

- Container có chữ dùng `heightIn(min = ...)`, không đặt `height(...)` cố định. Mockup onboarding đã bị vỡ chữ đúng ở các chỗ cao cố định: nút "Bỏ qua", nút "TIẾP THEO", washi label.
- Text dài đặt `maxLines` kèm `overflow`, hoặc cho phép xuống dòng. Nội dung cao phải cuộn được.
- Vùng chạm tối thiểu 48dp (`Modifier.minimumInteractiveComponentSize()`).
- Mascot trang trí đặt `contentDescription = null`. Ảnh mang ý nghĩa (offline, lỗi) phải có mô tả.
- Phần tử tự vẽ có `role`. Tab dùng `Modifier.selectable(role = Role.Tab)`.
- Animation lặp vô hạn phải tắt khi người dùng bật giảm chuyển động.
- Kiểm tra bằng `@PreviewFontScale` và trên máy thật với cỡ chữ và cỡ hiển thị lớn nhất.

## 8. Hướng màn hình & màn hình lớn

Chính sách ở [ADR-0002](adr/0002-orientation-policy.md): manifest giữ `portrait`. Từ Android 16, hệ thống tự cho xoay trên màn ≥600dp. Mọi màn hình vẫn phải hiển thị được ở landscape và multi-window:

- đọc `currentWindowAdaptiveInfo().windowSizeClass` ở `ToeicSpaceApp`, truyền xuống dưới dạng tham số;
- từ 600dp trở lên: NavigationRail thay BottomBar, nội dung giới hạn bởi `Dimens.contentMaxWidth`, Onboarding chia hai cột;
- state sống qua khi xoay (ViewModel, `rememberSaveable`).

## 9. Splash & màn Loading thương hiệu

Chính sách ở [ADR-0003](adr/0003-splash-and-branded-loading.md): `core-splashscreen` dùng cùng màu nền với màn Loading, không chờ cố định, không chặn khi offline.

## 10. Điều chỉnh so với mockup Stitch

| Mockup | Vấn đề | Trong app |
|---|---|---|
| Khung iPhone 390×844, có dynamic island | Không phải kích thước Android phổ biến | Thiết kế và test ở 360×800 và 412×915 dp |
| Thanh "Prototype test", ghi chú code, `Duration ~1.8s` | Chú thích của mockup | Bỏ |
| Tab "Cá nhân", nhưng code cũ ghi "Tài khoản" | Không nhất quán | Dùng **"Cá nhân"** (`home_label_tab_profile`) |
| Nhãn bottom bar 10sp, code cũ 9sp | Dưới mức tối thiểu | `labelSmall` 11sp |
| Top bar 56dp (spec) nhưng code cũ 64dp | Lệch | `Dimens.topBarHeight = 56.dp` |
| Ba kiểu gradient cho nút primary | Không nhất quán | Một `brandGradient` (mục 2.2) |
| Ảnh mascot trỏ tới `googleusercontent.com` | Link tạm của Stitch | Dùng `res/drawable/oy2_*` có sẵn |
| Kính mờ khắp nơi | Không chạy được trên API < 31 | Mục 6 |
| Card "Nhập mã lớp", "Khoá học của tôi", streak, deadline | Backend chưa có API cho học viên | Xem [screens-flow.md](screens-flow.md#52-ba-biến-thể-của-tab-trang-chủ) |
| Tag trên slide onboarding 1 trong mockup ("Nhiệm vụ ngày") khác `strings.xml` hiện tại | Nội dung chưa chốt | `strings.xml` là nguồn chuẩn. Đổi nội dung thì sửa ở đó |
| Thanh chọn màn trong mockup onboarding có "S1–S4, Màn 5", nhưng chỉ có 4 chấm chỉ trang | Đếm lẫn cả màn khác | 3 slide + 1 trang auth = 4 chấm |

## 11. Component

| Component | Ghi chú |
|---|---|
| `ToeicButton` (primary, secondary, text) | Primary là pill dùng `brandGradient`. Secondary nền trắng đục, viền `SubtleOutline`. Có trạng thái loading |
| `ToeicPillButton` | Cao 32dp khi nhìn, vùng chạm 48dp |
| `ToeicIconButton` | Tròn 40dp, có badge `BadgeRed` |
| `ToeicTopBar` | Avatar kèm badge streak, lời chào, tên, action. Nhận `windowInsets` qua tham số |
| `ToeicNavigationBar` | Nổi, bo góc 26dp, 5 tab, `Role.Tab` |
| `ToeicAvatar` | Hỗ trợ URL (Coil), badge streak |
| `ToeicTextField` | Nền `JournalPaper`, viền `SeaTurquoise` khi focus, có lỗi và nút hiện/ẩn mật khẩu |
| `ToeicCard` | Dạng giấy (mức 1) hoặc nổi (mức 2), có thể kèm washi tape |
| `WashiLabel` | Kicker nằm trên dải washi, xoay -4° |
| `StreakCard`, `ContinueLearningCard`, `CourseCard` | Component riêng của feature Home |
| `MascotView` | Animation trôi nổi chạy trong `graphicsLayer` |
| `LoadingState`, `ErrorState`, `EmptyState`, `ComingSoonState` | Dùng mascot tương ứng |
