package com.example.englishaicoach.core.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.englishaicoach.core.storage.ReadOnlyCachePolicy;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public final class OfflineBoundaryTest {
    @Test
    public void offlineMutationIsNotExecutedOrQueued() {
        AtomicInteger calls = new AtomicInteger();
        assertEquals(OfflineMutationGuard.Decision.BLOCK_OFFLINE,
                OfflineMutationGuard.runIfOnline(false, calls::incrementAndGet));
        assertEquals(0, calls.get());
        assertEquals(OfflineMutationGuard.Decision.ALLOW,
                OfflineMutationGuard.runIfOnline(true, calls::incrementAndGet));
        assertEquals(1, calls.get());
    }

    @Test
    public void cacheAllowsOnlyApprovedReadOnlyContent() {
        assertTrue(ReadOnlyCachePolicy.canDisplayOffline(
                ReadOnlyCachePolicy.Content.VOCABULARY_METADATA));
        assertTrue(ReadOnlyCachePolicy.canDisplayOffline(
                ReadOnlyCachePolicy.Content.APPROVED_REUSABLE_CONTENT));
        assertFalse(ReadOnlyCachePolicy.canDisplayOffline(ReadOnlyCachePolicy.Content.SRS));
        assertFalse(ReadOnlyCachePolicy.canDisplayOffline(ReadOnlyCachePolicy.Content.PROGRESS));
        assertFalse(ReadOnlyCachePolicy.canDisplayOffline(ReadOnlyCachePolicy.Content.XP));
        assertFalse(ReadOnlyCachePolicy.canDisplayOffline(ReadOnlyCachePolicy.Content.STREAK));
        assertFalse(ReadOnlyCachePolicy.canDisplayOffline(
                ReadOnlyCachePolicy.Content.ATTEMPT_HISTORY));
    }
}
