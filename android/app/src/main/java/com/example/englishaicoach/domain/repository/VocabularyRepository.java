package com.example.englishaicoach.domain.repository;

import com.example.englishaicoach.domain.model.VocabularyItem;

import java.util.List;

public interface VocabularyRepository {
    interface Cancelable { void cancel(); }
    interface Callback<T> {
        void onSuccess(T value, boolean fromCache);
        void onFailure(Failure failure);
    }
    enum Failure { OFFLINE, NOT_FOUND, HTTP, INVALID_RESPONSE }

    final class Query {
        public final int page;
        public final int size;
        public final String search;
        public final String cefr;
        public final String topicId;
        public final String partOfSpeech;

        public Query(int page, int size, String search, String cefr,
                     String topicId, String partOfSpeech) {
            this.page = page;
            this.size = size;
            this.search = search;
            this.cefr = cefr;
            this.topicId = topicId;
            this.partOfSpeech = partOfSpeech;
        }

        public String cacheKey() {
            return page + "|" + size + "|" + keyPart(search) + keyPart(cefr)
                    + keyPart(topicId) + keyPart(partOfSpeech);
        }

        private static String keyPart(String value) {
            if (value == null) return "-1:";
            String normalized = value.trim();
            return normalized.length() + ":" + normalized;
        }
    }

    final class Page {
        public final List<VocabularyItem> items;
        public final int page;
        public final boolean hasNext;
        public Page(List<VocabularyItem> items, int page, boolean hasNext) {
            this.items = items;
            this.page = page;
            this.hasNext = hasNext;
        }
    }

    Cancelable list(Query query, boolean online, Callback<Page> callback);
    Cancelable detail(String id, boolean online, Callback<VocabularyItem> callback);
    Cancelable examples(String id, boolean online,
                        Callback<List<VocabularyItem.Example>> callback);
    Cancelable topics(boolean online, Callback<List<VocabularyItem.Topic>> callback);
}
