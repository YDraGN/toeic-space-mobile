# Contributing

Tài liệu này mô tả quy trình làm việc với repo. Quy tắc viết code nằm ở [docs/coding-conventions.md](docs/coding-conventions.md).

## 1. Nhánh

| Nhánh | Vai trò | Được bảo vệ |
|---|---|---|
| `main` | Code đã release. Chỉ nhận merge từ `develop` hoặc `hotfix/*`. | Có (ruleset `protect-main`) |
| `develop` | Nhánh tích hợp. Mọi PR tính năng đều merge vào đây. | Có (ruleset `protect-develop`) |
| `<username>/<type>-<short-name>` | Nhánh làm việc | Không |

Ví dụ nhánh làm việc: `nganntb/feat-sign-in-screen`, `nganntb/fix-onboarding-skip`, `nganntb/docs-api-integration`.

## 2. Commit

Theo [Conventional Commits](https://www.conventionalcommits.org), viết bằng **tiếng Anh**, ở thể mệnh lệnh, chữ thường, không có dấu chấm cuối:

```text
<type>(<scope>): <summary>

[body: giải thích vì sao thay đổi, nếu cần]

[footer: Closes #12]
```

| `type` | Dùng khi |
|---|---|
| `feat` | Thêm tính năng mà người dùng thấy được |
| `fix` | Sửa lỗi |
| `refactor` | Đổi cấu trúc code, không đổi hành vi |
| `style` | Chỉ format, thường do ktlint |
| `docs` | Tài liệu |
| `test` | Thêm hoặc sửa test |
| `build` | Gradle, dependency, version catalog |
| `ci` | GitHub Actions |
| `chore` | Việc lặt vặt khác |
| `perf` | Cải thiện hiệu năng |

`scope` là tên feature hoặc vùng core: `splash`, `onboarding`, `auth`, `home`, `designsystem`, `network`, `datastore`, `navigation`, `deps`, `docs`.

Ví dụ:

```text
feat(auth): add sign-in screen with email validation
fix(onboarding): show get started button on the last slide
refactor(designsystem): split spacing and dimension tokens
build(deps): remove duplicated material3 dependency
```

Mỗi commit chỉ làm **một** việc và phải build được.

## 3. Pull request

1. Rebase nhánh của bạn lên `develop` mới nhất.
2. Chạy kiểm tra ở máy local:

   ```bash
   ./gradlew ktlintCheck lintDebug testDebugUnitTest assembleDebug
   ```

3. Mở PR vào `develop`. Điền đầy đủ template (mô tả, ảnh chụp hoặc video, cách test).
4. Đặt tiêu đề PR theo format commit, ví dụ `feat(auth): add sign-in screen`.
5. CI phải xanh trước khi review.
6. Merge bằng **Squash and merge**. Commit sau khi squash dùng chính tiêu đề PR.

Nên giữ PR dưới khoảng 400 dòng thay đổi. Tính năng lớn thì chia thành nhiều PR nối tiếp nhau.

## 4. Issue

Dùng template có sẵn: **Bug report**, **Feature request**, **Task**.

| Label | Ý nghĩa |
|---|---|
| `type: bug` / `type: feature` / `type: task` / `type: docs` | Loại công việc |
| `priority: high` / `priority: medium` / `priority: low` | Mức ưu tiên |
| `area: auth` / `area: onboarding` / `area: designsystem` / ... | Vùng code |
| `status: needs-info` / `status: blocked` | Trạng thái |
| `good first issue` | Phù hợp cho người mới |

Tiêu đề issue viết ngắn gọn, mô tả kết quả mong muốn, ví dụ "Sign-in screen shows backend validation errors under each field".

## 5. Phiên bản & release

Xem [docs/release.md](docs/release.md).
