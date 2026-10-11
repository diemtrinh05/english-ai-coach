package com.example.englishaicoach.core.auth;
import static org.junit.Assert.*;
import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.example.englishaicoach.MainActivity;
import com.example.englishaicoach.R;
import com.example.englishaicoach.core.network.ApiClient;
import com.example.englishaicoach.domain.repository.*;
import com.example.englishaicoach.domain.usecase.*;
import org.junit.*;
import org.junit.runner.RunWith;
import java.lang.reflect.Field;

@RunWith(AndroidJUnit4.class)
public final class AuthFormsInstrumentedTest {
    MainActivity activity; Field field; Object previous; SessionManager sessions; Fake repository;
    @Before public void setup() throws Exception {
        field=AppSessionDependencies.class.getDeclaredField("instance"); field.setAccessible(true); previous=field.get(null);
        sessions=new SessionManager(new TokenStore() {
            TokenSession value; public TokenSession read(){return value;} public void write(TokenSession value){this.value=value;}
        });
        repository=new Fake();
        field.set(null,new AppSessionDependencies(sessions,new ApiClient("https://example.invalid/api/v1/",sessions),
            new RestoreSessionUseCase(callback->{callback.completed(sessions.current()==null ? AuthSessionRepository.Result.UNAUTHENTICATED : AuthSessionRepository.Result.AUTHENTICATED); return ()->{};}),
            new SubmitAuthFormUseCase(repository)));
        var instrumentation=InstrumentationRegistry.getInstrumentation();
        Intent intent=new Intent(instrumentation.getTargetContext(),MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity=(MainActivity)instrumentation.startActivitySync(intent); instrumentation.waitForIdleSync();
        // Chờ connectivity callback thực tế trước khi gửi form.
        for(int i=0;i<30 && !activity.findViewById(R.id.form_submit).isEnabled();i++){Thread.sleep(100); instrumentation.waitForIdleSync();}
        assertTrue(activity.findViewById(R.id.form_submit).isEnabled());
    }
    @After public void cleanup() throws Exception {
        if(activity!=null){ui(()->activity.finish());} field.set(null,previous);
    }
    void ui(Runnable action){InstrumentationRegistry.getInstrumentation().runOnMainSync(action); InstrumentationRegistry.getInstrumentation().waitForIdleSync();}
    void fill(){ui(()->{((EditText)activity.findViewById(R.id.email_input)).setText("a@example.com");((EditText)activity.findViewById(R.id.password_input)).setText("password");});}
    void submit(){ui(()->activity.findViewById(R.id.form_submit).performClick());}
    @Test public void invalidLoginShowsInlineVietnameseErrorsWithoutRequest(){
        submit(); assertEquals(0,repository.calls);
        assertEquals(activity.getString(R.string.auth_field_required),((com.google.android.material.textfield.TextInputLayout)activity.findViewById(R.id.email_layout)).getError().toString());
        assertEquals(View.GONE,activity.findViewById(R.id.main_navigation).getVisibility());
    }
    @Test public void registerMismatchAndModeSwitchClearPasswords(){
        ui(()->activity.findViewById(R.id.form_switch).performClick()); fill();
        ui(()->{((EditText)activity.findViewById(R.id.name_input)).setText("Tên");((EditText)activity.findViewById(R.id.confirmation_input)).setText("different");});
        submit(); assertEquals(0,repository.calls);
        assertEquals(activity.getString(R.string.auth_password_mismatch),((com.google.android.material.textfield.TextInputLayout)activity.findViewById(R.id.confirmation_layout)).getError().toString());
        ui(()->activity.getOnBackPressedDispatcher().onBackPressed()); assertEquals("",((EditText)activity.findViewById(R.id.password_input)).getText().toString());
    }
    @Test public void loadingGuardsDuplicateAndLockedErrorStaysAtLogin(){
        fill();submit();submit();assertEquals(1,repository.calls);assertFalse(activity.findViewById(R.id.form_submit).isEnabled());
        ui(()->repository.callback.completed(AuthFormsRepository.Result.LOCKED));
        assertEquals(activity.getString(R.string.auth_locked),((TextView)activity.findViewById(R.id.form_message)).getText().toString());
        assertTrue(activity.findViewById(R.id.form_submit).isEnabled()); assertNull(sessions.current());
    }
    @Test public void successUsesSharedRestoreGateAndClearsAuthBackStack(){
        fill();submit();ui(()->{sessions.start("test-access","test-refresh");repository.callback.completed(AuthFormsRepository.Result.SUCCESS);});
        assertEquals(View.VISIBLE,activity.findViewById(R.id.main_navigation).getVisibility());
        assertEquals(0,activity.getSupportFragmentManager().getBackStackEntryCount());assertEquals("HOME",activity.getSupportFragmentManager().findFragmentById(R.id.main_content).getTag());
    }
    @Test public void passwordFieldsExcludeSavedState(){
        assertFalse(activity.findViewById(R.id.password_input).isSaveEnabled());assertFalse(activity.findViewById(R.id.confirmation_input).isSaveEnabled());
        fill();ui(()->activity.recreate());
        InstrumentationRegistry.getInstrumentation().runOnMainSync(()->{
            for(android.app.Activity candidate:androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(androidx.test.runner.lifecycle.Stage.RESUMED))
                if(candidate instanceof MainActivity)activity=(MainActivity)candidate;
        });
        assertEquals("",((EditText)activity.findViewById(R.id.password_input)).getText().toString());
    }
    static final class Fake implements AuthFormsRepository {
        int calls; Callback callback;
        public Cancelable submit(boolean register,String name,String email,String password,Callback callback){calls++;this.callback=callback;return ()->{};}
    }
}
