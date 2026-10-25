# ADR-0001: Dùng một Gradle module, tách package theo feature

- **Trạng thái:** Accepted
- **Ngày:** 2026-10-25

## Bối cảnh

Sản phẩm có thể mở rộng thành nền tảng quản lý nhiều trung tâm, và backend được xây dựng theo microservices. Câu hỏi đặt ra là app có nên tách thành nhiều Gradle module (`:core:*`, `:feature:*`) giống Now in Android hay không.

Tách module giúp ích ở ba việc:

1. build song song và build lại từng phần, có ý nghĩa khi codebase lớn;
2. **compiler** chặn được việc feature này import nhầm feature khác;
3. nhiều người hoặc nhiều team làm việc độc lập trên các phần khác nhau.

Đổi lại, tách module tốn công: phải có convention plugin (`build-logic`), cấu hình Hilt và resource riêng cho từng module, và người mới với Gradle sẽ mất nhiều thời gian hơn.

**Kiến trúc backend không quyết định cách chia module của app.** Microservices là ranh giới triển khai ở phía server. App vẫn chỉ nói chuyện với một API Gateway. Quản lý nhiều trung tâm là vấn đề về **dữ liệu và phân quyền** (`centerId`, role trong session), không phải vấn đề về module.

## Quyết định

- Dùng một module `:app`. Code được chia theo `core/*` và `feature/*`, mỗi feature gồm các lớp `data`, `domain`, `presentation`.
- Giữ ranh giới **như thể đã tách module**:
  - một feature không import trực tiếp `data` hay `presentation` của feature khác; khi cần dùng chung thì đưa phần đó lên `core`;
  - design system (`core/designsystem`) không phụ thuộc feature nào;
  - mỗi feature tự khai báo navigation của mình (`feature/<x>/navigation`).

## Hệ quả

- Build đơn giản và người mới dễ đọc code.
- Ranh giới chỉ được giữ bằng quy ước và code review, compiler không kiểm tra hộ.
- Khi cần tách, có thể chuyển từng thư mục sang module riêng mà không phải tổ chức lại code.

## Xem lại khi

Chỉ cần **một** trong các điều kiện sau xảy ra:

- team mobile có từ 3 người trở lên làm song song;
- build debug sau khi sửa một dòng mất hơn khoảng 2 phút;
- cần phát hành nhiều app dùng chung lõi, ví dụ app riêng cho từng trung tâm (white-label) hoặc app cho giáo viên;
- cần dynamic feature hoặc kiểm tra kiến trúc tự động.

Khi tách, thứ tự đề xuất là: `:core:designsystem` → `:core:network` + `:core:datastore` → `:feature:*`.
