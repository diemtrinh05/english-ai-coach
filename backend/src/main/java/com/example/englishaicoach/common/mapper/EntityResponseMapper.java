package com.example.englishaicoach.common.mapper;

import com.example.englishaicoach.common.response.PaginatedResponse;
import java.util.List;
import java.util.Objects;
import org.springframework.data.domain.Page;

@FunctionalInterface
public interface EntityResponseMapper<E, R> {

    R toResponse(E entity);

    default List<R> toResponseList(Iterable<E> entities) {
        Objects.requireNonNull(entities, "entities không được null");
        var responses = new java.util.ArrayList<R>();
        entities.forEach(entity -> responses.add(toResponse(entity)));
        return List.copyOf(responses);
    }

    default PaginatedResponse<R> toResponsePage(Page<E> entities) {
        Objects.requireNonNull(entities, "entities không được null");
        return PaginatedResponse.from(entities.map(this::toResponse));
    }
}
