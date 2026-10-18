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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

public final class LearningSessionFragment extends Fragment {
    private LearningSessionViewModel model;
    private LinearLayout content;
    private MaterialTextView status, word, phonetic, meaning, examples;
    private MaterialButton reveal, retry, audio;
    private MediaPlayer player;
    private boolean authReady;
    public static LearningSessionFragment forVocabulary(String id) {
        LearningSessionFragment fragment = new LearningSessionFragment();
        Bundle args = new Bundle(); args.putString("vocabulary", id); fragment.setArguments(args); return fragment;
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
        LearningSessionViewModel.Snapshot snapshot = saved != null && saved.containsKey("event")
                ? new LearningSessionViewModel.Snapshot(saved.getString("owner"), saved.getString("event"),
                    saved.getString("vocabulary"), saved.getString("server")) : null;
        AppSessionDependencies dependencies = AppSessionDependencies.get(requireContext());
        model = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> type) {
                Handler handler = new Handler(Looper.getMainLooper());
                return type.cast(new LearningSessionViewModel(dependencies.sessions,
                        LearningDependencies.useCase(requireContext()), command -> {
                            if (Looper.myLooper() == Looper.getMainLooper()) command.run(); else handler.post(command);
                        }, SystemClock::elapsedRealtime, requireArguments().getString("vocabulary"), snapshot));
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
    }
    private void render(LearningSessionViewModel.State state) {
        hideCard(); retry.setVisibility(View.GONE);
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
                    meaning.setVisibility(View.VISIBLE); examples.setVisibility(View.VISIBLE);
                    meaning.setText(join(card.meaningEn, card.meaningVi));
                    StringBuilder detail = new StringBuilder();
                    for (VocabularyItem.Example example : card.examples) {
                        if (detail.length() > 0) detail.append("\n\n"); detail.append(join(example.text, example.translation));
                    }
                    examples.setText(detail.toString());
                }
                break;
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
        new MaterialAlertDialogBuilder(requireContext()).setMessage(R.string.learning_exit_confirm)
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
            out.putString("vocabulary", saved.vocabulary); out.putString("server", saved.server);
        }
    }
    @Override public void onDestroyView() {
        stopAudio(); content = null; status = null; word = null; phonetic = null;
        meaning = null; examples = null; reveal = null; retry = null; audio = null;
        super.onDestroyView();
    }
}
