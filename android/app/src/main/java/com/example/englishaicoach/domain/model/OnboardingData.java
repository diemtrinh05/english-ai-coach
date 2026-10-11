package com.example.englishaicoach.domain.model;

import java.util.*;

public final class OnboardingData {
    private OnboardingData() { }
    public static final class Profile {
        public final String fullName, timezone, avatarUrl;
        public final Integer dailyLearningMinutes;
        public Profile(String name, Integer minutes, String zone, String avatar) {
            fullName = name; dailyLearningMinutes = minutes; timezone = zone; avatarUrl = avatar;
        }
        @Override public boolean equals(Object other) {
            if (!(other instanceof Profile)) return false;
            Profile p = (Profile) other;
            return Objects.equals(fullName, p.fullName) && Objects.equals(timezone, p.timezone)
                    && Objects.equals(avatarUrl, p.avatarUrl) && Objects.equals(dailyLearningMinutes, p.dailyLearningMinutes);
        }
        @Override public int hashCode() { return Objects.hash(fullName, timezone, avatarUrl, dailyLearningMinutes); }
    }
    public static final class Goal {
        public final String id, name;
        public final boolean active;
        public Goal(String id, String name, boolean active) { this.id = id; this.name = name; this.active = active; }
    }
    public static final class Selection {
        public final List<String> goalIds;
        public final String primaryGoalId;
        public Selection(Collection<String> ids, String primary) {
            goalIds = Collections.unmodifiableList(new ArrayList<>(ids)); primaryGoalId = primary;
        }
    }
    public static final class Snapshot {
        public final Profile profile;
        public final List<Goal> goals;
        public final Selection selection;
        public Snapshot(Profile profile, List<Goal> goals, Selection selection) {
            this.profile = profile; this.goals = Collections.unmodifiableList(new ArrayList<>(goals)); this.selection = selection;
        }
    }
}
