package com.example.englishaicoach.feature.review;
import static org.junit.Assert.*;
import androidx.arch.core.executor.*;
import com.example.englishaicoach.core.auth.*;
import com.example.englishaicoach.core.network.*;
import com.example.englishaicoach.domain.model.*;
import com.example.englishaicoach.domain.repository.*;
import com.example.englishaicoach.domain.usecase.*;
import org.junit.*;
import java.util.*;
public final class ReviewViewModelTest {
    SessionManager sessions; Fake repository; ReviewViewModel model;
    @Before public void setup() {
        ArchTaskExecutor.getInstance().setDelegate(new TaskExecutor() {
            public void executeOnDiskIO(Runnable task) {task.run();} public void postToMainThread(Runnable task) {task.run();} public boolean isMainThread(){return true;}
        });
        sessions=new SessionManager(new TokenStore(){TokenSession value;public TokenSession read(){return value;}public void write(TokenSession value){this.value=value;}});
        sessions.start("a","r");repository=new Fake();model=new ReviewViewModel(sessions,new ReviewLearningUseCase(repository),Runnable::run);
    }
    @After public void cleanup(){model.onCleared();ArchTaskExecutor.getInstance().setDelegate(null);}
    void activate(){model.authenticated(true);model.online(true);model.resume();}
    @Test public void preservesServerOrderAndEmptyState(){
        activate();List<ReviewItem> rows=List.of(new ReviewItem("b","second","REVIEWING","date"),new ReviewItem("a","first","LEARNING","date"));
        repository.callback.completed(rows,null);assertEquals(rows,model.state().getValue().items);assertTrue(model.canOpen());
        model.refresh();repository.callback.completed(List.of(),null);assertEquals(ReviewViewModel.Phase.EMPTY,model.state().getValue().phase);assertFalse(model.canOpen());
    }
    @Test public void reloadsWhenReturningAndOfflineCannotStart(){
        activate();repository.callback.completed(List.of(new ReviewItem("x","w","NEW","d")),null);
        model.pause();model.resume();assertEquals(2,repository.calls);model.online(false);assertFalse(model.canOpen());
        repository.callback.completed(List.of(new ReviewItem("x","w","NEW","d")),null);assertEquals(ReviewViewModel.Phase.OFFLINE,model.state().getValue().phase);
        model.online(true);assertEquals(3,repository.calls);
    }
    @Test public void errorsAreControlledAndExplicitRetryDoesNotCreateMutation(){activate();repository.callback.completed(null,new ApiErrorParser().parse(503,null));assertEquals(ReviewViewModel.Phase.ERROR,model.state().getValue().phase);model.refresh();assertEquals(2,repository.calls);}
    @Test public void switchedOwnerClearsPrivateListAndIgnoresLateResult(){activate();var callback=repository.callback;sessions.start("b","r");callback.completed(List.of(new ReviewItem("x","private","NEW","d")),null);assertEquals(ReviewViewModel.Phase.EXPIRED,model.state().getValue().phase);assertTrue(model.state().getValue().items.isEmpty());model.refresh();assertEquals(1,repository.calls);}
    static final class Fake implements ReviewLearningRepository {
        int calls;Callback<List<ReviewItem>> callback;
        public LearningSessionRepository.Cancelable reviews(String owner,Callback<List<ReviewItem>> callback){calls++;this.callback=callback;return ()->{};}
        public LearningSessionRepository.Cancelable progress(String owner,String vocabulary,Callback<VocabularyProgress> callback){throw new AssertionError();}
        public LearningSessionRepository.Cancelable complete(String owner,String session,String event,Callback<LearningSummary> callback){throw new AssertionError();}
        public LearningSessionRepository.Cancelable summary(String owner,String session,Callback<LearningSummary> callback){throw new AssertionError();}
    }
}
