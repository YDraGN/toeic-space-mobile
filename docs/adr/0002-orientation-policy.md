# ADR-0002: Khoá dọc trên điện thoại, xoay tự do trên màn ≥600dp

- **Trạng thái:** Accepted
- **Ngày:** 2026-10-25

## Bối cảnh

Thiết kế của team được vẽ cho điện thoại dọc. Từ Android 16, với app target SDK 36 trở lên, hệ thống **bỏ qua** `screenOrientation`, `resizeableActivity` và giới hạn tỉ lệ khung hình trên màn có smallest width ≥600dp (tablet, máy gập, cửa sổ desktop). App đang target SDK 37 nên cờ opt-out tạm thời của Android 16 không còn tác dụng.

## Quyết định

- Giữ `android:screenOrientation="portrait"` trên `MainActivity`.
  - Điện thoại (<600dp): luôn dọc, đúng thiết kế.
  - Màn ≥600dp chạy Android 16+: hệ thống cho xoay, đáp ứng yêu cầu của Google.
- Mọi màn hình phải hiển thị được ở landscape và trong multi-window. Bố cục thay đổi theo `WindowSizeClass` (xem [design-system.md](../design-system.md#7-hướng-màn-hình--màn-hình-lớn)).

## Hệ quả

- Tablet chạy **Android 15 trở xuống** vẫn bị khoá dọc. Đây là hành vi được chấp nhận ở giai đoạn này.
- UI phải được test ở landscape trên emulator tablet (Pixel Tablet, API 36+).
- Nếu sau này muốn mọi tablet đều xoay được (kể cả bản Android cũ), thì bỏ khoá trong manifest và chỉ khoá dọc bằng code khi `smallestScreenWidthDp < 600`.

## Xem lại khi

- Team có thiết kế riêng cho tablet hoặc landscape.
- Chuẩn bị phát hành lên Google Play. Khi đó cần đối chiếu với large-screen quality guidelines.
