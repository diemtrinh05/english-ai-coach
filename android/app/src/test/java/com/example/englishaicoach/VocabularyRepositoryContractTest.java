package com.example.englishaicoach;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.englishaicoach.data.api.VocabularyApi;
import com.example.englishaicoach.data.repository.RetrofitVocabularyRepository;
import com.example.englishaicoach.domain.model.VocabularyItem;
import com.example.englishaicoach.domain.repository.VocabularyRepository;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class VocabularyRepositoryContractTest {
    private MockWebServer server;
    private RetrofitVocabularyRepository repository;

    @Before public void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        VocabularyApi api = new Retrofit.Builder().baseUrl(server.url("/api/v1/"))
                .addConverterFactory(GsonConverterFactory.create()).build()
                .create(VocabularyApi.class);
        repository = new RetrofitVocabularyRepository(api);
    }

    @After public void tearDown() throws IOException { server.shutdown(); }

    @Test public void listUsesApprovedQueryAndOfflineShowsOnlyCachedPage() throws Exception {
        server.enqueue(json("{\"content\":[{\"id\":\"11111111-1111-1111-1111-111111111111\","
                + "\"word\":\"abandon\",\"cefr\":\"B1\",\"meaningVi\":\"từ bỏ\"}],"
                + "\"page\":0,\"size\":20,\"hasNext\":true}"));
        VocabularyRepository.Query query = new VocabularyRepository.Query(0, 20,
                "abandon", "B1", "22222222-2222-2222-2222-222222222222", "verb");
        Capture<VocabularyRepository.Page> online = new Capture<>();
        repository.list(query, true, online);
        assertTrue(online.done.await(5, TimeUnit.SECONDS));
        assertNotNull(online.value);
        assertEquals("abandon", online.value.items.get(0).word);
        assertTrue(online.value.hasNext);
        RecordedRequest request = server.takeRequest(5, TimeUnit.SECONDS);
        assertNotNull(request);
        assertEquals("/api/v1/vocabulary?page=0&size=20&search=abandon&cefr=B1"
                + "&topicId=22222222-2222-2222-2222-222222222222&partOfSpeech=verb&sort=word%2Casc",
                request.getPath());

        Capture<VocabularyRepository.Page> cached = new Capture<>();
        repository.list(query, false, cached);
        assertTrue(cached.done.await(1, TimeUnit.SECONDS));
        assertTrue(cached.fromCache);
        assertEquals("abandon", cached.value.items.get(0).word);
        assertEquals(1, server.getRequestCount());

        Capture<VocabularyRepository.Page> unavailable = new Capture<>();
        repository.list(new VocabularyRepository.Query(1, 20, "abandon", "B1", null, null),
                false, unavailable);
        assertTrue(unavailable.done.await(1, TimeUnit.SECONDS));
        assertEquals(VocabularyRepository.Failure.OFFLINE, unavailable.failure);
    }

    @Test public void detailAndExamplesUseExactFieldsAndReadCache() throws Exception {
        String id = "11111111-1111-1111-1111-111111111111";
        server.enqueue(json("{\"id\":\"" + id + "\",\"word\":\"abandon\",\"cefr\":\"B1\","
                + "\"phoneticIpa\":\"/əˈbændən/\",\"meaningVi\":\"từ bỏ\","
                + "\"topics\":[{\"id\":\"2\",\"name\":\"Daily Life\"}],"
                + "\"examples\":[{\"id\":\"3\",\"exampleText\":\"He abandoned it.\","
                + "\"translationText\":\"Anh ấy từ bỏ nó.\",\"source\":\"MANUAL\"}]}"));
        server.enqueue(json("[{\"id\":\"3\",\"exampleText\":\"He abandoned it.\","
                + "\"translationText\":\"Anh ấy từ bỏ nó.\",\"source\":\"MANUAL\"}]"));
        Capture<VocabularyItem> detail = new Capture<>();
        repository.detail(id, true, detail);
        assertTrue(detail.done.await(5, TimeUnit.SECONDS));
        assertEquals("từ bỏ", detail.value.meaningVi);
        assertEquals("/api/v1/vocabulary/" + id, server.takeRequest().getPath());
        Capture<List<VocabularyItem.Example>> examples = new Capture<>();
        repository.examples(id, true, examples);
        assertTrue(examples.done.await(5, TimeUnit.SECONDS));
        assertEquals("He abandoned it.", examples.value.get(0).text);
        assertEquals("MANUAL", examples.value.get(0).source);
        assertEquals("/api/v1/vocabulary/" + id + "/examples", server.takeRequest().getPath());
        Capture<VocabularyItem> cached = new Capture<>();
        repository.detail(id, false, cached);
        assertTrue(cached.done.await(1, TimeUnit.SECONDS));
        assertTrue(cached.fromCache);
        assertFalse(cached.value.examples.isEmpty());
        assertEquals(2, server.getRequestCount());
    }

    @Test public void topicsFilterLoadsAllPagesBeforeSelection() throws Exception {
        server.enqueue(json("{\"content\":[{\"id\":\"a\",\"name\":\"Travel\"}],"
                + "\"hasNext\":true}"));
        server.enqueue(json("{\"content\":[{\"id\":\"b\",\"name\":\"Business\"}],"
                + "\"hasNext\":false}"));
        Capture<List<VocabularyItem.Topic>> topics = new Capture<>();
        repository.topics(true, topics);
        assertTrue(topics.done.await(5, TimeUnit.SECONDS));
        assertEquals(2, topics.value.size());
        assertEquals("/api/v1/topics?page=0&size=100", server.takeRequest().getPath());
        assertEquals("/api/v1/topics?page=1&size=100", server.takeRequest().getPath());
        Capture<List<VocabularyItem.Topic>> cached = new Capture<>();
        repository.topics(false, cached);
        assertTrue(cached.fromCache);
        assertEquals("Business", cached.value.get(1).name);
    }

    @Test public void cacheKeyDistinguishesNullLiteralAndSeparators() {
        VocabularyRepository.Query absent = new VocabularyRepository.Query(0, 20,
                null, null, null, null);
        VocabularyRepository.Query literal = new VocabularyRepository.Query(0, 20,
                "null", null, null, null);
        VocabularyRepository.Query separator = new VocabularyRepository.Query(0, 20,
                "a|b", "B1", null, null);
        VocabularyRepository.Query ordinary = new VocabularyRepository.Query(0, 20,
                "a", "b|B1", null, null);
        assertFalse(absent.cacheKey().equals(literal.cacheKey()));
        assertFalse(separator.cacheKey().equals(ordinary.cacheKey()));
    }

    private static MockResponse json(String body) {
        return new MockResponse().setResponseCode(200)
                .addHeader("Content-Type", "application/json").setBody(body);
    }

    private static final class Capture<T> implements VocabularyRepository.Callback<T> {
        final CountDownLatch done = new CountDownLatch(1);
        volatile T value;
        volatile boolean fromCache;
        volatile VocabularyRepository.Failure failure;
        @Override public void onSuccess(T value, boolean fromCache) {
            this.value = value;
            this.fromCache = fromCache;
            done.countDown();
        }
        @Override public void onFailure(VocabularyRepository.Failure failure) {
            this.failure = failure;
            done.countDown();
        }
    }
}
