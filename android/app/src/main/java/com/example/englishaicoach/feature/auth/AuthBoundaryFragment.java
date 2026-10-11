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
    public AuthBoundaryFragment() { super(R.layout.fragment_auth_boundary); }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        FragmentAuthBoundaryBinding binding = FragmentAuthBoundaryBinding.bind(view);
        MainActivity activity = (MainActivity) requireActivity();
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
        });
    }
}
