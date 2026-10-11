package com.example.englishaicoach.feature.auth;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.englishaicoach.MainActivity;
import com.example.englishaicoach.R;
import com.example.englishaicoach.databinding.FragmentAuthBoundaryBinding;

public final class AuthBoundaryFragment extends Fragment {
    private AuthFormViewModel forms;
    private boolean online;
    public AuthBoundaryFragment() { super(R.layout.fragment_auth_boundary); }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        FragmentAuthBoundaryBinding binding = FragmentAuthBoundaryBinding.bind(view);
        MainActivity activity = (MainActivity) requireActivity();
        forms = new androidx.lifecycle.ViewModelProvider(this, new androidx.lifecycle.ViewModelProvider.Factory() {
            @NonNull @Override public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> type) {
                return type.cast(new AuthFormViewModel(com.example.englishaicoach.core.auth.AppSessionDependencies
                        .get(requireContext()).submitAuthForm, androidx.core.content.ContextCompat
                        .getMainExecutor(requireContext().getApplicationContext())));
            }
        }).get(AuthFormViewModel.class);
        new com.example.englishaicoach.core.network.ConnectivityMonitor(requireContext().getApplicationContext())
                .observe(getViewLifecycleOwner(), available -> {
                    online = Boolean.TRUE.equals(available);
                    AuthFormViewModel.State current = forms.state().getValue();
                    binding.formSubmit.setEnabled(online && current != null && !current.loading);
                });
        binding.formSwitch.setOnClickListener(v -> {
            binding.passwordInput.setText(""); binding.confirmationInput.setText("");
            forms.switchMode();
        });
        binding.formSubmit.setOnClickListener(v -> forms.submit(text(binding.nameInput), text(binding.emailInput),
                text(binding.passwordInput), text(binding.confirmationInput), online));
        androidx.activity.OnBackPressedCallback back = new androidx.activity.OnBackPressedCallback(false) {
            @Override public void handleOnBackPressed() {
                binding.passwordInput.setText(""); binding.confirmationInput.setText(""); forms.switchMode();
            }
        };
        activity.getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), back);
        forms.state().observe(getViewLifecycleOwner(), state -> { back.setEnabled(state.register); renderForm(binding, state); });
        binding.authRetry.setOnClickListener(v -> activity.retryAuth());
        activity.authState().observe(getViewLifecycleOwner(), state -> {
            boolean loading = state == AuthSessionViewModel.State.UNKNOWN || state == AuthSessionViewModel.State.LOADING;
            boolean login = state == AuthSessionViewModel.State.UNAUTHENTICATED || state == AuthSessionViewModel.State.SESSION_EXPIRED;
            binding.authLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.authTitle.setText(login ? R.string.auth_login_title : R.string.app_name);
            binding.authMessage.setText(loading ? R.string.auth_restoring :
                    state == AuthSessionViewModel.State.SESSION_EXPIRED ? R.string.auth_expired :
                    login ? R.string.auth_login_boundary : R.string.auth_restore_error);
            binding.authRetry.setVisibility(state == AuthSessionViewModel.State.ERROR ? View.VISIBLE : View.GONE);
            binding.authForm.setVisibility(login ? View.VISIBLE : View.GONE);
            if (login) binding.authTitle.setText(forms.state().getValue().register ? R.string.auth_register_title : R.string.auth_login_title);
        });
    }
    private static String text(android.widget.EditText input) { return input.getText().toString(); }
    private void renderForm(FragmentAuthBoundaryBinding binding, AuthFormViewModel.State state) {
        binding.authTitle.setText(state.register ? R.string.auth_register_title : R.string.auth_login_title);
        binding.nameLayout.setVisibility(state.register ? View.VISIBLE : View.GONE);
        binding.confirmationLayout.setVisibility(state.register ? View.VISIBLE : View.GONE);
        binding.formSubmit.setText(state.register ? R.string.auth_register_title : R.string.auth_login_title);
        binding.formSwitch.setText(state.register ? R.string.auth_switch_login : R.string.auth_switch_register);
        binding.formSubmit.setEnabled(online && !state.loading);
        binding.formSwitch.setEnabled(!state.loading);
        binding.nameInput.setEnabled(!state.loading); binding.emailInput.setEnabled(!state.loading);
        binding.passwordInput.setEnabled(!state.loading); binding.confirmationInput.setEnabled(!state.loading);
        binding.formLoading.setVisibility(state.loading ? View.VISIBLE : View.GONE);
        AuthFormValidator.Errors errors = state.errors;
        binding.nameLayout.setError(error(errors == null ? null : errors.name, R.string.auth_name_length));
        binding.emailLayout.setError(error(errors == null ? null : errors.email, R.string.auth_email_invalid));
        binding.passwordLayout.setError(error(errors == null ? null : errors.password,
                state.register ? R.string.auth_password_register_length : R.string.auth_password_login_length));
        binding.confirmationLayout.setError(error(errors == null ? null : errors.confirmation, R.string.auth_password_mismatch));
        if (state.result == null || state.result == com.example.englishaicoach.domain.repository.AuthFormsRepository.Result.SUCCESS)
            binding.formMessage.setText("");
        else binding.formMessage.setText(message(state.result));
        if (state.result == com.example.englishaicoach.domain.repository.AuthFormsRepository.Result.SUCCESS) {
            binding.passwordInput.setText(""); binding.confirmationInput.setText("");
        }
    }
    private String error(AuthFormValidator.Error error, int fallback) {
        if (error == null) return null;
        return getString(error == AuthFormValidator.Error.REQUIRED ? R.string.auth_field_required : fallback);
    }
    private static int message(com.example.englishaicoach.domain.repository.AuthFormsRepository.Result result) {
        switch (result) {
            case INVALID_CREDENTIALS: return R.string.auth_invalid_credentials;
            case LOCKED: return R.string.auth_locked;
            case EMAIL_EXISTS: return R.string.auth_email_exists;
            case VALIDATION: return R.string.auth_validation_failed;
            case RATE_LIMITED: return R.string.auth_rate_limited;
            case NETWORK: return R.string.auth_network_failed;
            case SUPERSEDED: return R.string.auth_session_changed;
            default: return R.string.auth_form_failed;
        }
    }
}
