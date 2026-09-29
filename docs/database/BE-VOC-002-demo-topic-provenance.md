# BE-VOC-002 — Nguồn gốc liên kết topic demo

- **Ngày biên soạn:** 2026-09-29.
- **Người biên soạn:** Codex Backend Lead, trong task BE-VOC-002.
- **Nguồn:** Bộ từ demo `DB-CONTENT-001` (`source=MANUAL`) và tám Initial Topics của `BE-VOC-001`. Đây là phân loại thủ công cho dữ liệu demo, không phải bộ phân loại chuẩn hay nội dung học được phê duyệt riêng.
- **Phương pháp:** Chọn 27 cặp từ–topic có quan hệ nghĩa trực tiếp hoặc bối cảnh sử dụng rõ ràng; so sánh `word`, `part_of_speech`, CEFR và nghĩa tiếng Việt trong V4 trước khi gán. Mỗi topic có ít nhất ba liên kết để bộ lọc `topicId` có dữ liệu demo kiểm thử.
- **Rà soát:** Đối chiếu từng cặp với V4/V5, kiểm tra không tạo từ/topic mới, FK đúng và không trùng khóa `(vocabulary_id, topic_id)`. PostgreSQL integration tests kiểm tra số lượng, phân trang và lọc topic. Những từ chưa được gán topic vẫn được trả về ở danh sách chung.
- **Giới hạn:** `Technology` dùng các từ khái niệm thường gặp trong bối cảnh công nghệ (`framework`, `efficiency`, `reliability`). Ví dụ câu không có trong seed production; `vocabulary_examples` để trống cho tới khi có nội dung được biên soạn/phê duyệt, còn API trả mảng rỗng.
- **Độ phủ CEFR của 27 liên kết:** A1: 10; A2: 9; B1: 1; B2: 7; C1: 0; C2: 0. Bộ seed này chỉ chứng minh đường đọc/lọc của API, không bảo đảm mọi mức CEFR đều có từ thuộc mọi topic. `DB-CONTENT-002` cần đánh giá giới hạn này khi sử dụng goal–topic relevance; không suy diễn một liên kết từ–topic không có trong dữ liệu.
