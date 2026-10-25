# Release

Hiện tại app được phân phối dưới dạng **APK** cho tester. Chưa phát hành lên Google Play. Quảng cáo và in-app purchase nằm ngoài phạm vi.

## 1. Version

- `versionName` theo [Semantic Versioning](https://semver.org): `MAJOR.MINOR.PATCH`, ví dụ `0.3.0`.
- `versionCode` tăng 1 ở mỗi bản phát cho tester.
- Mỗi bản release được tag `vX.Y.Z` trên nhánh `main` và có GitHub Release kèm changelog.

## 2. Ký ứng dụng

- Keystore release **không** được commit (`*.jks` và `*.keystore` đã có trong `.gitignore`).
- Đường dẫn và mật khẩu keystore khai báo trong `local.properties` (xem `local.properties.example`). `signingConfigs.release` đọc các giá trị này.
- Người giữ keystore: **YDraGN**. Keystore được sao lưu ở nơi an toàn ngoài repo. Mất keystore thì không cập nhật được app cho người đã cài.
- SHA-1 của keystore release phải được đăng ký vào OAuth client Android trên Google Cloud để Google Sign-In hoạt động (xem [api-integration.md](api-integration.md#4-đăng-nhập-bằng-google)).

## 3. Build

```bash
./gradlew clean ktlintCheck lintRelease testReleaseUnitTest assembleRelease
```

File output: `app/build/outputs/apk/release/app-release.apk`.

Build release phải:

- bật R8 (`optimization { enable = true }`) để thu nhỏ và làm rối mã;
- dùng `API_BASE_URL` HTTPS;
- không có logging body của HTTP.

## 4. Checklist trước khi phát

- [ ] CI trên `develop` xanh, đã merge `develop` vào `main`
- [ ] Đã tăng `versionCode` và `versionName`
- [ ] Đã cài thử trên máy thật API 29 và một máy Android 15 trở lên
- [ ] Đã thử xoay màn hình trên tablet hoặc emulator ≥600dp
- [ ] Đã thử luồng Onboarding → Sign in → Home và chế độ khách
- [ ] Đã tạo tag `vX.Y.Z` và GitHub Release, đính kèm APK

## 5. Phân phối

Đính kèm APK vào GitHub Release. Tester cần bật "Cài đặt ứng dụng không rõ nguồn gốc" cho trình duyệt hoặc ứng dụng quản lý file mà họ dùng để mở APK.
