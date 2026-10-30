package com.example.englishaicoach.core.auth;
import static org.junit.Assert.*;
import android.content.Intent;
import android.view.View;
import android.widget.*;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.example.englishaicoach.MainActivity;
import com.example.englishaicoach.R;
import com.example.englishaicoach.core.network.*;
import com.example.englishaicoach.domain.repository.*;
import com.example.englishaicoach.domain.usecase.*;
import com.example.englishaicoach.domain.model.OnboardingData.*;
import org.junit.*;
import org.junit.runner.RunWith;
import java.lang.reflect.Field;
import java.util.*;
@RunWith(AndroidJUnit4.class)
public final class OnboardingInstrumentedTest {
    static final String ID="00000000-0000-0000-0000-000000000001";
    MainActivity activity;Field field;Object previous;SessionManager sessions;Fake repository;
    Field gateField; Object previousGate;
    @Before public void setup() throws Exception {
        field=AppSessionDependencies.class.getDeclaredField("instance");field.setAccessible(true);previous=field.get(null);
        sessions=new SessionManager(new TokenStore(){TokenSession value;public TokenSession read(){return value;}public void write(TokenSession v){value=v;}});sessions.start("test-a","test-r");
        repository=new Fake();
        gateField=com.example.englishaicoach.feature.onboarding.OnboardingGateDependencies.class.getDeclaredField("useCase");
        gateField.setAccessible(true);previousGate=gateField.get(null);
        gateField.set(null,new ResolveOnboardingUseCase((owner,callback)->{
            callback.completed(repository.saved?com.example.englishaicoach.domain.model.OnboardingRoute.PLACEMENT:com.example.englishaicoach.domain.model.OnboardingRoute.HOME,null);return ()->{};
        }));
        field.set(null,new AppSessionDependencies(sessions,new ApiClient("https://example.invalid/api/v1/",sessions),
                new RestoreSessionUseCase(callback->{callback.completed(sessions.current()==null ? AuthSessionRepository.Result.UNAUTHENTICATED:AuthSessionRepository.Result.AUTHENTICATED);return ()->{};}),
                new SubmitAuthFormUseCase((register,name,email,password,callback)->()->{}),new ConfigureOnboardingUseCase(repository)));
        var instrumentation=InstrumentationRegistry.getInstrumentation();
        activity=(MainActivity)instrumentation.startActivitySync(new Intent(instrumentation.getTargetContext(),MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));instrumentation.waitForIdleSync();
    }
    @After public void cleanup() throws Exception {if(activity!=null)ui(()->activity.finish());field.set(null,previous);gateField.set(null,previousGate);}
    void ui(Runnable action){var i=InstrumentationRegistry.getInstrumentation();i.runOnMainSync(action);i.waitForIdleSync();}
    void open() throws Exception {ui(()->activity.findViewById(R.id.open_onboarding).performClick());for(int n=0;n<30&&!activity.findViewById(R.id.onboarding_next).isEnabled();n++){Thread.sleep(100);InstrumentationRegistry.getInstrumentation().waitForIdleSync();}}
    void capture(String stage) throws Exception {
        // Ảnh UI dùng fixture test; lưu ngoài repository để kiểm chứng visual của flow thật.
        android.os.ParcelFileDescriptor descriptor = InstrumentationRegistry.getInstrumentation().getUiAutomation()
                .executeShellCommand("screencap -p /data/local/tmp/AND-ONB-001-" + stage + ".png");
        try (java.io.InputStream stream = new android.os.ParcelFileDescriptor.AutoCloseInputStream(descriptor)) {
            byte[] buffer = new byte[512]; while (stream.read(buffer) >= 0) { }
        }
    }
    void next(){ui(()->activity.findViewById(R.id.onboarding_next).performClick());}
    @Test public void entryLoadsServerAndNewProfileHasNoHiddenDefaults() throws Exception {
        repository.missing=true;open();assertEquals(View.GONE,activity.findViewById(R.id.main_navigation).getVisibility());
        assertEquals("",((EditText)activity.findViewById(R.id.onboarding_zone)).getText().toString());assertEquals("",((EditText)activity.findViewById(R.id.onboarding_minutes)).getText().toString());
        assertFalse(((CheckBox)activity.findViewById(R.id.onboarding_zone_confirm)).isChecked());next();assertEquals(0,repository.saves);
        assertEquals(activity.getString(R.string.onboarding_validation),((TextView)activity.findViewById(R.id.onboarding_message)).getText().toString());
    }
    @Test public void confirmedSaveRechecksServerGateAndRoutesPlacementWithoutAutoStart() throws Exception {
        open();capture("profile");next();
        LinearLayout row=(LinearLayout)((LinearLayout)activity.findViewById(R.id.onboarding_goal_items)).getChildAt(0);
        assertEquals(activity.getString(R.string.onboarding_goal_travel),((CheckBox)row.getChildAt(0)).getText().toString());
        capture("goals");next();capture("daily");next();assertEquals(1,repository.saves);
        repository.saved=true;ui(()->repository.callback.completed(new OnboardingRepository.Result(null,true,true,null)));
        assertEquals("PLACEMENT",activity.getSupportFragmentManager().findFragmentById(R.id.main_content).getTag());
        assertEquals(View.VISIBLE,activity.findViewById(R.id.assessment_start).getVisibility());
        assertEquals(View.GONE,activity.findViewById(R.id.main_navigation).getVisibility());
    }
    @Test public void partialFailureKeepsDraftAndExplicitRetrySkipsProfile() throws Exception {
        open();next();next();next();next();assertEquals(1,repository.saves);
        ui(()->repository.callback.completed(new OnboardingRepository.Result(null,false,true,new ApiErrorParser().parse(400,null))));
        assertEquals(activity.getString(R.string.onboarding_partial),((TextView)activity.findViewById(R.id.onboarding_message)).getText().toString());
        next();assertEquals(2,repository.saves);assertTrue(repository.skipped);assertEquals("avatar",repository.profile.avatarUrl);
    }
    @Test public void rotationKeepsMemoryDraftWithoutReloadOrSavedPrivateFields() throws Exception {
        open();ui(()->((EditText)activity.findViewById(R.id.onboarding_name)).setText("Tên đã sửa"));assertFalse(activity.findViewById(R.id.onboarding_name).isSaveEnabled());
        ui(()->activity.recreate());InstrumentationRegistry.getInstrumentation().runOnMainSync(()->{
            for(android.app.Activity candidate:androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(androidx.test.runner.lifecycle.Stage.RESUMED))if(candidate instanceof MainActivity)activity=(MainActivity)candidate;
        });InstrumentationRegistry.getInstrumentation().waitForIdleSync();assertEquals("Tên đã sửa",((EditText)activity.findViewById(R.id.onboarding_name)).getText().toString());assertEquals(1,repository.loads);
    }
    @Test public void sessionExpiryRemovesPrivateFlowAndLateResultCannotReopenIt() throws Exception {
        open();next();next();next();var late=repository.callback;ui(()->sessions.clear());
        assertEquals(0,activity.getSupportFragmentManager().getBackStackEntryCount());assertEquals("AUTH_BOUNDARY",activity.getSupportFragmentManager().findFragmentById(R.id.main_content).getTag());
        ui(()->late.completed(new OnboardingRepository.Result(null,true,true,null)));assertEquals("AUTH_BOUNDARY",activity.getSupportFragmentManager().findFragmentById(R.id.main_content).getTag());
    }
    static final class Fake implements OnboardingRepository {
        boolean missing,skipped,saved;int loads,saves;Profile profile;Callback callback;
        public Cancelable load(Callback callback){loads++;callback.completed(new Result(new Snapshot(new Profile("Tên",missing?null:20,missing?null:"UTC","avatar"),List.of(new Goal(ID,"TRAVEL",true)),new Selection(List.of(ID),null)),true,false,null));return ()->{};}
        public Cancelable save(Profile p,Selection s,boolean skip,Callback c){profile=p;skipped=skip;saves++;callback=c;return ()->{};}
    }
}
