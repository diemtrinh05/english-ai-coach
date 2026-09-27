package com.example.englishaicoach.core.network;

import java.util.Objects;

public final class OfflineMutationGuard {
    public enum Decision { ALLOW, BLOCK_OFFLINE }

    private OfflineMutationGuard() { }

    public static Decision check(boolean networkAvailable) {
        return networkAvailable ? Decision.ALLOW : Decision.BLOCK_OFFLINE;
    }

    public static Decision runIfOnline(boolean networkAvailable, Runnable mutation) {
        Objects.requireNonNull(mutation);
        Decision decision = check(networkAvailable);
        if (decision == Decision.ALLOW) {
            mutation.run();
        }
        return decision;
    }
}
