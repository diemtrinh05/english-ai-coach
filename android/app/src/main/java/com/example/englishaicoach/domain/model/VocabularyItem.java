package com.example.englishaicoach.domain.model;

import java.util.Collections;
import java.util.List;

public final class VocabularyItem {
    public final String id;
    public final String word;
    public final String phoneticIpa;
    public final String meaningVi;
    public final String meaningEn;
    public final String partOfSpeech;
    public final String cefr;
    public final List<Topic> topics;
    public final String audioUrl;
    public final List<Example> examples;

    public VocabularyItem(String id, String word, String phoneticIpa, String meaningVi,
                          String meaningEn, String partOfSpeech, String cefr,
                          List<Topic> topics, String audioUrl, List<Example> examples) {
        this.id = id;
        this.word = word;
        this.phoneticIpa = phoneticIpa;
        this.meaningVi = meaningVi;
        this.meaningEn = meaningEn;
        this.partOfSpeech = partOfSpeech;
        this.cefr = cefr;
        this.topics = Collections.unmodifiableList(topics);
        this.audioUrl = audioUrl;
        this.examples = Collections.unmodifiableList(examples);
    }

    public static final class Topic {
        public final String id;
        public final String name;
        public Topic(String id, String name) { this.id = id; this.name = name; }
    }

    public static final class Example {
        public final String id;
        public final String text;
        public final String translation;
        public final String source;
        public Example(String id, String text, String translation, String source) {
            this.id = id;
            this.text = text;
            this.translation = translation;
            this.source = source;
        }
    }
}
