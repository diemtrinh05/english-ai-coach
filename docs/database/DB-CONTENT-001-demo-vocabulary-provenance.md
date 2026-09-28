# DB-CONTENT-001 — nguồn gốc bộ từ vựng demo

- Ngày biên soạn: 2026-09-28.
- Người tạo: AI agent Codex, theo phê duyệt của chủ dự án cho bộ demo tự biên soạn.
- Nguồn: tự biên soạn, không sao chép hay nhập từ một bộ dữ liệu bên ngoài. Giá trị cột `vocabulary.source` là `MANUAL`; không gán tác giả, giấy phép hay mức CEFR cho nguồn thứ ba.
- Phạm vi: 30 mục riêng cho mỗi mức A1, A2, B1, B2, C1, C2 trong `V4__seed_demo_vocabulary.sql`; tổng cộng 180 mục. Đây là dữ liệu demo để có nội dung hoạt động cho catalog và placement. Mức CEFR là phân nhóm ước lượng khi biên soạn, chưa được hiệu chuẩn theo một danh mục CEFR có bản quyền hoặc thẩm định sư phạm độc lập.

## Cách biên soạn và rà soát

1. Chọn từ đơn thông dụng cho A1/A2, từ về trải nghiệm và khái niệm phổ biến cho B1/B2, từ trừu tượng và ít gặp hơn cho C1/C2. Chọn một nghĩa tiếng Việt cụ thể tương ứng với từ loại đã lưu.
2. Codex rà soát từng mục về chính tả tiếng Anh, dấu tiếng Việt, sự phù hợp giữa `word`, `part_of_speech`, `meaning_vi`, tính hợp lý tương đối của CEFR, và các nghĩa trùng/khó phân biệt trong cùng mức để dùng cho câu hỏi nhiều lựa chọn. Không khẳng định đã được con người hay chuyên gia ngôn ngữ duyệt.
3. Test PostgreSQL trên migration thực kiểm tra 30 mục active/mức, nghĩa không rỗng, `source=MANUAL`, từ loại không rỗng, khóa tự nhiên không trùng, và chạy lại seed không đổi số mục/ID/thời điểm tạo.

## Giới hạn và bảo trì

`phonetic_ipa`, `meaning_en`, `audio_url`, `image_url`, topic và example chưa nằm trong acceptance của task này. Không xem bản demo là bộ học liệu chính thức. Nội dung có thể được biên tập sau bằng quy trình quản lý vocabulary; migration đã phát hành phải giữ append-only. `ON CONFLICT DO NOTHING` giữ nguyên mục đã được biên tập khi seed chạy lại.
