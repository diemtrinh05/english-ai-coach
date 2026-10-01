package com.example.englishaicoach.data.repository;

import androidx.annotation.NonNull;

import com.example.englishaicoach.data.api.VocabularyApi;
import com.example.englishaicoach.data.dto.VocabularyDto;
import com.example.englishaicoach.data.mapper.VocabularyMapper;
import com.example.englishaicoach.domain.model.VocabularyItem;
import com.example.englishaicoach.domain.repository.VocabularyRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

public final class RetrofitVocabularyRepository implements VocabularyRepository {
    private static final Cancelable NO_OP = () -> { };
    private final VocabularyApi api;
    private final Map<String, Page> pages = boundedCache(16);
    private final Map<String, VocabularyItem> details = boundedCache(40);
    private final Map<String, List<VocabularyItem.Example>> examples = boundedCache(40);
    private List<VocabularyItem.Topic> topicCache;

    public RetrofitVocabularyRepository(VocabularyApi api) { this.api = api; }

    @Override
    public Cancelable list(Query query, boolean online, Callback<Page> callback) {
        String key = query.cacheKey();
        if (!online) {
            Page cached = pages.get(key);
            if (cached == null) callback.onFailure(Failure.OFFLINE);
            else callback.onSuccess(cached, true);
            return NO_OP;
        }
        Call<VocabularyDto.PageDto> call = api.list(query.page, query.size, emptyToNull(query.search),
                emptyToNull(query.cefr), emptyToNull(query.topicId),
                emptyToNull(query.partOfSpeech), "word,asc");
        call.enqueue(new retrofit2.Callback<VocabularyDto.PageDto>() {
            @Override public void onResponse(@NonNull Call<VocabularyDto.PageDto> request,
                                             @NonNull Response<VocabularyDto.PageDto> response) {
                if (!response.isSuccessful()) {
                    callback.onFailure(response.code() == 404 ? Failure.NOT_FOUND : Failure.HTTP);
                    return;
                }
                VocabularyDto.PageDto body = response.body();
                if (body == null || body.content == null) {
                    callback.onFailure(Failure.INVALID_RESPONSE);
                    return;
                }
                try {
                    List<VocabularyItem> items = new ArrayList<>();
                    for (VocabularyDto item : body.content) items.add(VocabularyMapper.map(item));
                    Page page = new Page(items, body.page, body.hasNext);
                    pages.put(key, page);
                    callback.onSuccess(page, false);
                } catch (IllegalArgumentException failure) {
                    callback.onFailure(Failure.INVALID_RESPONSE);
                }
            }
            @Override public void onFailure(@NonNull Call<VocabularyDto.PageDto> request,
                                            @NonNull Throwable failure) {
                if (!request.isCanceled()) callback.onFailure(Failure.OFFLINE);
            }
        });
        return call::cancel;
    }

    @Override
    public Cancelable detail(String id, boolean online, Callback<VocabularyItem> callback) {
        if (!online) {
            VocabularyItem cached = details.get(id);
            if (cached == null) callback.onFailure(Failure.OFFLINE);
            else callback.onSuccess(cached, true);
            return NO_OP;
        }
        Call<VocabularyDto> call = api.detail(id);
        call.enqueue(new retrofit2.Callback<VocabularyDto>() {
            @Override public void onResponse(@NonNull Call<VocabularyDto> request,
                                             @NonNull Response<VocabularyDto> response) {
                if (!response.isSuccessful()) {
                    callback.onFailure(response.code() == 404 ? Failure.NOT_FOUND : Failure.HTTP);
                    return;
                }
                try {
                    VocabularyItem item = VocabularyMapper.map(response.body());
                    details.put(id, item);
                    examples.put(id, item.examples);
                    callback.onSuccess(item, false);
                } catch (IllegalArgumentException failure) {
                    callback.onFailure(Failure.INVALID_RESPONSE);
                }
            }
            @Override public void onFailure(@NonNull Call<VocabularyDto> request,
                                            @NonNull Throwable failure) {
                if (!request.isCanceled()) callback.onFailure(Failure.OFFLINE);
            }
        });
        return call::cancel;
    }

