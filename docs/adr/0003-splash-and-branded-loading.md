# ADR-0003: System splash kết hợp màn Loading thương hiệu

- **Trạng thái:** Accepted
- **Ngày:** 2026-10-25

## Bối cảnh

Team muốn dùng màn Loading riêng (mascot, ngọc trai, slogan) để tạo nhận diện thương hiệu. Từ Android 12, hệ thống **luôn** hiển thị một splash (nền và icon app) khi cold start. App **không tắt được** splash này. Mẹo dùng icon trong suốt chỉ tạo ra một màn trống, và Google khuyến cáo không làm vậy. Nếu không cấu hình gì, người dùng sẽ thấy hai splash khác nhau nối tiếp nhau.

Trên Android 11 trở xuống (máy test API 29), không có system splash. Thay vào đó `windowBackground` của theme được hiển thị trong lúc app khởi động.

## Quyết định

1. Dùng `androidx.core:core-splashscreen` với theme `Theme.ToeicSpace.Starting`:
   - `windowSplashScreenBackground` trùng màu nền của màn Loading thương hiệu;
   - `windowSplashScreenAnimatedIcon` là logo hoặc mascot của brand;
   - `postSplashScreenTheme` là `Theme.ToeicSpace`.
2. Gọi `installSplashScreen()` trong `MainActivity` và **không** giữ system splash lâu. Màn Loading thương hiệu hiện ngay sau đó với cùng màu nền, nên người dùng thấy liền một mạch.
3. Màn Loading kết thúc khi `SessionState` sẵn sàng **và** đã hiển thị tối thiểu 3 giây (`MIN_SPLASH_MILLIS`), để người dùng kịp thấy mascot và slogan. Nếu `SessionState` cần lâu hơn thì chờ thêm. *(Sửa đổi 2026-11-01: trước đây tối thiểu 600–800 ms, theo phản hồi của product owner nâng lên 3 giây.)*
4. Không chặn người dùng khi offline. Mất mạng chỉ hiện banner, vì app có chế độ khách và cache local.

## Hệ quả

- Hành vi khởi động giống nhau từ API 28 đến 37.
- Không vi phạm hướng dẫn của Google về splash.
- Khi đổi màu nền brand, phải đổi ở cả theme XML và theme Compose.
