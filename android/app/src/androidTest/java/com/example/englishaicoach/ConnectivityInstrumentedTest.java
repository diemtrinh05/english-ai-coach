package com.example.englishaicoach;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import android.app.Instrumentation;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.view.View;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

@RunWith(AndroidJUnit4.class)
public final class ConnectivityInstrumentedTest {
    private static final long TRANSITION_TIMEOUT_MS = 20_000;

    @Test
    public void offlineBannerFollowsDeviceConnectivityInBothDirections() throws Exception {
        assumeTrue(Build.PRODUCT.startsWith("sdk_")
                || Build.FINGERPRINT.contains("generic")
                || Build.FINGERPRINT.contains("emulator"));
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        ConnectivityManager connectivityManager = instrumentation.getTargetContext()
                .getSystemService(ConnectivityManager.class);
        assumeTrue(hasInternetNetwork(connectivityManager));

        Intent intent = new Intent(instrumentation.getTargetContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        MainActivity activity = (MainActivity) instrumentation.startActivitySync(intent);
        try {
            waitForBanner(instrumentation, activity, View.GONE);

            runShellCommand(instrumentation, "cmd connectivity airplane-mode enable");
            runShellCommand(instrumentation, "svc wifi disable");
            waitForNetwork(connectivityManager, false);
            waitForBanner(instrumentation, activity, View.VISIBLE);
        } finally {
            // Khôi phục mạng của emulator kể cả khi một assertion thất bại.
            runShellCommand(instrumentation, "svc wifi enable");
            runShellCommand(instrumentation, "cmd connectivity airplane-mode disable");
            try {
                waitForNetwork(connectivityManager, true);
                waitForBanner(instrumentation, activity, View.GONE);
            } finally {
                activity.runOnUiThread(activity::finish);
            }
        }
    }

    private static void runShellCommand(Instrumentation instrumentation, String command)
            throws IOException {
        try (ParcelFileDescriptor output = instrumentation.getUiAutomation()
                .executeShellCommand(command);
             FileInputStream stream = new FileInputStream(output.getFileDescriptor())) {
            byte[] buffer = new byte[256];
            while (stream.read(buffer) != -1) {
                // Đọc hết output để chờ lệnh shell hoàn tất.
            }
        }
    }

    private static void waitForNetwork(ConnectivityManager manager, boolean expected)
            throws InterruptedException {
        long deadline = System.currentTimeMillis() + TRANSITION_TIMEOUT_MS;
        while (System.currentTimeMillis() < deadline) {
            if (hasInternetNetwork(manager) == expected) {
                return;
            }
            Thread.sleep(250);
        }
        assertEquals(expected, hasInternetNetwork(manager));
    }

    private static boolean hasInternetNetwork(ConnectivityManager manager) {
        Network network = manager.getActiveNetwork();
        NetworkCapabilities capabilities = network == null
                ? null : manager.getNetworkCapabilities(network);
        return capabilities != null
                && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    private static void waitForBanner(Instrumentation instrumentation, MainActivity activity,
                                      int expectedVisibility) throws InterruptedException {
        long deadline = System.currentTimeMillis() + TRANSITION_TIMEOUT_MS;
        AtomicInteger visibility = new AtomicInteger();
        while (System.currentTimeMillis() < deadline) {
            instrumentation.runOnMainSync(() -> visibility.set(
                    activity.findViewById(R.id.offline_banner).getVisibility()));
            if (visibility.get() == expectedVisibility) {
                return;
            }
            Thread.sleep(250);
        }
        assertTrue("Banner không đổi theo callback mạng", visibility.get() == expectedVisibility);
    }
}
