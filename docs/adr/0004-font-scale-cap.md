# ADR-0004: Giới hạn font scale ở mức 1.3 cho UI chung

- **Trạng thái:** Accepted
- **Ngày:** 2026-10-25

## Bối cảnh

Nhiều máy (Samsung, Xiaomi, OPPO...) đặt sẵn cỡ chữ (`fontScale`) hoặc cỡ hiển thị (`density`) lớn hơn mặc định. Các màn có bố cục cố định như Onboarding (mascot cùng card) hay bottom bar bị tràn chữ, đẩy nút ra ngoài màn hình. Mặt khác, người dùng thật sự cần chữ to (thị lực kém) vẫn phải đọc được nội dung học.

## Quyết định

- `ToeicSpaceTheme` giới hạn `fontScale` tối đa **1.3** bằng cách cung cấp lại `LocalDensity` (code mẫu ở [design-system.md](../design-system.md#6-accessibility--các-máy-phóng-to-giao-diện)).
- Màn đọc nội dung dài (passage Part 6–7, giải thích đáp án, bài học) dùng `fontScale` đầy đủ của hệ thống, bằng cách bọc trong một `CompositionLocalProvider` riêng.
- **Không** ghi đè `density` (cỡ hiển thị). Bố cục phải tự co giãn: không đặt chiều cao cố định cho container có chữ, nội dung dài phải cuộn được, text có `maxLines` và `overflow`.

## Hệ quả

- UI chung giữ được bố cục trên các máy đặt cỡ chữ lớn, nội dung học vẫn đọc được với chữ to.
- Chưa đáp ứng đầy đủ khuyến nghị của Google (hỗ trợ tới 200%) ở UI chung. Đây là đánh đổi có chủ ý.
- Mỗi màn mới phải có `@PreviewFontScale`.

## Xem lại khi

- Phát hành trên Google Play hoặc có yêu cầu accessibility chính thức.
- Có phản hồi từ người dùng cần chữ lớn hơn.
