package com.example.englishaicoach.data.repository;
import static org.junit.Assert.*;
import com.example.englishaicoach.core.auth.*;
import com.example.englishaicoach.core.network.*;
import com.example.englishaicoach.data.api.*;
import com.example.englishaicoach.domain.repository.*;
import com.example.englishaicoach.domain.model.*;
import com.google.gson.*;
import okhttp3.*;
import okhttp3.mockwebserver.*;
import org.junit.*;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
public final class ReviewLearningRepositoryTest {
    static final String ID="00000000-0000-0000-0000-000000000001", EVENT="00000000-0000-0000-0000-000000000002";
    static final String USER=LearningAttemptRepositoryTest.USER, PROGRESS=LearningAttemptRepositoryTest.PROGRESS;
    static final String SUMMARY="{\"sessionId\":\""+ID+"\",\"sessionType\":\"REVIEW\",\"startedAt\":\"2026-10-18T00:00:00Z\",\"endedAt\":\"2026-10-18T00:01:00Z\",\"wordsStudiedCount\":1,\"accuracyPercent\":0.00}";
    static final String ITEM="{\"vocabularyId\":\""+ID+"\",\"word\":\"word\",\"status\":\"REVIEWING\",\"nextReviewAt\":\"2026-10-18T00:00:00Z\"}";
    MockWebServer server;SessionManager sessions;RetrofitReviewLearningRepository repository;Retrofit retrofit;String owner;
    @Before public void setup() throws Exception {
        server=new MockWebServer();server.start();sessions=new SessionManager(new TokenStore(){TokenSession value;public TokenSession read(){return value;}public void write(TokenSession value){this.value=value;}});
        sessions.start("access-A","refresh-A");owner=sessions.current().sessionId();
        OkHttpClient client=new OkHttpClient.Builder().retryOnConnectionFailure(false).followRedirects(false)
                .addInterceptor(new AuthInterceptor(sessions)).addNetworkInterceptor(new SessionGuardNetworkInterceptor(sessions)).build();
        retrofit=new Retrofit.Builder().baseUrl(server.url("/api/v1/")).callFactory(new SessionBindingCallFactory(client,sessions)).addConverterFactory(GsonConverterFactory.create()).build();
        repository=new RetrofitReviewLearningRepository(sessions,retrofit.create(ReviewLearningApi.class),new ApiErrorParser());
    }
    @After public void cleanup() throws Exception {server.shutdown();}
    void body(String body,int status){server.enqueue(new MockResponse().setResponseCode(status).setBody(body));}
    interface Action<T>{void run(ReviewLearningRepository.Callback<T> callback);}
    ApiError failure;
    <T>T await(Action<T> action)throws Exception{CountDownLatch done=new CountDownLatch(1);AtomicReference<T> result=new AtomicReference<>();failure=null;action.run((v,e)->{result.set(v);failure=e;done.countDown();});assertTrue(done.await(6,TimeUnit.SECONDS));return result.get();}
    @Test public void reviewsDefaultLimitNoInventedRiskOrQueryAndPreservesOrder()throws Exception{
        body(USER,200);body("{\"items\":["+ITEM+","+ITEM.replace(ID,EVENT)+"],\"count\":2}",200);
        List<ReviewItem> items=await(cb->repository.reviews(owner,cb));assertNull(failure);assertEquals(ID,items.get(0).vocabularyId);assertEquals(EVENT,items.get(1).vocabularyId);
        assertEquals("/api/v1/users/me",server.takeRequest().getPath());var get=server.takeRequest();assertEquals("/api/v1/learning/reviews",get.getPath());assertEquals("GET",get.getMethod());assertEquals("Bearer access-A",get.getHeader("Authorization"));
    }
    @Test public void emptyQueueAndIncorrectCountHaveDifferentResults()throws Exception{
        body(USER,200);body("{\"items\":[],\"count\":0}",200);List<ReviewItem> items=await(cb->repository.reviews(owner,cb));assertTrue(items.isEmpty());assertNull(failure);
        body(USER,200);body("{\"items\":["+ITEM+"],\"count\":2}",200);items=await(cb->repository.reviews(owner,cb));assertNull(items);assertEquals(502,failure.status);
    }
    @Test public void progressReadsExactResourceAndAcceptsExplicitNullNextReview()throws Exception{
        body(USER,200);body(PROGRESS.replace("\"2026-11-04T00:00:00Z\"","null"),200);VocabularyProgress result=await(cb->repository.progress(owner,ID,cb));assertNull(failure);assertNull(result.nextReviewAt);assertEquals(17,result.intervalDays);server.takeRequest();assertEquals("/api/v1/vocabulary/"+ID+"/progress",server.takeRequest().getPath());assertEquals(2,server.getRequestCount());
    }
    @Test public void completeHasOnlyBodyEventNoOpenGetNoAutomaticRetry()throws Exception{
        body(USER,200);body("{}",503);LearningSummary result=await(cb->repository.complete(owner,ID,EVENT,cb));assertNull(result);assertEquals(503,failure.status);
        assertEquals("/api/v1/users/me",server.takeRequest().getPath());var post=server.takeRequest();String first=post.getBody().readUtf8();assertEquals("POST",post.getMethod());assertEquals("/api/v1/learning/sessions/"+ID+"/complete",post.getPath());assertNull(post.getHeader("Idempotency-Key"));assertEquals(Set.of("eventId"),JsonParser.parseString(first).getAsJsonObject().keySet());
        body(USER,200);body(SUMMARY,200);result=await(cb->repository.complete(owner,ID,EVENT,cb));server.takeRequest();assertEquals(first,server.takeRequest().getBody().readUtf8());assertNull(failure);assertEquals(1,result.wordsStudiedCount);assertEquals(4,server.getRequestCount());
    }
    @Test public void completedRestoreFetchesSummaryWithoutPost()throws Exception{
        body(USER,200);body(SUMMARY,200);LearningSummary result=await(cb->repository.summary(owner,ID,cb));assertEquals("REVIEW",result.sessionType);assertEquals("0.00",result.accuracyPercent.toPlainString());server.takeRequest();var get=server.takeRequest();assertEquals("GET",get.getMethod());assertEquals("/api/v1/learning/sessions/"+ID,get.getPath());
    }
    @Test public void malformedSummaryAndMismatchedProgressNeverProduceSuccess()throws Exception{
        body(USER,200);body(SUMMARY.replace("\"endedAt\":\"2026-10-18T00:01:00Z\"","\"endedAt\":null"),200);LearningSummary result=await(cb->repository.complete(owner,ID,EVENT,cb));assertNull(result);assertEquals(502,failure.status);
        body(USER,200);body(PROGRESS.replace(ID,EVENT),200);VocabularyProgress progress=await(cb->repository.progress(owner,ID,cb));assertNull(progress);assertEquals(502,failure.status);
    }
    @Test public void reuse409AndReadFailureRemainControlled()throws Exception{
        body(USER,200);body("{\"code\":\"IDEMPOTENCY_KEY_REUSE\"}",409);LearningSummary result=await(cb->repository.complete(owner,ID,EVENT,cb));assertNull(result);assertTrue(failure.isIdempotencyKeyReuse());
        body(USER,200);body("{}",404);VocabularyProgress progress=await(cb->repository.progress(owner,ID,cb));assertNull(progress);assertEquals(404,failure.status);assertEquals(4,server.getRequestCount());
    }
    @Test public void nonUserAndOldOwnerCannotCompleteOrReadPrivateQueue()throws Exception{
        body(USER.replace("USER","ADMIN"),200);LearningSummary result=await(cb->repository.complete(owner,ID,EVENT,cb));assertNull(result);assertEquals(403,failure.status);assertEquals(1,server.getRequestCount());
        sessions.start("B","r");repository.reviews(owner,(v,e)->fail("Không callback phiên cũ"));repository.complete(owner,ID,EVENT,(v,e)->fail("Không callback phiên cũ"));assertEquals(1,server.getRequestCount());
    }
    @Test public void switchDuringAuthNeverPostsOldOperationUnderNewOwner()throws Exception{
        CountDownLatch reached=new CountDownLatch(1),allow=new CountDownLatch(1),done=new CountDownLatch(1);
        server.setDispatcher(new okhttp3.mockwebserver.Dispatcher(){public MockResponse dispatch(RecordedRequest request)throws InterruptedException{reached.countDown();assertTrue(allow.await(5,TimeUnit.SECONDS));return new MockResponse().setBody(USER);}});
        repository.complete(owner,ID,EVENT,(v,e)->done.countDown());assertTrue(reached.await(5,TimeUnit.SECONDS));sessions.start("B","r");allow.countDown();assertFalse(done.await(300,TimeUnit.MILLISECONDS));assertEquals(1,server.getRequestCount());
    }
    @Test public void reviewStartDispatchesReviewAndValidatesReturnedType()throws Exception{
        RetrofitLearningSessionRepository start=new RetrofitLearningSessionRepository(sessions,retrofit.create(LearningSessionApi.class),retrofit.create(VocabularyApi.class),new ApiErrorParser());
        body(USER,200);body(LearningAttemptRepositoryTest.SESSION,201);CountDownLatch done=new CountDownLatch(1);AtomicReference<ApiError> error=new AtomicReference<>();
        start.load(owner,EVENT,ID,null,true,"REVIEW",result->{error.set(result.error);done.countDown();});assertTrue(done.await(5,TimeUnit.SECONDS));assertEquals(502,error.get().status);server.takeRequest();var post=server.takeRequest();var json=JsonParser.parseString(post.getBody().readUtf8()).getAsJsonObject();assertEquals("REVIEW",json.get("sessionType").getAsString());assertEquals(EVENT,json.get("eventId").getAsString());assertEquals(Set.of("sessionType","eventId"),json.keySet());assertEquals(2,server.getRequestCount());
    }
}
