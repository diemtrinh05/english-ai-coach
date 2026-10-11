package com.example.englishaicoach.feature.onboarding;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.*;
import com.example.englishaicoach.MainActivity;
import com.example.englishaicoach.R;
import com.example.englishaicoach.core.auth.AppSessionDependencies;
import com.example.englishaicoach.core.network.*;
import com.example.englishaicoach.domain.model.OnboardingData.*;
import com.example.englishaicoach.databinding.FragmentOnboardingBinding;

public final class OnboardingFragment extends Fragment {
    private OnboardingViewModel model;
    private boolean online;
    private boolean rendering;
    public OnboardingFragment() { super(R.layout.fragment_onboarding); }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        FragmentOnboardingBinding binding = FragmentOnboardingBinding.bind(view);
        AppSessionDependencies dependencies = AppSessionDependencies.get(requireContext());
        model = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> type) {
                return type.cast(new OnboardingViewModel(dependencies.configureOnboarding,
                        dependencies.sessions, androidx.core.content.ContextCompat.getMainExecutor(requireContext().getApplicationContext())));
            }
        }).get(OnboardingViewModel.class);
        new ConnectivityMonitor(requireContext().getApplicationContext()).observe(getViewLifecycleOwner(), available -> {
            online = Boolean.TRUE.equals(available); render(binding, model.state().getValue());
        });
        binding.onboardingBack.setOnClickListener(v -> {
            capture(binding);
            if (model.step > 0) { model.step--; render(binding, model.state().getValue()); }
            else requireActivity().getOnBackPressedDispatcher().onBackPressed();
        });
        binding.onboardingNext.setOnClickListener(v -> {
            capture(binding);
            if (model.step == 0 && !model.validProfileFields() || model.step == 1 && !model.validGoals()) {
                binding.onboardingMessage.setText(R.string.onboarding_validation); return;
            }
            if (model.step < 2) { model.step++; render(binding, model.state().getValue()); }
            else model.save(online);
        });
        binding.onboardingRetry.setOnClickListener(v -> model.load());
        binding.onboardingDone.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
        android.text.TextWatcher watcher = new android.text.TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            public void afterTextChanged(android.text.Editable value) { if (!rendering) capture(binding); }
        };
        binding.onboardingName.addTextChangedListener(watcher);
        binding.onboardingZone.addTextChangedListener(watcher);
        binding.onboardingMinutes.addTextChangedListener(watcher);
        binding.onboardingZoneConfirm.setOnCheckedChangeListener((button, checked) -> { if (!rendering) capture(binding); });
        model.state().observe(getViewLifecycleOwner(), state -> render(binding, state));
        model.load();
    }
    private void capture(FragmentOnboardingBinding binding) {
        model.name = binding.onboardingName.getText().toString();
        model.zone = binding.onboardingZone.getText().toString();
        model.zoneConfirmed = binding.onboardingZoneConfirm.isChecked();
        model.minutes = binding.onboardingMinutes.getText().toString();
    }
    private void render(FragmentOnboardingBinding binding, OnboardingViewModel.State state) {
        rendering = true;
        boolean busy = state.phase == OnboardingViewModel.Phase.LOADING || state.phase == OnboardingViewModel.Phase.SAVING;
        boolean editing = state.phase == OnboardingViewModel.Phase.EDITING || state.phase == OnboardingViewModel.Phase.SAVING;
        binding.onboardingLoading.setVisibility(busy ? View.VISIBLE : View.GONE);
        binding.onboardingForm.setVisibility(editing ? View.VISIBLE : View.GONE);
        binding.onboardingSuccess.setVisibility(state.phase == OnboardingViewModel.Phase.SUCCESS ? View.VISIBLE : View.GONE);
        binding.onboardingRetry.setVisibility(state.phase == OnboardingViewModel.Phase.ERROR ? View.VISIBLE : View.GONE);
        binding.onboardingRetry.setEnabled(online && !busy);
        binding.onboardingProfile.setVisibility(model.step == 0 ? View.VISIBLE : View.GONE);
        binding.onboardingGoals.setVisibility(model.step == 1 ? View.VISIBLE : View.GONE);
        binding.onboardingDaily.setVisibility(model.step == 2 ? View.VISIBLE : View.GONE);
        binding.onboardingNext.setText(model.step == 2 ? R.string.onboarding_save : R.string.onboarding_continue);
        binding.onboardingNext.setEnabled(!busy && online && (model.step != 1 || model.validGoals()));
        binding.onboardingBack.setEnabled(!busy);
        binding.onboardingName.setEnabled(!busy); binding.onboardingZone.setEnabled(!busy);
        binding.onboardingZoneConfirm.setEnabled(!busy); binding.onboardingMinutes.setEnabled(!busy);
        binding.onboardingName.setText(model.name); binding.onboardingZone.setText(model.zone);
        binding.onboardingZoneConfirm.setChecked(model.zoneConfirmed); binding.onboardingMinutes.setText(model.minutes);
        binding.onboardingGoalItems.removeAllViews();
        if (state.snapshot != null) for (Goal goal : state.snapshot.goals) {
            String displayName = goalName(goal.name);
            LinearLayout row = new LinearLayout(requireContext()); row.setOrientation(LinearLayout.VERTICAL);
            CheckBox choose = new CheckBox(requireContext()); choose.setMinHeight(dp(48));
            choose.setSaveEnabled(false);
            choose.setText(goal.active ? displayName : getString(R.string.onboarding_inactive_goal, displayName));
            choose.setChecked(model.selected.contains(goal.id)); choose.setEnabled(!busy && (goal.active || model.selected.contains(goal.id)));
            choose.setOnCheckedChangeListener((button, checked) -> {
                if (checked) model.selected.add(goal.id); else { model.selected.remove(goal.id); if (goal.id.equals(model.primary)) model.primary = null; }
                capture(binding); model.publishDraft();
            });
            row.addView(choose);
            CheckBox primary = new CheckBox(requireContext()); primary.setMinHeight(dp(48));
            primary.setSaveEnabled(false);
            primary.setText(getString(R.string.onboarding_primary_goal, displayName)); primary.setChecked(goal.id.equals(model.primary));
            primary.setEnabled(!busy && goal.active && model.selected.contains(goal.id));
            primary.setOnCheckedChangeListener((button, checked) -> { model.primary = checked ? goal.id : null; capture(binding); model.publishDraft(); });
            row.addView(primary); binding.onboardingGoalItems.addView(row);
        }
        binding.onboardingTimeChoices.removeAllViews();
        for (int value : new int[]{5, 10, 20, 30, 45, 60}) {
            com.google.android.material.button.MaterialButton button = new com.google.android.material.button.MaterialButton(requireContext());
            button.setMinHeight(dp(48)); button.setText(getString(R.string.onboarding_minutes_choice, value)); button.setEnabled(!busy);
            button.setOnClickListener(v -> { binding.onboardingMinutes.setText(Integer.toString(value)); capture(binding); });
            binding.onboardingTimeChoices.addView(button);
        }
        if (state.validation) binding.onboardingMessage.setText(R.string.onboarding_validation);
        else if (state.result != null && !state.result.success) binding.onboardingMessage.setText(
                state.result.profileSaved ? R.string.onboarding_partial : message(state.result.error));
        else if (state.snapshot != null && state.snapshot.goals.stream().noneMatch(goal -> goal.active))
            binding.onboardingMessage.setText(R.string.onboarding_empty);
        else binding.onboardingMessage.setText(online ? R.string.onboarding_intro : R.string.onboarding_offline);
        rendering = false;
    }
    private String goalName(String name) {
        switch (name) {
            case "GENERAL_ENGLISH": return getString(R.string.onboarding_goal_general);
            case "TRAVEL": return getString(R.string.onboarding_goal_travel);
            case "BUSINESS": return getString(R.string.onboarding_goal_business);
            case "COMMUNICATION": return getString(R.string.onboarding_goal_communication);
            case "ACADEMIC": return getString(R.string.onboarding_goal_academic);
            case "IELTS": return getString(R.string.onboarding_goal_ielts);
            case "TOEIC": return getString(R.string.onboarding_goal_toeic);
            default: return name;
        }
    }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
    private static int message(ApiError error) {
        if (error == null) return R.string.onboarding_error;
        switch (error.kind) {
            case VALIDATION: return R.string.onboarding_server_validation;
            case UNAUTHORIZED: return R.string.auth_expired;
            case FORBIDDEN: return R.string.onboarding_forbidden;
            case NOT_FOUND: return R.string.onboarding_not_found;
            case RATE_LIMITED: return R.string.auth_rate_limited;
            case OFFLINE: return R.string.onboarding_network;
            default: return R.string.onboarding_error;
        }
    }
}
