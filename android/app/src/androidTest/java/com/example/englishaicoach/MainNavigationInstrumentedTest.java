package com.example.englishaicoach;

import static org.junit.Assert.assertEquals;

import android.content.Intent;
import android.widget.TextView;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class MainNavigationInstrumentedTest {
    @Test
    public void mainTabsSwitchFragmentAndVietnameseTitle() {
        var instrumentation = InstrumentationRegistry.getInstrumentation();
        Intent intent = new Intent(instrumentation.getTargetContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        MainActivity activity = (MainActivity) instrumentation.startActivitySync(intent);
        try {
            assertTab(activity, R.id.nav_home, R.string.nav_home, "HOME");
            assertTab(activity, R.id.nav_review, R.string.nav_review, "REVIEW");
            assertTab(activity, R.id.nav_profile, R.string.nav_profile, "PROFILE");
        } finally {
            activity.runOnUiThread(activity::finish);
        }
    }

    private void assertTab(MainActivity activity, int menuId, int titleId, String tag) {
        var instrumentation = InstrumentationRegistry.getInstrumentation();
        BottomNavigationView navigation = activity.findViewById(R.id.main_navigation);
        activity.runOnUiThread(() -> navigation.setSelectedItemId(menuId));
        instrumentation.waitForIdleSync();
        assertEquals(menuId, navigation.getSelectedItemId());
        assertEquals(tag, activity.getSupportFragmentManager()
                .findFragmentById(R.id.main_content).getTag());
        TextView title = activity.findViewById(R.id.destination_title);
        assertEquals(activity.getString(titleId), title.getText().toString());
    }
}
