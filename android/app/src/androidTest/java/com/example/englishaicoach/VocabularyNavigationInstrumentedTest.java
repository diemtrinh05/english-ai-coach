package com.example.englishaicoach;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import android.content.Intent;
import android.widget.EditText;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class VocabularyNavigationInstrumentedTest {
    @Test public void learnOpensVocabularyAsSecondaryScreenAndBackRestoresLearn() {
        var instrumentation = InstrumentationRegistry.getInstrumentation();
        Intent intent = new Intent(instrumentation.getTargetContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        MainActivity activity = (MainActivity) instrumentation.startActivitySync(intent);
        try {
            BottomNavigationView navigation = activity.findViewById(R.id.main_navigation);
            activity.runOnUiThread(() -> navigation.setSelectedItemId(R.id.nav_learn));
            instrumentation.waitForIdleSync();
            activity.runOnUiThread(() -> activity.findViewById(R.id.open_vocabulary)
                    .performClick());
            instrumentation.waitForIdleSync();
            EditText search = activity.findViewById(R.id.search_input);
            assertNotNull(search);
            assertEquals("VOCABULARY_LIST", activity.getSupportFragmentManager()
                    .findFragmentById(R.id.main_content).getTag());
            activity.runOnUiThread(() -> activity.getSupportFragmentManager().popBackStack());
            instrumentation.waitForIdleSync();
            assertEquals("LEARN", activity.getSupportFragmentManager()
                    .findFragmentById(R.id.main_content).getTag());
            assertNull(activity.findViewById(R.id.search_input));
        } finally {
            activity.runOnUiThread(activity::finish);
        }
    }
}
