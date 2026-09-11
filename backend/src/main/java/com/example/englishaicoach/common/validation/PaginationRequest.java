package com.example.englishaicoach.common.validation;

import com.example.englishaicoach.common.pagination.PaginationConvention;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record PaginationRequest(
        @Min(value = 0, message = "page phải lớn hơn hoặc bằng 0") Integer page,
        @Min(value = PaginationConvention.MIN_SIZE, message = "size phải lớn hơn hoặc bằng 1")
        @Max(value = PaginationConvention.MAX_SIZE, message = "size phải nhỏ hơn hoặc bằng 100") Integer size) {

    public PaginationRequest {
        page = page == null ? PaginationConvention.DEFAULT_PAGE : page;
        size = size == null ? PaginationConvention.DEFAULT_SIZE : size;
    }
}
