package com.example.englishaicoach.feature.vocabulary;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.arch.core.executor.TaskExecutor;

import com.example.englishaicoach.core.ui.UiState;
import com.example.englishaicoach.domain.model.VocabularyItem;
import com.example.englishaicoach.domain.repository.VocabularyRepository;
import com.example.englishaicoach.domain.usecase.BrowseVocabularyUseCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class VocabularyViewModelStateTest {
    private FakeRepository repository;
    private BrowseVocabularyUseCase useCase;

    @Before public void setUp() {
        ArchTaskExecutor.getInstance().setDelegate(new TaskExecutor() {
            @Override public void executeOnDiskIO(Runnable runnable) { runnable.run(); }
            @Override public void postToMainThread(Runnable runnable) { runnable.run(); }
            @Override public boolean isMainThread() { return true; }
        });
        repository = new FakeRepository();
        useCase = new BrowseVocabularyUseCase(repository);
    }

    @After public void tearDown() { ArchTaskExecutor.getInstance().setDelegate(null); }

    @Test public void listKeepsLoadedPageWhenNextPageFailsAndRetryAppendsOnce() {
        VocabularyListViewModel viewModel = new VocabularyListViewModel(useCase);
        assertStatus(UiState.Status.INITIAL, viewModel.state().getValue());
        viewModel.search("ab", "B1", null, null);
        assertStatus(UiState.Status.LOADING, viewModel.state().getValue());
        FakeRepository.Request<VocabularyRepository.Page> first = repository.pages.get(0);
        assertEquals(0, first.query.page);
        assertEquals("ab", first.query.search);
        first.succeed(new VocabularyRepository.Page(Collections.singletonList(item("one")),
                0, true));
        assertStatus(UiState.Status.SUCCESS, viewModel.state().getValue());
        assertEquals(1, viewModel.state().getValue().getData().size());

        viewModel.loadNext();
        assertTrue(viewModel.loadingMore().getValue());
        FakeRepository.Request<VocabularyRepository.Page> failed = repository.pages.get(1);
        assertEquals(1, failed.query.page);
        failed.fail(VocabularyRepository.Failure.HTTP);
        assertFalse(viewModel.loadingMore().getValue());
        assertTrue(viewModel.nextPageFailed().getValue());
        assertEquals("one", viewModel.state().getValue().getData().get(0).id);

        viewModel.retry();
        FakeRepository.Request<VocabularyRepository.Page> retry = repository.pages.get(2);
        assertEquals(1, retry.query.page);
        retry.succeed(new VocabularyRepository.Page(Collections.singletonList(item("two")),
                1, false));
        assertStatus(UiState.Status.SUCCESS, viewModel.state().getValue());
        assertEquals(2, viewModel.state().getValue().getData().size());
        assertEquals("two", viewModel.state().getValue().getData().get(1).id);
        assertFalse(viewModel.hasNext());
        assertFalse(viewModel.nextPageFailed().getValue());
    }

    @Test public void listIgnoresLateResponseFromPreviousSearchAndShowsEmptyErrorOffline() {
        VocabularyListViewModel viewModel = new VocabularyListViewModel(useCase);
        viewModel.search("old", null, null, null);
        FakeRepository.Request<VocabularyRepository.Page> old = repository.pages.get(0);
        viewModel.search("new", null, null, null);
        assertTrue(old.canceled);
        FakeRepository.Request<VocabularyRepository.Page> newer = repository.pages.get(1);
        old.succeed(new VocabularyRepository.Page(Collections.singletonList(item("old")),
                0, false));
        assertStatus(UiState.Status.LOADING, viewModel.state().getValue());
        newer.succeed(new VocabularyRepository.Page(Collections.singletonList(item("new")),
                0, false));
        assertEquals("new", viewModel.state().getValue().getData().get(0).id);

        viewModel.search("empty", null, null, null);
        repository.pages.get(2).succeed(new VocabularyRepository.Page(
                Collections.emptyList(), 0, false));
        assertStatus(UiState.Status.EMPTY, viewModel.state().getValue());

        viewModel.search("error", null, null, null);
        repository.pages.get(3).fail(VocabularyRepository.Failure.HTTP);
        assertStatus(UiState.Status.ERROR, viewModel.state().getValue());

        viewModel.search("offline", null, null, null);
        repository.pages.get(4).fail(VocabularyRepository.Failure.OFFLINE);
        assertStatus(UiState.Status.OFFLINE, viewModel.state().getValue());
    }

    @Test public void detailAndExamplesHaveIndependentSuccessEmptyErrorOfflineStates() {
        VocabularyDetailViewModel viewModel = new VocabularyDetailViewModel(useCase);
        viewModel.load("one");
        assertStatus(UiState.Status.LOADING, viewModel.detail().getValue());
        assertStatus(UiState.Status.LOADING, viewModel.examples().getValue());
        repository.details.get(0).succeed(item("one"));
        repository.examples.get(0).succeed(Collections.singletonList(example("first")));
        assertStatus(UiState.Status.SUCCESS, viewModel.detail().getValue());
        assertStatus(UiState.Status.SUCCESS, viewModel.examples().getValue());
        assertEquals("first", viewModel.examples().getValue().getData().get(0).text);

        viewModel.load("two");
        repository.details.get(1).succeed(item("two"));
        repository.examples.get(1).succeed(Collections.emptyList());
        assertStatus(UiState.Status.SUCCESS, viewModel.detail().getValue());
        assertStatus(UiState.Status.EMPTY, viewModel.examples().getValue());

        viewModel.load("three");
        repository.details.get(2).fail(VocabularyRepository.Failure.NOT_FOUND);
        repository.examples.get(2).fail(VocabularyRepository.Failure.HTTP);
        assertStatus(UiState.Status.ERROR, viewModel.detail().getValue());
        assertStatus(UiState.Status.ERROR, viewModel.examples().getValue());

        viewModel.setOnline(false);
        viewModel.retry();
        assertFalse(repository.details.get(3).online);
        assertFalse(repository.examples.get(3).online);
        repository.details.get(3).fail(VocabularyRepository.Failure.OFFLINE);
        repository.examples.get(3).fail(VocabularyRepository.Failure.OFFLINE);
        assertStatus(UiState.Status.OFFLINE, viewModel.detail().getValue());
        assertStatus(UiState.Status.OFFLINE, viewModel.examples().getValue());
    }

    @Test public void detailIgnoresLateCallbacksAfterAnotherWordLoads() {
        VocabularyDetailViewModel viewModel = new VocabularyDetailViewModel(useCase);
        viewModel.load("old");
        FakeRepository.Request<VocabularyItem> oldDetail = repository.details.get(0);
        FakeRepository.Request<List<VocabularyItem.Example>> oldExamples = repository.examples.get(0);
        viewModel.load("new");
        assertTrue(oldDetail.canceled);
        assertTrue(oldExamples.canceled);
        oldDetail.succeed(item("old"));
        oldExamples.succeed(Collections.singletonList(example("old")));
        assertStatus(UiState.Status.LOADING, viewModel.detail().getValue());
        assertStatus(UiState.Status.LOADING, viewModel.examples().getValue());
        repository.details.get(1).succeed(item("new"));
        repository.examples.get(1).succeed(Collections.singletonList(example("new")));
        assertEquals("new", viewModel.detail().getValue().getData().id);
        assertEquals("new", viewModel.examples().getValue().getData().get(0).text);
    }

    private static void assertStatus(UiState.Status expected, UiState<?> actual) {
        assertEquals(expected, actual.getStatus());
    }

    private static VocabularyItem item(String id) {
        return new VocabularyItem(id, id, null, "nghĩa", null, null, "B1",
                Collections.emptyList(), null, Collections.emptyList());
    }

    private static VocabularyItem.Example example(String text) {
        return new VocabularyItem.Example(text, text, null, "MANUAL");
    }

    private static final class FakeRepository implements VocabularyRepository {
        final List<Request<Page>> pages = new ArrayList<>();
        final List<Request<VocabularyItem>> details = new ArrayList<>();
        final List<Request<List<VocabularyItem.Example>>> examples = new ArrayList<>();

        @Override public Cancelable list(Query query, boolean online, Callback<Page> callback) {
            Request<Page> request = new Request<>(query, online, callback);
            pages.add(request);
            return request;
        }

        @Override public Cancelable detail(String id, boolean online,
                                           Callback<VocabularyItem> callback) {
            Request<VocabularyItem> request = new Request<>(null, online, callback);
            details.add(request);
            return request;
        }

        @Override public Cancelable examples(String id, boolean online,
                                             Callback<List<VocabularyItem.Example>> callback) {
            Request<List<VocabularyItem.Example>> request = new Request<>(null, online, callback);
            examples.add(request);
            return request;
        }

        @Override public Cancelable topics(boolean online,
                                           Callback<List<VocabularyItem.Topic>> callback) {
            return () -> { };
        }

        static final class Request<T> implements Cancelable {
            final Query query;
            final boolean online;
            final Callback<T> callback;
            boolean canceled;

            Request(Query query, boolean online, Callback<T> callback) {
                this.query = query;
                this.online = online;
                this.callback = callback;
            }

            @Override public void cancel() { canceled = true; }
            void succeed(T value) { callback.onSuccess(value, false); }
            void fail(Failure failure) { callback.onFailure(failure); }
        }
    }
}
