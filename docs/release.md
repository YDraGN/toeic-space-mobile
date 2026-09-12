# Release Guide

Ứng dụng được phân phối dưới dạng APK build thủ công, không qua Google Play Store.

## Build release APK

​```bash
./gradlew assembleRelease
​```

File APK output nằm tại `app/build/outputs/apk/release/app-release.apk`.

## Ký ứng dụng (Signing)

- Keystore thật **không được commit** vào repo (đã có trong `.gitignore`).
- Thông tin keystore (path, alias, password) khai báo trong `local.properties`, không hardcode trong `build.gradle.kts`.
- Người giữ keystore: [điền tên/role người quản lý keystore của nhóm].

## Version

- Cập nhật `versionCode` (tăng dần mỗi lần release) và `versionName` (ví dụ `1.0.0`) trong `app/build.gradle.kts` trước mỗi lần build release.

## Phân phối

- Sau khi build, chia sẻ file APK cho tester qua kênh nội bộ (Drive/Zalo nhóm...).
- Cài đặt trên máy Android cần bật "Cài đặt ứng dụng từ nguồn không xác định" (Install unknown apps).