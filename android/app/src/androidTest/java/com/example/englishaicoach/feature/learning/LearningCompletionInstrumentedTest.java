package com.example.englishaicoach.feature.learning;
import static org.junit.Assert.*;
import android.content.Intent;
import android.view.*;
import android.widget.*;
import androidx.fragment.app.Fragment;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.example.englishaicoach.*;
import com.example.englishaicoach.core.auth.AuthTestDependencies;
import com.example.englishaicoach.core.network.*;
import com.example.englishaicoach.domain.model.*;
import com.example.englishaicoach.domain.repository.*;
import com.example.englishaicoach.domain.usecase.*;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import okhttp3.ResponseBody;
import org.junit.*;
import org.junit.runner.RunWith;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;
@RunWith(AndroidJUnit4.class)
public final class LearningCompletionInstrumentedTest {
    static final String ID="00000000-0000-0000-0000-000000000001";
    MainActivity activity;AuthTestDependencies auth;Data fake;Start start;LearningAttemptInstrumentedTest.AttemptFake attempt;
    final Map<Field,Object> previous=new HashMap<>();
    @Before public void setup()throws Exception{
        auth=new AuthTestDependencies(true);fake=new Data();start=new Start();attempt=new LearningAttemptInstrumentedTest.AttemptFake();
        inject("reviewUseCase",new ReviewLearningUseCase(fake));inject("useCase",new OpenLearningSessionUseCase(start));inject("attemptUseCase",new SubmitLearningAttemptUseCase(attempt));
        var i=InstrumentationRegistry.getInstrumentation();activity=(MainActivity)i.startActivitySync(new Intent(i.getTargetContext(),MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));i.waitForIdleSync();
        ui(()->((BottomNavigationView)activity.findViewById(R.id.main_navigation)).setSelectedItemId(R.id.nav_review));
        for(int n=0;n<40&&fake.reviewCalls==0;n++){Thread.sleep(100);i.waitForIdleSync();}
        assertEquals(1,fake.reviewCalls);
    }
    void inject(String name,Object value)throws Exception{Field field=LearningDependencies.class.getDeclaredField(name);field.setAccessible(true);previous.put(field,field.get(null));field.set(null,value);}
    @After public void cleanup()throws Exception{if(activity!=null)ui(()->activity.finish());for(var entry:previous.entrySet())entry.getKey().set(null,entry.getValue());auth.close();}
    void ui(Runnable task){var i=InstrumentationRegistry.getInstrumentation();i.runOnMainSync(task);i.waitForIdleSync();}
    TextView status(){return activity.findViewById(R.id.learning_status);}
    ApiError error(int status,String code){return new ApiErrorParser().parse(status,ResponseBody.create(null,"{\"code\":\""+code+"\"}"));}
    void open() {
        ui(()->fake.reviews.completed(List.of(new ReviewItem(ID,"negotiate","REVIEWING","2026-10-18T00:00:00Z")),null));
        assertTrue(((TextView)activity.findViewById(R.id.review_status)).getText().toString().contains("1"));
        ui(()->((ViewGroup)activity.findViewById(R.id.review_rows)).getChildAt(1).performClick());
        assertEquals("REVIEW",start.type);assertEquals(1,start.loads);
        ui(()->start.callback.completed(new LearningSessionRepository.Result(new LearningSession(ID,"REVIEW","2026-10-18T00:00:00Z",null),
                new VocabularyItem(ID,"negotiate",null,"đàm phán","discuss to agree","VERB","B2",List.of(),null,List.of()),null)));
    }
    void accepted(){open();ui(()->activity.findViewById(R.id.learning_reveal_button).performClick());ui(()->activity.findViewById(R.id.learning_quality_3).performClick());ui(()->attempt.callback.completed(new LearningAttempt(ID,true,3,"2026-10-19T00:00:00Z",1),null));assertEquals(1,fake.progressCalls);}
    LearningSummary summary(){return new LearningSummary(ID,"REVIEW","2026-10-18T00:00:00Z","2026-10-18T00:01:00Z",1,new BigDecimal("100.00"));}
    void recreate(){ui(()->{Fragment old=activity.getSupportFragmentManager().findFragmentById(R.id.main_content);Fragment.SavedState saved=activity.getSupportFragmentManager().saveFragmentInstanceState(old);LearningSessionFragment replacement=LearningSessionFragment.forVocabulary(ID);replacement.setInitialSavedState(saved);activity.getSupportFragmentManager().beginTransaction().replace(R.id.main_content,replacement,"LEARNING").commitNow();});}
    @Test public void reviewAttemptPartialReadFailureStillCompletesAndReturnsToReloadedQueue(){
        accepted();ui(()->fake.progress.completed(null,error(503,"")));assertEquals(View.VISIBLE,activity.findViewById(R.id.learning_progress_retry).getVisibility());
        ui(()->activity.findViewById(R.id.learning_complete_button).performClick());ui(()->activity.findViewById(R.id.learning_complete_button).performClick());assertEquals(1,fake.events.size());
        ui(()->fake.complete.completed(summary(),null));assertTrue(status().getText().toString().contains("100.00"));assertTrue(status().getText().toString().contains("2026-10-18T00:01:00Z"));assertEquals(1,attempt.requests.size());
        ui(()->activity.getSupportFragmentManager().popBackStackImmediate());assertEquals(2,fake.reviewCalls);
    }
    @Test public void pendingSavedStateDoesNotPostUntilExplicitSameEventRetry(){
        accepted();ui(()->activity.findViewById(R.id.learning_complete_button).performClick());String event=fake.events.get(0);recreate();assertEquals(1,fake.events.size());assertEquals(1,start.loads);
        ui(()->activity.findViewById(R.id.learning_retry_button).performClick());assertEquals(List.of(event,event),fake.events);assertEquals(0,fake.summaryCalls);
    }
    @Test public void acceptedMarkerRefreshesProgressAndRetainsCompletionWithoutResubmission(){
        accepted();recreate();assertEquals(2,fake.progressCalls);assertEquals(1,attempt.requests.size());assertEquals(View.GONE,activity.findViewById(R.id.learning_quality_selector).getVisibility());
        ui(()->activity.findViewById(R.id.learning_complete_button).performClick());assertEquals(1,fake.events.size());ui(()->fake.complete.completed(summary(),null));assertTrue(status().getText().toString().contains("100.00"));
    }
    @Test public void completedMarkerRestoresBySummaryGetOnly(){
        accepted();ui(()->activity.findViewById(R.id.learning_complete_button).performClick());ui(()->fake.complete.completed(summary(),null));recreate();assertEquals(1,fake.summaryCalls);assertEquals(1,fake.events.size());
        ui(()->fake.summary.completed(null,error(503,"")));ui(()->activity.findViewById(R.id.learning_retry_button).performClick());assertEquals(2,fake.summaryCalls);assertEquals(1,fake.events.size());
        ui(()->fake.summary.completed(summary(),null));assertTrue(status().getText().toString().contains("100.00"));
    }
    @Test public void completionReuseConflictCannotCreateReplacementEvent(){
        accepted();ui(()->activity.findViewById(R.id.learning_complete_button).performClick());ui(()->fake.complete.completed(null,error(409,"IDEMPOTENCY_KEY_REUSE")));
        assertEquals(activity.getString(R.string.learning_complete_reuse),status().getText().toString());assertEquals(View.GONE,activity.findViewById(R.id.learning_retry_button).getVisibility());recreate();assertEquals(1,fake.events.size());assertEquals(View.GONE,activity.findViewById(R.id.learning_complete_button).getVisibility());
    }
    @Test public void reviewEmptyAndErrorStatesHaveControlledRetry(){
        ui(()->fake.reviews.completed(List.of(),null));assertEquals(activity.getString(R.string.review_empty),((TextView)activity.findViewById(R.id.review_status)).getText().toString());
        ui(()->activity.findViewById(R.id.review_reload).performClick());assertEquals(2,fake.reviewCalls);ui(()->fake.reviews.completed(null,error(503,"")));assertEquals(activity.getString(R.string.review_error),((TextView)activity.findViewById(R.id.review_status)).getText().toString());
    }
    @Test public void progressRetryIsReadOnlyAndDisplaysServerSnapshot(){
        accepted();ui(()->fake.progress.completed(null,error(503,"")));ui(()->activity.findViewById(R.id.learning_progress_retry).performClick());assertEquals(2,fake.progressCalls);assertEquals(1,attempt.requests.size());
        ui(()->fake.progress.completed(new VocabularyProgress(ID,"REVIEWING",null,new BigDecimal("2.5"),17,3,4,1),null));assertTrue(((TextView)activity.findViewById(R.id.learning_progress_status)).getText().toString().contains("17"));assertEquals(View.VISIBLE,activity.findViewById(R.id.learning_complete_button).getVisibility());
    }
    static final class Start implements LearningSessionRepository{
        String type;int loads;Callback callback;
        public Cancelable load(String o,String e,String v,String s,boolean start,Callback cb){return load(o,e,v,s,start,"MIXED",cb);}
        public Cancelable load(String o,String e,String v,String s,boolean start,String type,Callback cb){this.type=type;loads++;callback=cb;return ()->{};}
    }
    static final class Data implements ReviewLearningRepository{
        int reviewCalls,progressCalls,summaryCalls;List<String> events=new ArrayList<>();Callback<List<ReviewItem>> reviews;Callback<VocabularyProgress> progress;Callback<LearningSummary> complete,summary;
        public LearningSessionRepository.Cancelable reviews(String o,Callback<List<ReviewItem>> cb){reviewCalls++;reviews=cb;return ()->{};}
        public LearningSessionRepository.Cancelable progress(String o,String v,Callback<VocabularyProgress> cb){progressCalls++;progress=cb;return ()->{};}
        public LearningSessionRepository.Cancelable complete(String o,String s,String e,Callback<LearningSummary> cb){events.add(e);complete=cb;return ()->{};}
        public LearningSessionRepository.Cancelable summary(String o,String s,Callback<LearningSummary> cb){summaryCalls++;summary=cb;return ()->{};}
    }
}
