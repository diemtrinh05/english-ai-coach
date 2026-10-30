package com.example.englishaicoach.domain.model;

import java.util.List;

public final class Assessment {
    private Assessment() { }
    public static final class Start {
        public final String type = "INITIAL";
        public final String eventId;
        public Start(String eventId) { this.eventId = eventId; }
    }
    public static final class Answer {
        public final String questionId, answer, eventId;
        public final int responseTimeMs;
        public Answer(String questionId, String answer, int responseTimeMs, String eventId) {
            this.questionId = questionId; this.answer = answer;
            this.responseTimeMs = responseTimeMs; this.eventId = eventId;
        }
    }
    public static final class Question {
        public final String id, text, level;
        public final List<String> options;
        public Question(String id, String text, List<String> options, String level) {
            this.id = id; this.text = text; this.options = List.copyOf(options); this.level = level;
        }
    }
    public static final class Result {
        public final String id, status;
        public final int answered;
        public final int correct;
        public final java.math.BigDecimal score;
        public final Level level;
        public Result(String id, String status, int answered) { this(id, status, answered, 0, null, null); }
        public Result(String id, String status, int answered, int correct, java.math.BigDecimal score, Level level) {
            this.id = id; this.status = status; this.answered = answered; this.correct = correct; this.score = score; this.level = level;
        }
    }
    public static final class Level {
        public final String code, name, description;
        public Level(String code, String name, String description) { this.code = code; this.name = name; this.description = description; }
    }
    public static final class Ack {
        public final boolean correct, completed;
        public final String nextDifficulty;
        public final int answered;
        public Ack(boolean correct, boolean completed, String nextDifficulty, int answered) {
            this.correct = correct; this.completed = completed; this.nextDifficulty = nextDifficulty; this.answered = answered;
        }
    }
    public static final class Checkpoint {
        public final String owner, startEvent, assessmentId;
        public final String type = "INITIAL";
        public final Answer pending;
        public Checkpoint(String owner, String startEvent, String assessmentId, Answer pending) {
            this.owner = owner; this.startEvent = startEvent; this.assessmentId = assessmentId; this.pending = pending;
        }
    }
}
