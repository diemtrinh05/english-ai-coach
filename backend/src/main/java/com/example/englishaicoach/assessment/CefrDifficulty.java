package com.example.englishaicoach.assessment;

/** Thứ tự difficulty cố định của assessment-block-v1. */
public enum CefrDifficulty {
    A1, A2, B1, B2, C1, C2;

    public CefrDifficulty move(int step) {
        if (step < -1 || step > 1) {
            throw new IllegalArgumentException("Difficulty chỉ được dịch một level");
        }
        return values()[Math.clamp(ordinal() + step, 0, values().length - 1)];
    }
}
