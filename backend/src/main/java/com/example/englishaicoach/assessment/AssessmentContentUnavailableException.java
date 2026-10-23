package com.example.englishaicoach.assessment;

/** Lỗi domain có kiểu; HTTP mapping thuộc task API assessment sau này. */
public final class AssessmentContentUnavailableException extends RuntimeException {
    public static final String CODE = "ASSESSMENT_CONTENT_UNAVAILABLE";

    public AssessmentContentUnavailableException() {
        super("Không đủ nội dung để tạo câu hỏi đánh giá hợp lệ");
    }
}
