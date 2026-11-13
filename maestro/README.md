# UI automation (Maestro)

Flow kiểm thử end-to-end chạy trên emulator với backend thật ở máy local.

## Chuẩn bị

1. Backend: trong `toeic-space-be`, chạy `docker compose --profile identity up -d`.
2. Một tài khoản **đã xác thực email** trên backend local, dùng riêng cho test. Flow `profile/` ghi đè họ tên, SĐT, giới thiệu, giới tính, mục tiêu và trình độ của tài khoản này.
3. Emulator đang chạy, cài bản debug mới nhất: `gradlew installDebug`.
4. Cài [Maestro CLI](https://docs.maestro.dev/).

## Chạy trên Windows (Command Prompt)

Maestro chạy trên Java, mà Java trên Windows đọc file theo bảng mã của hệ thống chứ không phải UTF-8: chữ tiếng Việt trong flow sẽ thành `??` và không selector nào khớp. Luôn đặt biến này trước:

```bat
set JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8
```

Tài khoản test truyền qua `-e`, không ghi vào file:

```bat
maestro test -e TEST_EMAIL=<email> -e TEST_PASSWORD=<mật khẩu> maestro\
```

Chạy riêng một nhóm hoặc một flow:

```bat
maestro test maestro\auth\
maestro test -e TEST_EMAIL=<email> -e TEST_PASSWORD=<mật khẩu> maestro\profile\update_profile_success.yaml
```

Flow `auth/` không cần tài khoản và không tạo dữ liệu trên backend.

## Test case

| ID | Flow | Kiểm tra |
|---|---|---|
| TC-AUTH-01 | `auth/sign_up_live_validation` | Email sai báo lỗi sau khi ngừng gõ, sửa đúng thì lỗi mất |
| TC-AUTH-02 | `auth/sign_up_live_validation` | Mật khẩu yếu báo lỗi ngay, không cần bấm nút |
| TC-AUTH-03 | `auth/sign_up_live_validation` | Nhập lại sai thì báo không khớp; sửa ô **Mật khẩu** cho khớp thì lỗi mất |
| TC-PRO-01 | `profile/update_profile_success` | Lưu mọi loại trường, form đóng, báo "Đã lưu" |
| TC-PRO-02 | `profile/update_profile_success` | Tắt mở lại app, dữ liệu tải lại đúng từ server |
| TC-PRO-03, 04 | `profile/update_profile_validation` | Họ tên trống, SĐT sai: báo lỗi, không gửi |
| TC-PRO-05 | `profile/update_profile_cancel` | Huỷ thì không lưu thay đổi |

## Cấu trúc và quy ước

| Thư mục | Nội dung |
|---|---|
| `common/` | Bước dùng chung (đăng nhập, mở màn hình), gọi bằng `runFlow` |
| `auth/` | Đăng ký, đăng nhập |
| `profile/` | Hồ sơ (PBL-108) |

- Ô nhập tìm theo `id:` (test tag). Danh sách tag nằm trong `app/src/main/java/com/toeicspace/android/core/ui/TestTags.kt`; đổi tag thì sửa cả flow.
- Nút và nhãn tìm theo chữ trong `strings.xml`. Chữ là regex: ký tự như `+`, `*`, `.` phải escape và đặt trong nháy đơn, ví dụ `'800\+'`.
- Trước khi chạm vào ô ở dưới, dùng `scrollUntilVisible` vì bàn phím có thể che ô đó.
- Không dùng `hideKeyboard`: trên Android nó bấm Back, và trong form sửa hồ sơ Back sẽ huỷ form.
- `launchApp` với `clearState: true` luôn mở onboarding, nhưng phải chờ splash xong (`extendedWaitUntil`) rồi mới chạm.
