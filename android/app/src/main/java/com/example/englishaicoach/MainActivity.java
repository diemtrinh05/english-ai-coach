package com.example.englishaicoach;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.englishaicoach.core.navigation.FoundationDestinationFragment;
import com.example.englishaicoach.core.navigation.MainDestination;
import com.example.englishaicoach.core.navigation.MainNavigationViewModel;
import com.example.englishaicoach.core.network.ConnectivityMonitor;
import com.example.englishaicoach.databinding.ActivityMainBinding;
import com.example.englishaicoach.feature.vocabulary.VocabularyDetailFragment;
import com.example.englishaicoach.feature.vocabulary.VocabularyListFragment;

public final class MainActivity extends AppCompatActivity {
    private static final String STATE_DESTINATION = "selected_destination";
    private ActivityMainBinding binding;
    private MainNavigationViewModel navigationViewModel;
    private boolean openingLearning;
    private com.example.englishaicoach.feature.auth.AuthSessionViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        com.example.englishaicoach.core.auth.AppSessionDependencies dependencies =
                com.example.englishaicoach.core.auth.AppSessionDependencies.get(this);
        authViewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull @Override public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> type) {
                return type.cast(new com.example.englishaicoach.feature.auth.AuthSessionViewModel(dependencies.sessions,
                        dependencies.restoreSession));
            }
        }).get(com.example.englishaicoach.feature.auth.AuthSessionViewModel.class);

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
        navigationViewModel.getDestination().observe(this, destination -> {
            if (authViewModel.state().getValue() == com.example.englishaicoach.feature.auth.AuthSessionViewModel.State.AUTHENTICATED)
                renderDestination(destination);
        });
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            openingLearning = getSupportFragmentManager().findFragmentById(R.id.main_content)
                    instanceof com.example.englishaicoach.feature.learning.LearningSessionFragment;
            if (binding != null) binding.mainNavigation.setVisibility(
                    authViewModel.state().getValue() == com.example.englishaicoach.feature.auth.AuthSessionViewModel.State.AUTHENTICATED
                    && getSupportFragmentManager().getBackStackEntryCount() == 0
                            ? View.VISIBLE : View.GONE);
        });
        binding.mainNavigation.setVisibility(View.GONE);
        authViewModel.state().observe(this, state -> {
            boolean authenticated = state == com.example.englishaicoach.feature.auth.AuthSessionViewModel.State.AUTHENTICATED;
            binding.mainNavigation.setVisibility(authenticated
                    && getSupportFragmentManager().getBackStackEntryCount() == 0 ? View.VISIBLE : View.GONE);
            Fragment preserved = getSupportFragmentManager().findFragmentById(R.id.main_content);
            if (!authenticated && preserved instanceof com.example.englishaicoach.feature.learning.LearningSessionFragment
                    && (state == com.example.englishaicoach.feature.auth.AuthSessionViewModel.State.UNKNOWN
                    || state == com.example.englishaicoach.feature.auth.AuthSessionViewModel.State.LOADING
                    || state == com.example.englishaicoach.feature.auth.AuthSessionViewModel.State.ERROR)) {
                if (state == com.example.englishaicoach.feature.auth.AuthSessionViewModel.State.UNKNOWN) authViewModel.restore();
                return;
            }
            if (authenticated) {
                Fragment current = getSupportFragmentManager().findFragmentById(R.id.main_content);
                if (current == null || current instanceof com.example.englishaicoach.feature.auth.AuthBoundaryFragment)
                    renderDestination(navigationViewModel.getDestination().getValue());
            } else {
                Fragment current = getSupportFragmentManager().findFragmentById(R.id.main_content);
                if (!(current instanceof com.example.englishaicoach.feature.auth.AuthBoundaryFragment)) {
                    getSupportFragmentManager().popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_content,
                            new com.example.englishaicoach.feature.auth.AuthBoundaryFragment(), "AUTH_BOUNDARY").commit();
                }
                if (state == com.example.englishaicoach.feature.auth.AuthSessionViewModel.State.UNKNOWN) authViewModel.restore();
            }
        });
        new ConnectivityMonitor(getApplicationContext()).observe(this, available ->
                binding.offlineBanner.setVisibility(Boolean.TRUE.equals(available)
                        ? View.GONE : View.VISIBLE));
    }

    private void renderDestination(MainDestination destination) {
        if (binding.mainNavigation.getSelectedItemId() != destination.getMenuId()) {
            binding.mainNavigation.setSelectedItemId(destination.getMenuId());
        }
        Fragment current = getSupportFragmentManager().findFragmentById(R.id.main_content);
        if (current instanceof com.example.englishaicoach.feature.learning.LearningSessionFragment
                && getSupportFragmentManager().getBackStackEntryCount() > 0) return;
        // Observer HOME khi recreate không được thay thế flow thiết lập đang mở.
        if (destination == MainDestination.HOME
                && current instanceof com.example.englishaicoach.feature.onboarding.OnboardingFragment
                && getSupportFragmentManager().getBackStackEntryCount() > 0) return;
        if (destination == MainDestination.LEARN
                && getSupportFragmentManager().getBackStackEntryCount() > 0) return;
        if (current == null || !destination.name().equals(current.getTag())) {
            getSupportFragmentManager().popBackStack(null,
                    androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_content,
                            FoundationDestinationFragment.forDestination(destination),
                            destination.name())
                    .commit();
        }
    }

    public void openOnboarding() {
        if (authViewModel.state().getValue() != com.example.englishaicoach.feature.auth.AuthSessionViewModel.State.AUTHENTICATED) return;
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_content, new com.example.englishaicoach.feature.onboarding.OnboardingFragment(), "ONBOARDING")
                .addToBackStack("ONBOARDING").commit();
    }

    public void openVocabulary() {
        if (authViewModel.state().getValue() != com.example.englishaicoach.feature.auth.AuthSessionViewModel.State.AUTHENTICATED) return;
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_content, new VocabularyListFragment(), "VOCABULARY_LIST")
                .addToBackStack("VOCABULARY_LIST")
                .commit();
    }

    public void openLearning(String id) {
        if (openingLearning || getSupportFragmentManager().isStateSaved()) return;
        if (authViewModel.state().getValue() != com.example.englishaicoach.feature.auth.AuthSessionViewModel.State.AUTHENTICATED) return;
        Fragment current = getSupportFragmentManager().findFragmentById(R.id.main_content);
        if (current instanceof com.example.englishaicoach.feature.learning.LearningSessionFragment) return;
        openingLearning = true;
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_content, com.example.englishaicoach.feature.learning.LearningSessionFragment.forVocabulary(id), "LEARNING")
                .addToBackStack("LEARNING").commit();
    }

    public void openVocabularyDetail(String id) {
        if (authViewModel.state().getValue() != com.example.englishaicoach.feature.auth.AuthSessionViewModel.State.AUTHENTICATED) return;
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_content, VocabularyDetailFragment.forId(id), "VOCABULARY_DETAIL")
                .addToBackStack("VOCABULARY_DETAIL")
                .commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        MainDestination destination = navigationViewModel.getDestination().getValue();
        outState.putString(STATE_DESTINATION,
                destination == null ? MainDestination.HOME.name() : destination.name());
        super.onSaveInstanceState(outState);
    }

    public androidx.lifecycle.LiveData<com.example.englishaicoach.feature.auth.AuthSessionViewModel.State> authState() {
        return authViewModel.state();
    }
    public void retryAuth() { authViewModel.restore(); }

    @Override
    protected void onDestroy() {
        binding = null;
        super.onDestroy();
    }
}
