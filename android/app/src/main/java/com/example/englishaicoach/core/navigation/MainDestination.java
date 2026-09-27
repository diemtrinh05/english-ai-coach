package com.example.englishaicoach.core.navigation;

import com.example.englishaicoach.R;

public enum MainDestination {
    HOME(R.id.nav_home, R.string.nav_home),
    LEARN(R.id.nav_learn, R.string.nav_learn),
    REVIEW(R.id.nav_review, R.string.nav_review),
    PROGRESS(R.id.nav_progress, R.string.nav_progress),
    PROFILE(R.id.nav_profile, R.string.nav_profile);

    private final int menuId;
    private final int titleId;

    MainDestination(int menuId, int titleId) {
        this.menuId = menuId;
        this.titleId = titleId;
    }

    public int getMenuId() { return menuId; }
    public int getTitleId() { return titleId; }

    public static MainDestination fromMenuId(int menuId) {
        for (MainDestination destination : values()) {
            if (destination.menuId == menuId) {
                return destination;
            }
        }
        throw new IllegalArgumentException("Unknown main navigation item");
    }
}