    @Override
    public Cancelable examples(String id, boolean online,
                               Callback<List<VocabularyItem.Example>> callback) {
        if (!online) {
            List<VocabularyItem.Example> cached = examples.get(id);
            if (cached == null) callback.onFailure(Failure.OFFLINE);
            else callback.onSuccess(cached, true);
            return NO_OP;
        }
        Call<List<VocabularyDto.ExampleDto>> call = api.examples(id);
        call.enqueue(new retrofit2.Callback<List<VocabularyDto.ExampleDto>>() {
            @Override public void onResponse(@NonNull Call<List<VocabularyDto.ExampleDto>> request,
                                             @NonNull Response<List<VocabularyDto.ExampleDto>> response) {
                if (!response.isSuccessful()) {
                    callback.onFailure(response.code() == 404 ? Failure.NOT_FOUND : Failure.HTTP);
                    return;
                }
                if (response.body() == null) {
                    callback.onFailure(Failure.INVALID_RESPONSE);
                    return;
                }
                List<VocabularyItem.Example> value = VocabularyMapper.mapExamples(response.body());
                examples.put(id, value);
                callback.onSuccess(value, false);
            }
            @Override public void onFailure(@NonNull Call<List<VocabularyDto.ExampleDto>> request,
                                            @NonNull Throwable failure) {
                if (!request.isCanceled()) callback.onFailure(Failure.OFFLINE);
            }
        });
        return call::cancel;
    }

    @Override
    public Cancelable topics(boolean online, Callback<List<VocabularyItem.Topic>> callback) {
        if (!online) {
            if (topicCache == null) callback.onFailure(Failure.OFFLINE);
            else callback.onSuccess(topicCache, true);
            return NO_OP;
        }
        TopicLoad load = new TopicLoad(callback);
        load.next(0);
        return load;
    }

    private final class TopicLoad implements Cancelable {
        private final Callback<List<VocabularyItem.Topic>> callback;
        private final List<VocabularyItem.Topic> values = new ArrayList<>();
        private Call<VocabularyDto.TopicPageDto> active;
        private boolean canceled;

        TopicLoad(Callback<List<VocabularyItem.Topic>> callback) { this.callback = callback; }

        void next(int page) {
            active = api.topics(page, 100);
            active.enqueue(new retrofit2.Callback<VocabularyDto.TopicPageDto>() {
                @Override public void onResponse(@NonNull Call<VocabularyDto.TopicPageDto> request,
                                                  @NonNull Response<VocabularyDto.TopicPageDto> response) {
                    if (canceled) return;
                    VocabularyDto.TopicPageDto body = response.body();
                    if (!response.isSuccessful() || body == null || body.content == null) {
                        callback.onFailure(Failure.HTTP);
                        return;
                    }
                    for (VocabularyDto.TopicDto topic : body.content) {
                        if (topic != null && topic.id != null && topic.name != null) {
                            values.add(new VocabularyItem.Topic(topic.id, topic.name));
                        }
                    }
                    if (body.hasNext) next(page + 1);
                    else {
                        topicCache = new ArrayList<>(values);
                        callback.onSuccess(topicCache, false);
                    }
                }
                @Override public void onFailure(@NonNull Call<VocabularyDto.TopicPageDto> request,
                                                @NonNull Throwable failure) {
                    if (!canceled && !request.isCanceled()) callback.onFailure(Failure.OFFLINE);
                }
            });
        }

        @Override public void cancel() {
            canceled = true;
            if (active != null) active.cancel();
        }
    }

    private static String emptyToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private static <T> Map<String, T> boundedCache(int size) {
        return new LinkedHashMap<String, T>(size, 0.75f, true) {
            @Override protected boolean removeEldestEntry(Map.Entry<String, T> eldest) {
                return size() > size;
            }
        };
    }
}
