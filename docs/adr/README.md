# Architecture Decision Records

Mỗi file ghi lại **một** quyết định kỹ thuật quan trọng: bối cảnh lúc quyết định, lựa chọn đã chốt, và khi nào cần xem lại. Khi đổi một quyết định, không sửa ADR cũ. Viết ADR mới và đánh dấu ADR cũ là `Superseded by ADR-xxxx`.

| # | Quyết định | Trạng thái |
|---|---|---|
| [0001](0001-single-gradle-module.md) | Dùng một Gradle module, tách package theo feature | Accepted |
| [0002](0002-orientation-policy.md) | Khoá dọc trên điện thoại, xoay tự do trên màn ≥600dp | Accepted |
| [0003](0003-splash-and-branded-loading.md) | System splash kết hợp màn Loading thương hiệu | Accepted |
| [0004](0004-font-scale-cap.md) | Giới hạn font scale ở mức 1.3 cho UI chung | Accepted |
| [0005](0005-stitch-mockups-as-reference.md) | Mockup Stitch là tham khảo, token là chuẩn | Accepted |

Mẫu cho ADR mới: copy một file có sẵn, giữ các mục *Bối cảnh*, *Quyết định*, *Hệ quả*, *Xem lại khi*.
