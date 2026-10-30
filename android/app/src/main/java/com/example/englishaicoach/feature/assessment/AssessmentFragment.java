package com.example.englishaicoach.feature.assessment;

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
import com.example.englishaicoach.data.local.EncryptedAssessmentCheckpointStore;
import com.example.englishaicoach.feature.auth.AuthSessionViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

public final class AssessmentFragment extends Fragment {
    private AssessmentViewModel model;
    private LinearLayout content;
    private MaterialTextView status, prompt, count;
    private RadioGroup options;
    private MaterialButton start, submit, retry, finish, continueResult;
    private boolean authReady;
    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle saved) {
        ScrollView scroll = new ScrollView(requireContext());
        content = new LinearLayout(requireContext()); content.setOrientation(LinearLayout.VERTICAL);
        int padding = getResources().getDimensionPixelSize(R.dimen.spacing_large);
        content.setPadding(padding, padding, padding, padding); scroll.addView(content);
        button(R.string.learning_back).setOnClickListener(v -> back());
        status = text(); status.setId(R.id.assessment_status);
        count = text(); prompt = text(); prompt.setId(R.id.assessment_prompt);
        options = new RadioGroup(requireContext()); options.setOrientation(RadioGroup.VERTICAL); content.addView(options);
        start = button(R.string.assessment_start); start.setId(R.id.assessment_start); start.setOnClickListener(v -> model.start());
        submit = button(R.string.assessment_submit); submit.setId(R.id.assessment_submit); submit.setOnClickListener(v -> model.submit());
        retry = button(R.string.assessment_retry); retry.setId(R.id.assessment_retry); retry.setOnClickListener(v -> {
            if (authReady) model.retry(); else ((MainActivity) requireActivity()).retryAuth();
        });
        finish = button(R.string.assessment_close); finish.setOnClickListener(v -> back());
        continueResult = button(R.string.assessment_result_continue); continueResult.setId(R.id.assessment_result_continue);
        continueResult.setOnClickListener(v -> model.continueResult());
        return scroll;
    }
    private MaterialTextView text() {
        MaterialTextView view = new MaterialTextView(requireContext());
        view.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyLarge);
        content.addView(view, new LinearLayout.LayoutParams(-1, -2)); return view;
    }
    private MaterialButton button(int label) {
        MaterialButton view = new MaterialButton(requireContext()); view.setText(label); content.addView(view); return view;
    }
    @Override public void onViewCreated(@NonNull View view, Bundle saved) {
        AppSessionDependencies dependencies = AppSessionDependencies.get(requireContext());
        model = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> type) {
                Handler handler = new Handler(Looper.getMainLooper());
                return type.cast(new AssessmentViewModel(dependencies.sessions,
                        new EncryptedAssessmentCheckpointStore(requireContext()),
                        AssessmentDependencies.useCase(requireContext()),
                        com.example.englishaicoach.feature.onboarding.OnboardingGateDependencies.useCase(requireContext()),
                        command -> { if (Looper.myLooper() == Looper.getMainLooper()) command.run(); else handler.post(command); },
                        SystemClock::elapsedRealtime));
            }
        }).get(AssessmentViewModel.class);
        model.state().observe(getViewLifecycleOwner(), state -> { if (authReady) render(state); });
        ((MainActivity) requireActivity()).authState().observe(getViewLifecycleOwner(), state -> {
            authReady = state == AuthSessionViewModel.State.AUTHENTICATED;
            model.setAuthenticated(authReady);
            if (authReady) render(model.state().getValue());
            else {
                hide(); status.setText(state == AuthSessionViewModel.State.ERROR ? R.string.learning_auth_error : R.string.learning_loading);
                retry.setVisibility(state == AuthSessionViewModel.State.ERROR ? View.VISIBLE : View.GONE);
            }
        });
        new ConnectivityMonitor(requireContext()).observe(getViewLifecycleOwner(), value -> model.setOnline(Boolean.TRUE.equals(value)));
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() { back(); }
        });
    }
    private void hide() {
        for (View v : new View[]{count, prompt, options, start, submit, retry, finish, continueResult}) v.setVisibility(View.GONE);
        options.setOnCheckedChangeListener(null); options.removeAllViews();
    }
    private void render(AssessmentViewModel.State state) {
        hide(); int label;
        switch (state.phase) {
            case INTRO: label = R.string.assessment_intro; start.setVisibility(View.VISIBLE); break;
            case QUESTION:
                label = R.string.assessment_question;
                count.setText(getString(R.string.assessment_count, state.answered)); count.setVisibility(View.VISIBLE);
                prompt.setText(state.question.text); prompt.setVisibility(View.VISIBLE);
                options.setVisibility(View.VISIBLE);
                for (String value : state.question.options) {
                    RadioButton choice = new RadioButton(requireContext()); choice.setId(View.generateViewId());
                    choice.setText(value); choice.setMinHeight((int) (48 * getResources().getDisplayMetrics().density));
                    options.addView(choice); if (value.equals(state.selected)) options.check(choice.getId());
                    choice.setOnClickListener(v -> model.select(value));
                }
                submit.setVisibility(View.VISIBLE); submit.setEnabled(state.selected != null); break;
            case PENDING: label = R.string.assessment_pending; retry.setVisibility(View.VISIBLE); break;
            case ERROR: label = R.string.assessment_error; retry.setVisibility(View.VISIBLE); break;
            case OFFLINE: label = R.string.assessment_offline; break;
            case SUBMITTING: label = R.string.assessment_submitting; break;
            case COMPLETED:
                label = R.string.assessment_result_title;
                AssessmentResultRenderer.render(requireContext(), prompt, count, state.result);
                continueResult.setVisibility(View.VISIBLE); finish.setVisibility(View.VISIBLE); break;
            case ROUTE:
                label = R.string.onboarding_gate_loading;
                if (isResumed()) ((MainActivity) requireActivity()).routeOnboarding(state.route, model.owner(), model::routeConsumed);
                break;
            case CLOSED: label = R.string.assessment_closed; finish.setVisibility(View.VISIBLE); break;
            case UNKNOWN: label = R.string.assessment_unknown; finish.setVisibility(View.VISIBLE); break;
            case REUSE: label = R.string.assessment_reuse; finish.setVisibility(View.VISIBLE); break;
            case STORAGE: label = R.string.assessment_storage; finish.setVisibility(View.VISIBLE); break;
            case OVERFLOW: label = R.string.assessment_overflow; finish.setVisibility(View.VISIBLE); break;
            case EXPIRED: label = R.string.learning_expired; break;
            case REJECTED: label = R.string.assessment_rejected; finish.setVisibility(View.VISIBLE); break;
            default: label = R.string.learning_loading; break;
        }
        if (state.error != null && "ASSESSMENT_CONTENT_UNAVAILABLE".equals(state.error.code))
            label = state.phase == AssessmentViewModel.Phase.PENDING ? R.string.assessment_content : R.string.assessment_content_read;
        status.setText(label);
    }
    private void back() {
        if (model == null) return;
        new MaterialAlertDialogBuilder(requireContext()).setMessage(R.string.assessment_exit_confirm)
                .setNegativeButton(R.string.learning_stay, (dialog, which) -> { })
                .setPositiveButton(R.string.learning_exit, (dialog, which) -> {
                    model.exit();
                    if (requireActivity().getSupportFragmentManager().getBackStackEntryCount() > 0)
                        requireActivity().getSupportFragmentManager().popBackStack();
                    else requireActivity().finish();
                }).show();
    }
    @Override public void onResume() { super.onResume(); if (model != null) { model.resume(); if (authReady) render(model.state().getValue()); } }
    @Override public void onPause() { if (model != null) model.pause(); super.onPause(); }
    @Override public void onDestroyView() {
        content = null; status = null; prompt = null; count = null; options = null;
        start = null; submit = null; retry = null; finish = null; continueResult = null; super.onDestroyView();
    }
}
