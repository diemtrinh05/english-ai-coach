package com.example.englishaicoach.common.response;

import com.example.englishaicoach.common.pagination.PaginationConvention;
import java.util.List;
import java.util.Objects;
import org.springframework.data.domain.Page;

public record PaginatedResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext) {

    public PaginatedResponse {
        content = List.copyOf(Objects.requireNonNull(content, "content không được null"));
        if (page < 0) {
            throw new IllegalArgumentException("page phải lớn hơn hoặc bằng 0");
        }
        if (size < PaginationConvention.MIN_SIZE || size > PaginationConvention.MAX_SIZE) {
            throw new IllegalArgumentException("size phải nằm trong khoảng 1..100");
        }
        if (totalElements < 0 || totalPages < 0) {
            throw new IllegalArgumentException("Tổng số phần tử và trang không được âm");
        }
    }

    public static <T> PaginatedResponse<T> from(Page<T> source) {
        Objects.requireNonNull(source, "source không được null");
        boolean unpagedEmpty = source.isEmpty() && source.getPageable().isUnpaged();
        int normalizedPage = unpagedEmpty ? PaginationConvention.DEFAULT_PAGE : source.getNumber();
        int normalizedSize = unpagedEmpty ? PaginationConvention.DEFAULT_SIZE : source.getSize();
        int normalizedTotalPages = unpagedEmpty ? 0 : source.getTotalPages();
        return new PaginatedResponse<>(
                source.getContent(),
                normalizedPage,
                normalizedSize,
                source.getTotalElements(),
                normalizedTotalPages,
                source.hasNext());
    }
}
