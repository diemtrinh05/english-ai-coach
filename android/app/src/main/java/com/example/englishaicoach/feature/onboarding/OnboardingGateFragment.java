package com.example.englishaicoach.feature.onboarding;

import android.os.Bundle;
import android.view.*;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.*;
import com.example.englishaicoach.MainActivity;
import com.example.englishaicoach.R;
import com.example.englishaicoach.core.auth.AppSessionDependencies;
import com.example.englishaicoach.core.network.ConnectivityMonitor;
import com.example.englishaicoach.feature.auth.AuthSessionViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

public final class OnboardingGateFragment extends Fragment {
    private OnboardingGateViewModel model;
    private MaterialTextView message;
    private MaterialButton retry;
    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle saved) {
        LinearLayout layout = new LinearLayout(requireContext()); layout.setOrientation(LinearLayout.VERTICAL);
        int padding = getResources().getDimensionPixelSize(R.dimen.spacing_large);
        layout.setPadding(padding, padding, padding, padding);
        message = new MaterialTextView(requireContext()); message.setId(R.id.onboarding_gate_message); layout.addView(message);
        retry = new MaterialButton(requireContext()); retry.setId(R.id.onboarding_gate_retry);
        retry.setText(R.string.assessment_retry); layout.addView(retry);
        retry.setOnClickListener(v -> model.retry()); return layout;
    }
    @Override public void onViewCreated(@NonNull View view, Bundle saved) {
        AppSessionDependencies dependencies = AppSessionDependencies.get(requireContext());
        model = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> type) {
                return type.cast(new OnboardingGateViewModel(dependencies.sessions,
                        OnboardingGateDependencies.useCase(requireContext()),
                        androidx.core.content.ContextCompat.getMainExecutor(requireContext())));
            }
        }).get(OnboardingGateViewModel.class);
        model.state().observe(getViewLifecycleOwner(), this::render);
        ((MainActivity) requireActivity()).authState().observe(getViewLifecycleOwner(), state ->
                model.setAuthenticated(state == AuthSessionViewModel.State.AUTHENTICATED));
        new ConnectivityMonitor(requireContext()).observe(getViewLifecycleOwner(), available -> model.setOnline(Boolean.TRUE.equals(available)));
    }
    private void render(OnboardingGateViewModel.State state) {
        if (message == null) return;
        message.setText(state.phase == OnboardingGateViewModel.Phase.ERROR ? R.string.onboarding_gate_error
                : state.phase == OnboardingGateViewModel.Phase.OFFLINE ? R.string.assessment_offline
                : state.phase == OnboardingGateViewModel.Phase.EXPIRED ? R.string.learning_expired : R.string.onboarding_gate_loading);
        retry.setVisibility(state.phase == OnboardingGateViewModel.Phase.ERROR ? View.VISIBLE : View.GONE);
        if (state.phase == OnboardingGateViewModel.Phase.READY && isResumed())
            ((MainActivity) requireActivity()).routeOnboarding(state.route, model.owner(), model::routed);
    }
    @Override public void onResume() { super.onResume(); if (model != null) { model.resume(); render(model.state().getValue()); } }
    @Override public void onPause() { if (model != null) model.pause(); super.onPause(); }
    @Override public void onDestroyView() { message = null; retry = null; super.onDestroyView(); }
}
