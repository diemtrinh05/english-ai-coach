package com.example.englishaicoach.vocabulary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class VocabularyBatchingTests {

    @Test
    void loadsTopicsAndExamplesOnceForAnEntirePage() {
        VocabularyRepository repository = mock(VocabularyRepository.class);
        VocabularyService service = new VocabularyService(repository);
        List<Vocabulary> words = IntStream.range(0, 100)
                .mapToObj(i -> new Vocabulary(UUID.randomUUID(), "word" + i, null,
                        "nghĩa " + i, null, "noun", "A1", null, null))
                .toList();
        when(repository.countActive(null, null, null, null)).thenReturn(100L);
        when(repository.findActive(null, null, null, null, "LOWER(v.word) ASC", 100, 0L))
                .thenReturn(words);
        when(repository.findTopics(anyList())).thenReturn(Map.of());
        when(repository.findExamples(anyList())).thenReturn(Map.of());

        var response = service.listVocabulary(0, 100, null, null, null, null, null);

        assertEquals(100, response.content().size());
        verify(repository, times(1)).findTopics(words.stream().map(Vocabulary::id).toList());
        verify(repository, times(1)).findExamples(words.stream().map(Vocabulary::id).toList());
    }
}
