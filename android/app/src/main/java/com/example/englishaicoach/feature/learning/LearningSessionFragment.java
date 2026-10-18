package com.example.englishaicoach.feature.learning;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.*;
import android.view.*;
import android.widget.*;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.*;
import com.example.englishaicoach.MainActivity;
import com.example.englishaicoach.R;
import com.example.englishaicoach.core.auth.AppSessionDependencies;
import com.example.englishaicoach.core.network.ConnectivityMonitor;
import com.example.englishaicoach.domain.model.VocabularyItem;
import com.example.englishaicoach.domain.model.LearningAttempt;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

public final class LearningSessionFragment extends Fragment {
    private LearningSessionViewModel model;
    private LinearLayout content;
    private MaterialTextView status, word, phonetic, meaning, examples;
    private MaterialButton reveal, retry, audio;
    private LinearLayout qualities;
    private MaterialButton returnToVocabulary, complete, progressRetry;
    private MaterialTextView progressStatus;
    private MediaPlayer player;
    private boolean authReady;
    public static LearningSessionFragment forVocabulary(String id) {
        return forVocabulary(id, "MIXED", "DETAIL");
    }
    public static LearningSessionFragment forVocabulary(String id, String type, String origin) {
        LearningSessionFragment fragment = new LearningSessionFragment();
        Bundle args = new Bundle(); args.putString("vocabulary", id); args.putString("sessionType", type); args.putString("origin", origin); fragment.setArguments(args); return fragment;
    }
    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle saved) {
        ScrollView scroll = new ScrollView(requireContext());
        content = new LinearLayout(requireContext()); content.setOrientation(LinearLayout.VERTICAL);
        int padding = getResources().getDimensionPixelSize(R.dimen.spacing_large);
        content.setPadding(padding, padding, padding, padding); scroll.addView(content);
        MaterialButton back = button(R.string.learning_back); back.setOnClickListener(v -> back());
        status = text(com.google.android.material.R.style.TextAppearance_Material3_BodyLarge);
        status.setId(R.id.learning_status);
        word = text(com.google.android.material.R.style.TextAppearance_Material3_HeadlineLarge);
        word.setId(R.id.learning_word);
        word.setGravity(Gravity.CENTER);
        phonetic = text(com.google.android.material.R.style.TextAppearance_Material3_TitleMedium);
        phonetic.setGravity(Gravity.CENTER);
        audio = button(R.string.learning_audio); audio.setOnClickListener(v -> play());
        meaning = text(com.google.android.material.R.style.TextAppearance_Material3_TitleLarge);
        examples = text(com.google.android.material.R.style.TextAppearance_Material3_BodyLarge);
        reveal = button(R.string.learning_reveal); reveal.setOnClickListener(v -> model.reveal());
        reveal.setId(R.id.learning_reveal_button);
        retry = button(R.string.learning_retry); retry.setOnClickListener(v -> {
            if (authReady) model.retry(); else ((MainActivity) requireActivity()).retryAuth();
        });
        retry.setId(R.id.learning_retry_button);
        qualities = new LinearLayout(requireContext()); qualities.setOrientation(LinearLayout.VERTICAL);
        qualities.setId(R.id.learning_quality_selector); content.addView(qualities);
        int[] labels = {R.string.learning_quality_0, R.string.learning_quality_1, R.string.learning_quality_2,
                R.string.learning_quality_3, R.string.learning_quality_4, R.string.learning_quality_5};
        int[] ids = {R.id.learning_quality_0, R.id.learning_quality_1, R.id.learning_quality_2,
                R.id.learning_quality_3, R.id.learning_quality_4, R.id.learning_quality_5};
        for (int row = 0; row < 2; row++) {
            LinearLayout line = new LinearLayout(requireContext()); qualities.addView(line);
            for (int column = 0; column < 3; column++) {
                final int quality = row * 3 + column;
                MaterialButton choice = new MaterialButton(requireContext()); choice.setText(labels[quality]); choice.setId(ids[quality]);
                choice.setOnClickListener(v -> model.selectQuality(quality));
                line.addView(choice, new LinearLayout.LayoutParams(0, -2, 1));
            }
        }
        returnToVocabulary = button(R.string.learning_return_vocabulary);
        returnToVocabulary.setId(R.id.learning_return_button); returnToVocabulary.setOnClickListener(v -> back());
        complete = button(R.string.learning_complete); complete.setId(R.id.learning_complete_button);
        complete.setOnClickListener(v -> model.complete());
        progressStatus = text(com.google.android.material.R.style.TextAppearance_Material3_BodyLarge);
        progressStatus.setId(R.id.learning_progress_status);
        progressRetry = button(R.string.learning_progress_reload); progressRetry.setId(R.id.learning_progress_retry);
        progressRetry.setOnClickListener(v -> model.refreshProgress());
        return scroll;
    }
    private MaterialTextView text(int appearance) {
        MaterialTextView view = new MaterialTextView(requireContext()); view.setTextAppearance(appearance);
        content.addView(view, new LinearLayout.LayoutParams(-1, -2)); return view;
    }
    private MaterialButton button(int label) {
        MaterialButton view = new MaterialButton(requireContext()); view.setText(label); content.addView(view); return view;
    }
    @Override public void onViewCreated(@NonNull View view, Bundle saved) {
        LearningAttempt.Request pending = saved != null && saved.containsKey("attemptEvent")
                ? new LearningAttempt.Request(saved.getString("server"), saved.getString("vocabulary"),
                    saved.getInt("attemptTime"), saved.getInt("attemptQuality"), saved.getString("attemptEvent")) : null;
        LearningSessionViewModel.Snapshot snapshot = saved != null && saved.containsKey("event")
                ? new LearningSessionViewModel.Snapshot(saved.getString("owner"), saved.getString("event"),
                    saved.getString("vocabulary"), saved.getString("server"), pending, saved.getBoolean("accepted"),
                    saved.getBoolean("conflict"), saved.getBoolean("reuse"), saved.getBoolean("rejected"),
                    saved.getString("sessionType", "MIXED"), saved.getString("origin", "DETAIL"),
                    saved.getString("completeEvent"), saved.getBoolean("completed"), saved.getBoolean("completeRejected")) : null;
        AppSessionDependencies dependencies = AppSessionDependencies.get(requireContext());
        model = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> type) {
                Handler handler = new Handler(Looper.getMainLooper());
                LearningSessionViewModel created = new LearningSessionViewModel(dependencies.sessions,
                        LearningDependencies.useCase(requireContext()), LearningDependencies.attempts(requireContext()), command -> {
                            if (Looper.myLooper() == Looper.getMainLooper()) command.run(); else handler.post(command);
                        }, SystemClock::elapsedRealtime, requireArguments().getString("vocabulary"), snapshot);
                created.configure(LearningDependencies.review(requireContext()), requireArguments().getString("sessionType", "MIXED"),
                        requireArguments().getString("origin", "DETAIL"));
                return type.cast(created);
            }
        }).get(LearningSessionViewModel.class);
        model.state().observe(getViewLifecycleOwner(), state -> { if (authReady) render(state); });
        ((MainActivity) requireActivity()).authState().observe(getViewLifecycleOwner(), state -> {
            authReady = state == com.example.englishaicoach.feature.auth.AuthSessionViewModel.State.AUTHENTICATED;
            model.setAuthenticated(authReady);
            if (authReady) render(model.state().getValue()); else renderAuth(state);
        });
        new ConnectivityMonitor(requireContext()).observe(getViewLifecycleOwner(), online -> model.setOnline(Boolean.TRUE.equals(online)));
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() { back(); }
        });
    }
    private void renderAuth(com.example.englishaicoach.feature.auth.AuthSessionViewModel.State state) {
        stopAudio(); hideCard(); status.setText(state == com.example.englishaicoach.feature.auth.AuthSessionViewModel.State.ERROR
                ? R.string.learning_auth_error : R.string.learning_loading);
        retry.setVisibility(state == com.example.englishaicoach.feature.auth.AuthSessionViewModel.State.ERROR ? View.VISIBLE : View.GONE);
    }
    private void hideCard() {
        for (View view : new View[]{word, phonetic, meaning, examples, reveal, audio}) view.setVisibility(View.GONE);
        qualities.setVisibility(View.GONE); returnToVocabulary.setVisibility(View.GONE);
        complete.setVisibility(View.GONE); progressStatus.setVisibility(View.GONE); progressRetry.setVisibility(View.GONE);
    }
    private void render(LearningSessionViewModel.State state) {
        hideCard(); retry.setVisibility(View.GONE); retry.setText(R.string.learning_retry);
        returnToVocabulary.setText("REVIEW".equals(model.origin()) ? R.string.learning_return_review : R.string.learning_return_vocabulary);
        int message;
        switch (state.phase) {
            case FRONT: case BACK:
                message = R.string.learning_card_position;
                VocabularyItem card = state.card;
                word.setText(card.word); phonetic.setText(card.phoneticIpa);
                word.setVisibility(View.VISIBLE); phonetic.setVisibility(View.VISIBLE);
                audio.setVisibility(View.VISIBLE); audio.setEnabled(validAudio(card.audioUrl));
                if (state.phase == LearningSessionViewModel.Phase.FRONT) reveal.setVisibility(View.VISIBLE);
                else {
                    qualities.setVisibility(View.VISIBLE);
                    meaning.setVisibility(View.VISIBLE); examples.setVisibility(View.VISIBLE);
                    meaning.setText(join(card.meaningEn, card.meaningVi));
                    StringBuilder detail = new StringBuilder();
                    for (VocabularyItem.Example example : card.examples) {
                        if (detail.length() > 0) detail.append("\n\n"); detail.append(join(example.text, example.translation));
                    }
                    examples.setText(detail.toString());
                }
                break;
            case SUBMITTING: stopAudio(); message = R.string.learning_submitting; break;
            case PENDING:
                stopAudio(); message = R.string.learning_attempt_pending;
                retry.setText(R.string.learning_attempt_retry); retry.setVisibility(View.VISIBLE); break;
            case CONCURRENT: case REUSE:
                stopAudio(); message = state.phase == LearningSessionViewModel.Phase.CONCURRENT
                        ? R.string.learning_concurrent : R.string.learning_attempt_reuse;
                retry.setText(R.string.learning_refresh_conflict); retry.setVisibility(View.VISIBLE);
                returnToVocabulary.setVisibility(View.VISIBLE); break;
            case REFRESHING: stopAudio(); message = R.string.learning_refreshing; break;
            case COMPLETING: stopAudio(); message = R.string.learning_completing; break;
            case COMPLETE_PENDING:
                stopAudio(); message = R.string.learning_complete_pending; retry.setVisibility(View.VISIBLE);
                returnToVocabulary.setVisibility(View.VISIBLE); break;
            case COMPLETE_REJECTED:
                stopAudio(); message = state.error != null && state.error.isIdempotencyKeyReuse()
                        ? R.string.learning_complete_reuse : R.string.learning_complete_rejected;
                returnToVocabulary.setVisibility(View.VISIBLE); break;
            case COMPLETED:
                stopAudio(); com.example.englishaicoach.domain.model.LearningSummary summary = model.summary();
                status.setText(getString(R.string.learning_summary, summary.wordsStudiedCount,
                        summary.accuracyPercent.toPlainString(), summary.startedAt, summary.endedAt));
                returnToVocabulary.setVisibility(View.VISIBLE); return;
            case FEEDBACK:
                stopAudio(); LearningAttempt feedback = model.feedback();
                status.setText(getString(feedback.isCorrect ? R.string.learning_feedback_correct : R.string.learning_feedback_incorrect,
                        feedback.answerQuality, feedback.intervalDays, feedback.nextReviewAt));
                returnToVocabulary.setVisibility(View.VISIBLE); renderAccepted(); return;
            case ACCEPTED: stopAudio(); message = R.string.learning_attempt_recorded;
                returnToVocabulary.setVisibility(View.VISIBLE); renderAccepted(); break;
            case OVERFLOW: stopAudio(); message = R.string.learning_timer_overflow;
                returnToVocabulary.setVisibility(View.VISIBLE); break;
            case REJECTED: stopAudio(); message = R.string.learning_attempt_rejected;
                returnToVocabulary.setVisibility(View.VISIBLE); break;
            case CLOSED: message = R.string.learning_closed; stopAudio(); break;
            case EXPIRED: case EXITED: message = R.string.learning_expired; stopAudio(); break;
            case ERROR:
                stopAudio(); message = state.error != null && state.error.isIdempotencyKeyReuse()
                        ? R.string.learning_reuse : state.error != null && state.error.status == 403
                        ? R.string.learning_forbidden : R.string.learning_pending;
                retry.setVisibility(state.error != null && (state.error.status == 403 || state.error.isIdempotencyKeyReuse()) ? View.GONE : View.VISIBLE); break;
            case OFFLINE: stopAudio(); message = R.string.learning_offline; break;
            default: message = R.string.learning_loading; break;
        }
        status.setText(message);
    }
    private void renderAccepted() {
        complete.setVisibility(View.VISIBLE);
        progressStatus.setVisibility(View.VISIBLE);
        if (model.progressLoading()) progressStatus.setText(R.string.learning_progress_loading);
        else if (model.progress() != null) {
            com.example.englishaicoach.domain.model.VocabularyProgress progress = model.progress();
            int label;
            switch (progress.status) {
                case "LEARNING": label = R.string.learning_status_learning; break;
                case "REVIEWING": label = R.string.learning_status_reviewing; break;
                case "MASTERED": label = R.string.learning_status_mastered; break;
                default: label = R.string.learning_status_new; break;
            }
            progressStatus.setText(getString(R.string.learning_progress_snapshot, getString(label), progress.intervalDays,
                    progress.nextReviewAt == null ? getString(R.string.learning_progress_no_review) : progress.nextReviewAt));
        } else { progressStatus.setText(R.string.learning_progress_error); progressRetry.setVisibility(View.VISIBLE); }
    }
    private String join(String first, String second) {
        return (first == null ? "" : first) + (first != null && second != null ? "\n" : "") + (second == null ? "" : second);
    }
    private boolean validAudio(String url) {
        if (url == null) return false;
        Uri uri = Uri.parse(url); return "https".equals(uri.getScheme()) && uri.getHost() != null && uri.getUserInfo() == null;
    }
    private void play() {
        LearningSessionViewModel.State state = model.state().getValue();
        if (!authReady || state.card == null || state.phase != LearningSessionViewModel.Phase.FRONT
                && state.phase != LearningSessionViewModel.Phase.BACK || !validAudio(state.card.audioUrl)) return;
        stopAudio();
        try {
            player = new MediaPlayer(); player.setDataSource(requireContext(), Uri.parse(state.card.audioUrl));
            player.setOnPreparedListener(MediaPlayer::start);
            player.setOnCompletionListener(value -> stopAudio());
            player.setOnErrorListener((value, what, extra) -> { stopAudio(); Toast.makeText(requireContext(), R.string.learning_audio_error, Toast.LENGTH_SHORT).show(); return true; });
            player.prepareAsync();
        } catch (Exception failure) { stopAudio(); Toast.makeText(requireContext(), R.string.learning_audio_error, Toast.LENGTH_SHORT).show(); }
    }
    private void stopAudio() { if (player != null) { player.release(); player = null; } }
    private void back() {
        if (model == null) return;
        if (authReady && model.backToFront()) return;
        new MaterialAlertDialogBuilder(requireContext()).setMessage(model.hasAttempt()
                ? R.string.learning_attempt_exit_confirm : R.string.learning_exit_confirm)
                .setNegativeButton(R.string.learning_stay, (dialog, which) -> { })
                .setPositiveButton(R.string.learning_exit, (dialog, which) -> {
                    model.exit(); stopAudio(); requireActivity().getSupportFragmentManager().popBackStack();
                }).show();
    }
    @Override public void onResume() { super.onResume(); if (model != null) model.resume(); }
    @Override public void onPause() { if (model != null) model.pause(); stopAudio(); super.onPause(); }
    @Override public void onSaveInstanceState(@NonNull Bundle out) {
        super.onSaveInstanceState(out);
        if (model != null) {
            LearningSessionViewModel.Snapshot saved = model.snapshot();
            out.putString("owner", saved.owner); out.putString("event", saved.event);
            out.putString("sessionType", saved.sessionType); out.putString("origin", saved.origin);
            out.putString("completeEvent", saved.completeEvent); out.putBoolean("completed", saved.completed);
            out.putBoolean("completeRejected", saved.completeRejected);
            out.putString("vocabulary", saved.vocabulary); out.putString("server", saved.server);
            out.putBoolean("accepted", saved.accepted); out.putBoolean("conflict", saved.conflict);
            out.putBoolean("reuse", saved.reuse); out.putBoolean("rejected", saved.rejected);
            if (saved.attempt != null) {
                out.putString("attemptEvent", saved.attempt.eventId); out.putInt("attemptTime", saved.attempt.responseTimeMs);
                out.putInt("attemptQuality", saved.attempt.answerQuality);
            }
        }
    }
    @Override public void onDestroyView() {
        stopAudio(); content = null; status = null; word = null; phonetic = null;
        meaning = null; examples = null; reveal = null; retry = null; audio = null;
        qualities = null; returnToVocabulary = null; complete = null; progressStatus = null; progressRetry = null;
        super.onDestroyView();
    }
}
