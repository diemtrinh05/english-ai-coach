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
    private static final String STATE_NAVIGATION_OWNER = "navigation_auth_owner";
    private ActivityMainBinding binding;
    private MainNavigationViewModel navigationViewModel;
    private boolean openingLearning;
    private boolean openingAssessment;
    private boolean onboardingNavigationPending;
    private boolean routingOnboarding;
    private String navigationOwner;
    private com.example.englishaicoach.feature.auth.AuthSessionViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        com.example.englishaicoach.core.auth.AppSessionDependencies dependencies =
                com.example.englishaicoach.core.auth.AppSessionDependencies.get(this);
        com.example.englishaicoach.core.auth.TokenSession initialToken = dependencies.sessions.current();
        navigationOwner = savedInstanceState == null ? (initialToken == null ? null : initialToken.sessionId())
                : savedInstanceState.getString(STATE_NAVIGATION_OWNER);
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
            if (!routingOnboarding && authViewModel.state().getValue() == com.example.englishaicoach.feature.auth.AuthSessionViewModel.State.AUTHENTICATED)
                renderDestination(destination);
        });
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            openingAssessment = getSupportFragmentManager().findFragmentById(R.id.main_content)
                    instanceof com.example.englishaicoach.feature.assessment.AssessmentFragment;
            openingLearning = getSupportFragmentManager().findFragmentById(R.id.main_content)
                    instanceof com.example.englishaicoach.feature.learning.LearningSessionFragment;
            if (binding != null) binding.mainNavigation.setVisibility(
                    authViewModel.state().getValue() == com.example.englishaicoach.feature.auth.AuthSessionViewModel.State.AUTHENTICATED
                    && navigationVisible()
                            ? View.VISIBLE : View.GONE);
        });
        binding.mainNavigation.setVisibility(View.GONE);
        authViewModel.state().observe(this, state -> {
            com.example.englishaicoach.core.auth.TokenSession currentToken = dependencies.sessions.current();
            String currentOwner = currentToken == null ? null : currentToken.sessionId();
            boolean sameOwner = currentOwner != null && currentOwner.equals(navigationOwner);
            boolean authenticated = currentOwner != null && state == com.example.englishaicoach.feature.auth.AuthSessionViewModel.State.AUTHENTICATED;
            binding.mainNavigation.setVisibility(authenticated
                    && navigationVisible() ? View.VISIBLE : View.GONE);
            Fragment preserved = getSupportFragmentManager().findFragmentById(R.id.main_content);
            if (!authenticated && (preserved instanceof com.example.englishaicoach.feature.learning.LearningSessionFragment
                    || preserved instanceof com.example.englishaicoach.feature.assessment.AssessmentFragment)
                    && sameOwner
                    && (state == com.example.englishaicoach.feature.auth.AuthSessionViewModel.State.UNKNOWN
                    || state == com.example.englishaicoach.feature.auth.AuthSessionViewModel.State.LOADING
                    || state == com.example.englishaicoach.feature.auth.AuthSessionViewModel.State.ERROR)) {
                if (state == com.example.englishaicoach.feature.auth.AuthSessionViewModel.State.UNKNOWN) authViewModel.restore();
                return;
            }
            if (authenticated) {
                Fragment current = getSupportFragmentManager().findFragmentById(R.id.main_content);
                navigationOwner = currentOwner;
                if (!sameOwner || current == null || current instanceof com.example.englishaicoach.feature.auth.AuthBoundaryFragment
                        || current instanceof FoundationDestinationFragment) recheckOnboarding();
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
        if (current instanceof com.example.englishaicoach.feature.onboarding.OnboardingGateFragment) return;
        if (current instanceof com.example.englishaicoach.feature.learning.LearningSessionFragment
                && getSupportFragmentManager().getBackStackEntryCount() > 0) return;
        if (current instanceof com.example.englishaicoach.feature.assessment.AssessmentFragment
                && getSupportFragmentManager().getBackStackEntryCount() > 0) return;
        // Observer HOME khi recreate không được thay thế flow thiết lập đang mở.
        if (destination == MainDestination.HOME
                && current instanceof com.example.englishaicoach.feature.onboarding.OnboardingFragment) return;
        if (destination == MainDestination.LEARN
                && getSupportFragmentManager().getBackStackEntryCount() > 0) return;
        if (current == null || !destination.name().equals(current.getTag())) {
            getSupportFragmentManager().popBackStack(null,
                    androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_content,
                            destination == MainDestination.REVIEW ? new com.example.englishaicoach.feature.review.ReviewFragment()
                                    : FoundationDestinationFragment.forDestination(destination),
                            destination.name())
                    .commit();
        }
    }

    private boolean navigationVisible() {
        Fragment current = getSupportFragmentManager().findFragmentById(R.id.main_content);
        return getSupportFragmentManager().getBackStackEntryCount() == 0
                && !(current instanceof com.example.englishaicoach.feature.onboarding.OnboardingGateFragment)
                && !(current instanceof com.example.englishaicoach.feature.onboarding.OnboardingFragment)
                && !(current instanceof com.example.englishaicoach.feature.assessment.AssessmentFragment);
    }

    public void recheckOnboarding() {
        com.example.englishaicoach.core.auth.TokenSession token =
                com.example.englishaicoach.core.auth.AppSessionDependencies.get(this).sessions.current();
        if (token != null) recheckOnboarding(token.sessionId());
    }

    public void recheckOnboarding(String owner) {
        if (getSupportFragmentManager().findFragmentById(R.id.main_content)
                instanceof com.example.englishaicoach.feature.onboarding.OnboardingGateFragment) return;
        scheduleOnboarding(new com.example.englishaicoach.feature.onboarding.OnboardingGateFragment(),
                "ONBOARDING_GATE", owner, false, () -> { });
    }

    public boolean routeOnboarding(com.example.englishaicoach.domain.model.OnboardingRoute route,
            String owner, Runnable consumed) {
        if (route == null) return false;
        Fragment target = route == com.example.englishaicoach.domain.model.OnboardingRoute.SETTINGS
                ? new com.example.englishaicoach.feature.onboarding.OnboardingFragment()
                : route == com.example.englishaicoach.domain.model.OnboardingRoute.PLACEMENT
                ? new com.example.englishaicoach.feature.assessment.AssessmentFragment()
                : FoundationDestinationFragment.forDestination(MainDestination.HOME);
        return scheduleOnboarding(target, route == com.example.englishaicoach.domain.model.OnboardingRoute.HOME ? "HOME" : route.name(),
                owner, route == com.example.englishaicoach.domain.model.OnboardingRoute.HOME, consumed);
    }

    private boolean scheduleOnboarding(Fragment target, String tag, String owner, boolean home, Runnable consumed) {
        com.example.englishaicoach.core.auth.SessionManager sessions =
                com.example.englishaicoach.core.auth.AppSessionDependencies.get(this).sessions;
        synchronized (sessions) {
            if (onboardingNavigationPending || !sessions.isCurrentSession(owner) || getSupportFragmentManager().isStateSaved()
                    || authViewModel.state().getValue() != com.example.englishaicoach.feature.auth.AuthSessionViewModel.State.AUTHENTICATED) return false;
            Fragment source = getSupportFragmentManager().findFragmentById(R.id.main_content);
            onboardingNavigationPending = true;
            // Commit trên lượt main riêng; kiểm owner/source lần nữa ngay khi tiêu thụ để bỏ route cũ đã queue.
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                synchronized (sessions) {
                    try {
                        if (isFinishing() || isDestroyed() || !sessions.isCurrentSession(owner) || getSupportFragmentManager().isStateSaved()
                                || authViewModel.state().getValue() != com.example.englishaicoach.feature.auth.AuthSessionViewModel.State.AUTHENTICATED
                                || getSupportFragmentManager().findFragmentById(R.id.main_content) != source) return;
                        routingOnboarding = true;
                        getSupportFragmentManager().popBackStackImmediate(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        if (home) {
                            navigationViewModel.select(MainDestination.HOME);
                            binding.mainNavigation.setSelectedItemId(MainDestination.HOME.getMenuId());
                        }
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_content, target, tag).commitNow();
                        binding.mainNavigation.setVisibility(home ? View.VISIBLE : View.GONE);
                        consumed.run();
                    } finally { routingOnboarding = false; onboardingNavigationPending = false; }
                }
            });
            return true;
        }
    }

    public void openAssessment() {
        if (openingAssessment || getSupportFragmentManager().isStateSaved()
                || authViewModel.state().getValue() != com.example.englishaicoach.feature.auth.AuthSessionViewModel.State.AUTHENTICATED
                || getSupportFragmentManager().findFragmentById(R.id.main_content) instanceof com.example.englishaicoach.feature.assessment.AssessmentFragment) return;
        openingAssessment = true;
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_content, new com.example.englishaicoach.feature.assessment.AssessmentFragment(), "ASSESSMENT")
                .addToBackStack("ASSESSMENT").commit();
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

    public void openLearning(String id) { openLearning(id, "MIXED", "DETAIL"); }
    public void openReviewLearning(String id) { openLearning(id, "REVIEW", "REVIEW"); }
    private void openLearning(String id, String type, String origin) {
        if (openingLearning || getSupportFragmentManager().isStateSaved()) return;
        if (authViewModel.state().getValue() != com.example.englishaicoach.feature.auth.AuthSessionViewModel.State.AUTHENTICATED) return;
        Fragment current = getSupportFragmentManager().findFragmentById(R.id.main_content);
        if (current instanceof com.example.englishaicoach.feature.learning.LearningSessionFragment) return;
        openingLearning = true;
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_content, com.example.englishaicoach.feature.learning.LearningSessionFragment.forVocabulary(id, type, origin), "LEARNING")
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
        // Chỉ lưu UUID owner để không phục hồi private navigation của phiên trước dưới tài khoản mới.
        outState.putString(STATE_NAVIGATION_OWNER, navigationOwner);
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
