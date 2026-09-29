# DB-CONTENT-002 — Nguồn gốc relevance goal–topic demo

- **Ngày biên soạn:** 2026-09-29.
- **Người biên soạn:** Codex Backend Lead trong task DB-CONTENT-002.
- **Nguồn:** Bảy goal từ seed V3, tám Initial Topics từ V5, 27 liên kết từ–topic demo trong V6; AI Personalization v1.3 quy định `goal_topics` cung cấp relevance cho recommendation V1. Đây là dữ liệu demo biên soạn thủ công, không phải trọng số đo từ người học hoặc quy tắc chấm điểm mới.
- **Tiêu chí:** Quan hệ nghĩa trực tiếp giữa tên goal và topic nhận `1.000`; bối cảnh học liên quan nhưng rộng hơn nhận `0.500`. `GENERAL_ENGLISH` phủ cả tám topic ở `0.250` để có lựa chọn chung nhưng không lấn goal chuyên biệt ở cùng topic. Không tạo topic hoặc goal mới.
- **Rà soát:** Đối chiếu tên goal/topic chính xác với V3/V5 và kiểm tra từng topic chuyên biệt có từ active được gắn trong V6. Kiểm tra FK, uniqueness `(goal_id, topic_id)`, khoảng `0..1`, reapply không thay dữ liệu và mỗi goal ngoài GENERAL_ENGLISH có ít nhất một lựa chọn topic với vocabulary active qua PostgreSQL integration test.
- **Giới hạn:** Đây là bộ relevance demo cho dữ liệu hiện có, chưa phải ma trận đã hiệu chỉnh theo hiệu quả học. V6 chưa gắn `vocabulary_topics` cho C1/C2; vì vậy goal–topic relevance không tạo được từ mới theo goal ở hai mức đó. `BE-PERS-003` phải tuân theo quy tắc eligibility/fallback CEFR của AI Personalization v1.3 và không được suy diễn coverage không tồn tại. Không thay đổi công thức, API, schema hay client.

| Goal | Topic và relevance |
|---|---|
| `GENERAL_ENGLISH` | Daily Life, Travel, Food, Business, Technology, Education, Health, Environment: `0.250` mỗi topic |
| `TRAVEL` | Travel `1.000`; Food `0.500` |
| `BUSINESS` | Business `1.000`; Technology `0.500` |
| `TOEIC` | Business `1.000`; Travel `0.500` |
| `IELTS` | Education `1.000`; Environment, Health `0.500` |
| `COMMUNICATION` | Daily Life `1.000`; Travel, Food `0.500` |
| `ACADEMIC` | Education `1.000`; Technology, Environment `0.500` |
