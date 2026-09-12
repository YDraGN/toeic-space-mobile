# API Integration

## Base configuration

- Base URL được cấu hình qua `API_BASE_URL` trong `local.properties`, expose ra code qua `BuildConfig.API_BASE_URL`.
- Mọi request gắn `Authorization: Bearer <token>` qua Retrofit `Interceptor`, trừ endpoint auth (login/register).
- Timeout mặc định: 30s (xem `NETWORK_TIMEOUT_SECONDS` trong `Constants.kt`).

## Response format

Backend trả response theo dạng chung:

​```json
{
  "success": true,
  "data": { ... },
  "message": null
}
​```

Mapper trong `data` layer chịu trách nhiệm parse `data` thành DTO, sau đó map DTO → Domain Model.

## Nhóm API chính

| Nhóm | Base path | Ghi chú |
|---|---|---|
| Auth | `/api/auth` | Login, Register, Refresh token |
| Course | `/api/courses` | Course, Module, Lesson |
| Vocabulary | `/api/vocabulary` | Vocabulary Set, Personal Vocabulary |
| Practice/Test | `/api/assessments` | Practice Set, Mini/Mock Test, Attempt |
| Result | `/api/results` | Result, History, Explanation |
| Mistake | `/api/mistakes` | Mistake Notebook, Smart Review |
| Roadmap | `/api/roadmap` | Placement Test, Goal, Roadmap |
| Notification | `/api/notifications` | In-app Notification |

Chi tiết từng endpoint tham khảo Swagger của Backend (xem README của repo `toeic-space-be`).

## Xử lý lỗi mạng

- Lỗi timeout/không có mạng → hiển thị trạng thái kết nối, cho phép retry, không làm crash app.
- Timer của Mock Test không dừng chỉ vì mất mạng — chỉ dừng khi Student chủ động Submit hoặc hết giờ.