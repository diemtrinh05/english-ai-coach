package com.example.englishaicoach.data.api;

import com.example.englishaicoach.data.dto.VocabularyDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface VocabularyApi {
    @GET("topics")
    Call<VocabularyDto.TopicPageDto> topics(@Query("page") int page,
                                             @Query("size") int size);

    @GET("vocabulary")
    Call<VocabularyDto.PageDto> list(
            @Query("page") int page,
            @Query("size") int size,
            @Query("search") String search,
            @Query("cefr") String cefr,
            @Query("topicId") String topicId,
            @Query("partOfSpeech") String partOfSpeech,
            @Query("sort") String sort);

    @GET("vocabulary/{vocabularyId}")
    Call<VocabularyDto> detail(@Path("vocabularyId") String vocabularyId);

    @GET("vocabulary/{vocabularyId}/examples")
    Call<List<VocabularyDto.ExampleDto>> examples(@Path("vocabularyId") String vocabularyId);
}
