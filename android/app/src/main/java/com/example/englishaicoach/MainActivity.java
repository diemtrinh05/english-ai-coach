package com.example.englishaicoach;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.englishaicoach.core.ui.AppUiState;
import com.example.englishaicoach.core.ui.AppViewModel;
import com.example.englishaicoach.databinding.ActivityMainBinding;

public final class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        AppViewModel viewModel = new ViewModelProvider(this).get(AppViewModel.class);
        viewModel.getUiState().observe(this, this::render);
    }

    private void render(AppUiState state) {
        binding.bootstrapStatus.setText(
                state == AppUiState.READY
                        ? R.string.bootstrap_status_ready
                        : R.string.bootstrap_status_initializing
        );
    }

    @Override
    protected void onDestroy() {
        binding = null;
        super.onDestroy();
    }
}
