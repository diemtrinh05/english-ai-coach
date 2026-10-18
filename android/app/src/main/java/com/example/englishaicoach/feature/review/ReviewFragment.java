package com.example.englishaicoach.feature.review;
import android.os.*;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.*;
import com.example.englishaicoach.*;
import com.example.englishaicoach.core.auth.AppSessionDependencies;
import com.example.englishaicoach.core.network.ConnectivityMonitor;
import com.example.englishaicoach.domain.model.ReviewItem;
import com.example.englishaicoach.feature.learning.LearningDependencies;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
public final class ReviewFragment extends Fragment {
    private ReviewViewModel model;
    private LinearLayout content, rows;
    private MaterialTextView status;
    private MaterialButton retry;
    private boolean authReady;
    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle saved) {
        ScrollView scroll = new ScrollView(requireContext()); content = new LinearLayout(requireContext());
        content.setOrientation(LinearLayout.VERTICAL); int padding = getResources().getDimensionPixelSize(R.dimen.spacing_large);
        content.setPadding(padding, padding, padding, padding); scroll.addView(content);
        MaterialTextView title = new MaterialTextView(requireContext()); title.setText(R.string.review_title); title.setId(R.id.destination_title);
        title.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_HeadlineMedium); content.addView(title);
        status = new MaterialTextView(requireContext()); status.setId(R.id.review_status); content.addView(status);
        retry = new MaterialButton(requireContext()); retry.setText(R.string.review_reload); retry.setId(R.id.review_reload);
        retry.setOnClickListener(view -> { if (authReady) model.refresh(); else ((MainActivity) requireActivity()).retryAuth(); }); content.addView(retry);
        rows = new LinearLayout(requireContext()); rows.setOrientation(LinearLayout.VERTICAL); rows.setId(R.id.review_rows); content.addView(rows);
        return scroll;
    }
    @Override public void onViewCreated(@NonNull View view, Bundle saved) {
        AppSessionDependencies dependencies = AppSessionDependencies.get(requireContext()); Handler handler = new Handler(Looper.getMainLooper());
        model = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull public <T extends ViewModel> T create(@NonNull Class<T> type) {
                return type.cast(new ReviewViewModel(dependencies.sessions, LearningDependencies.review(requireContext()),
                        command -> { if (Looper.myLooper() == Looper.getMainLooper()) command.run(); else handler.post(command); }));
            }
        }).get(ReviewViewModel.class);
        model.state().observe(getViewLifecycleOwner(), value -> { if (authReady) render(value); });
        ((MainActivity) requireActivity()).authState().observe(getViewLifecycleOwner(), value -> {
            authReady = value == com.example.englishaicoach.feature.auth.AuthSessionViewModel.State.AUTHENTICATED;
            model.authenticated(authReady);
            if (authReady) render(model.state().getValue()); else { rows.removeAllViews(); status.setText(R.string.learning_auth_error); }
        });
        new ConnectivityMonitor(requireContext()).observe(getViewLifecycleOwner(), value -> model.online(Boolean.TRUE.equals(value)));
    }
    private void render(ReviewViewModel.State state) {
        rows.removeAllViews(); retry.setVisibility(View.VISIBLE);
        switch (state.phase) {
            case READY:
                status.setText(getString(R.string.review_shown_count, state.items.size()));
                for (ReviewItem item : state.items) {
                    MaterialTextView detail = new MaterialTextView(requireContext());
                    detail.setText(getString(R.string.review_item, item.word, item.nextReviewAt)); rows.addView(detail);
                    MaterialButton practice = new MaterialButton(requireContext()); practice.setText(R.string.review_practice);
                    practice.setContentDescription(getString(R.string.review_practice_word, item.word));
                    practice.setOnClickListener(view -> { if (model.canOpen()) ((MainActivity) requireActivity()).openReviewLearning(item.vocabularyId); });
                    rows.addView(practice);
                } break;
            case EMPTY: status.setText(R.string.review_empty); break;
            case OFFLINE: status.setText(R.string.review_offline); break;
            case ERROR: status.setText(R.string.review_error); break;
            case EXPIRED: status.setText(R.string.learning_expired); retry.setVisibility(View.GONE); break;
            default: status.setText(R.string.learning_loading); retry.setVisibility(View.GONE); break;
        }
    }
    @Override public void onResume() { super.onResume(); if (model != null) model.resume(); }
    @Override public void onPause() { if (model != null) model.pause(); super.onPause(); }
    @Override public void onDestroyView() { content = null; rows = null; status = null; retry = null; super.onDestroyView(); }
}
