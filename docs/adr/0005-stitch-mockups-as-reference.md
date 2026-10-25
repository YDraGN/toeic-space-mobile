# ADR-0005: Mockup Stitch là tham khảo, token là chuẩn

- **Trạng thái:** Accepted
- **Ngày:** 2026-10-25

## Bối cảnh

Giao diện được sinh bằng Google Stitch (bộ "Serene Ocean Journal": `DESIGN.md` và 6 màn kèm `code.html` + `screen.png`), sau đó copy sang Figma. Bản Stitch có những đặc điểm sau:

- `code.html` là HTML + Tailwind cho web, dùng `backdrop-blur`, ảnh trên `googleusercontent.com` và khung iPhone 390×844.
- Các màn không đồng nhất với nhau: ba kiểu gradient cho nút primary, nhãn tab khác nhau, top bar 56dp so với 64dp.
- Có các section mà backend chưa hỗ trợ: streak, nhiệm vụ hằng ngày, khoá học của tôi, nhập mã lớp, deadline.
- Có chú thích của prototype (thanh "Prototype test", sơ đồ component) lẫn trong ảnh.

## Quyết định

1. **Thứ tự ưu tiên khi các nguồn mâu thuẫn:** `docs/design-system.md` > token trong `DESIGN.md` > Figma > ảnh `screen.png` > `code.html`.
2. Không chuyển `code.html` sang Compose từng dòng một. Chỉ lấy bố cục, phân cấp và nội dung, rồi dựng lại bằng component trong design system.
3. Mỗi điểm lệch giữa mockup và app được ghi vào bảng "Điều chỉnh so với mockup Stitch" trong `design-system.md`.
4. Section cần backend chưa có thì dùng `FakeRepository` ở debug và **ẩn ở release** (xem `api-integration.md` mục 8).
5. Không dùng blur cho nền phía sau. Surface "kính" là màu trắng đục với alpha 85–92%.

## Hệ quả

- App đồng nhất hơn mockup, nhưng một số hiệu ứng thị giác bị đơn giản hoá.
- Designer cần xác nhận lại `brandGradient` và nhãn tab. Khi xác nhận xong, cập nhật `design-system.md` và Figma cùng lúc.

## Xem lại khi

- Team có bộ token dùng chung giữa web và mobile.
- Designer cập nhật Figma thành bản chính thức thay cho bản Stitch.
