package com.example.englishaicoach;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.englishaicoach.core.navigation.FoundationDestinationFragment;
import com.example.englishaicoach.core.navigation.MainDestination;
import com.example.englishaicoach.core.navigation.MainNavigationViewModel;
import com.example.englishaicoach.core.network.ConnectivityMonitor;
import com.example.englishaicoach.databinding.ActivityMainBinding;

public final class MainActivity extends AppCompatActivity {
    private static final String STATE_DESTINATION = "selected_destination";
    private ActivityMainBinding binding;
    private MainNavigationViewModel navigationViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        navigationViewModel = new ViewModelProvider(this).get(MainNavigationViewModel.class);
        if (savedInstanceState != null) {
            String name = savedInstanceState.getString(STATE_DESTINATION);
            if (name != null) {
                navigationViewModel.select(MainDestination.valueOf(name));
            }
        }

        binding.mainNavigation.setOnItemSelectedListener(item -> {
            navigationViewModel.select(MainDestination.fromMenuId(item.getItemId()));
            return true;
        });
        navigationViewModel.getDestination().observe(this, this::renderDestination);
        new ConnectivityMonitor(getApplicationContext()).observe(this, available ->
                binding.offlineBanner.setVisibility(Boolean.TRUE.equals(available)
                        ? View.GONE : View.VISIBLE));
    }

    private void renderDestination(MainDestination destination) {
        if (binding.mainNavigation.getSelectedItemId() != destination.getMenuId()) {
            binding.mainNavigation.setSelectedItemId(destination.getMenuId());
        }
        Fragment current = getSupportFragmentManager().findFragmentById(R.id.main_content);
        if (current == null || !destination.name().equals(current.getTag())) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_content,
                            FoundationDestinationFragment.forDestination(destination),
                            destination.name())
                    .commit();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        MainDestination destination = navigationViewModel.getDestination().getValue();
        outState.putString(STATE_DESTINATION,
                destination == null ? MainDestination.HOME.name() : destination.name());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        binding = null;
        super.onDestroy();
    }
}
