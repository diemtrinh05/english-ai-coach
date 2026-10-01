package com.example.englishaicoach.data.mapper;

import com.example.englishaicoach.data.dto.VocabularyDto;
import com.example.englishaicoach.domain.model.VocabularyItem;

import java.util.ArrayList;
import java.util.List;

public final class VocabularyMapper {
    private VocabularyMapper() { }

    public static VocabularyItem map(VocabularyDto dto) {
        if (dto == null || dto.id == null || dto.word == null || dto.cefr == null) {
            throw new IllegalArgumentException("Invalid vocabulary response");
        }
        List<VocabularyItem.Topic> topics = new ArrayList<>();
        if (dto.topics != null) {
            for (VocabularyDto.TopicDto topic : dto.topics) {
                if (topic != null && topic.id != null && topic.name != null) {
                    topics.add(new VocabularyItem.Topic(topic.id, topic.name));
                }
            }
        }
        return new VocabularyItem(dto.id, dto.word, dto.phoneticIpa, dto.meaningVi,
                dto.meaningEn, dto.partOfSpeech, dto.cefr, topics, dto.audioUrl,
                mapExamples(dto.examples));
    }

    public static List<VocabularyItem.Example> mapExamples(List<VocabularyDto.ExampleDto> source) {
        List<VocabularyItem.Example> examples = new ArrayList<>();
        if (source != null) {
            for (VocabularyDto.ExampleDto dto : source) {
                if (dto != null && dto.id != null && dto.exampleText != null && dto.source != null) {
                    examples.add(new VocabularyItem.Example(dto.id, dto.exampleText,
                            dto.translationText, dto.source));
                }
            }
        }
        return examples;
    }
}
