package com.example.englishaicoach.data.dto;

import java.util.List;

public final class VocabularyDto {
    public String id;
    public String word;
    public String phoneticIpa;
    public String meaningVi;
    public String meaningEn;
    public String partOfSpeech;
    public String cefr;
    public List<TopicDto> topics;
    public String audioUrl;
    public String imageUrl;
    public List<ExampleDto> examples;

    public static final class TopicDto {
        public String id;
        public String name;
    }

    public static final class TopicPageDto {
        public List<TopicDto> content;
        public boolean hasNext;
    }

    public static final class ExampleDto {
        public String id;
        public String exampleText;
        public String translationText;
        public String source;
    }

    public static final class PageDto {
        public List<VocabularyDto> content;
        public int page;
        public int size;
        public long totalElements;
        public int totalPages;
        public boolean hasNext;
    }
}
