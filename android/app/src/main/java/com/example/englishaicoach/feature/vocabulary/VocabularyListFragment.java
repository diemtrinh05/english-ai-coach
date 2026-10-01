package com.example.englishaicoach.feature.vocabulary;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.englishaicoach.MainActivity;
import com.example.englishaicoach.R;
import com.example.englishaicoach.core.network.ConnectivityMonitor;
import com.example.englishaicoach.core.ui.UiState;
import com.example.englishaicoach.databinding.FragmentVocabularyListBinding;

public final class VocabularyListFragment extends Fragment {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private FragmentVocabularyListBinding binding;
    private VocabularyListViewModel viewModel;
    private VocabularyAdapter adapter;
    private final java.util.List<com.example.englishaicoach.domain.model.VocabularyItem.Topic> topics =
            new java.util.ArrayList<>();
    private Runnable pendingSearch;

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater,
                                                  @Nullable ViewGroup container,
                                                  @Nullable Bundle state) {
        binding = FragmentVocabularyListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        viewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> type) {
                return type.cast(new VocabularyListViewModel(
                        VocabularyDependencies.useCase(requireContext())));
            }
        }).get(VocabularyListViewModel.class);
        adapter = new VocabularyAdapter(id -> ((MainActivity) requireActivity())
                .openVocabularyDetail(id));
        binding.vocabularyItems.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.vocabularyItems.setAdapter(adapter);
        String[] levels = {getString(R.string.vocabulary_cefr_all), "A1", "A2", "B1",
                "B2", "C1", "C2"};
        binding.cefrFilter.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, levels));
        binding.topicFilter.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{getString(R.string.vocabulary_topic_all)}));
        com.example.englishaicoach.domain.repository.VocabularyRepository.Query previous =
                viewModel.currentQuery();
        binding.searchInput.setText(previous.search);
        binding.posFilter.setText(previous.partOfSpeech);
        if (previous.cefr != null) {
            for (int index = 1; index < levels.length; index++) {
                if (levels[index].equals(previous.cefr)) binding.cefrFilter.setSelection(index);
            }
        }
        binding.searchButton.setOnClickListener(ignored -> submitSearch());
        binding.searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence value, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence value, int start, int before, int count) {
                if (pendingSearch != null) handler.removeCallbacks(pendingSearch);
                pendingSearch = VocabularyListFragment.this::submitSearch;
                handler.postDelayed(pendingSearch, 350);
            }
            @Override public void afterTextChanged(Editable value) { }
        });
        binding.retryButton.setOnClickListener(ignored -> viewModel.retry());
        binding.loadMore.setOnClickListener(ignored -> {
            if (Boolean.TRUE.equals(viewModel.nextPageFailed().getValue())) viewModel.retry();
            else viewModel.loadNext();
        });
        viewModel.state().observe(getViewLifecycleOwner(), this::render);
        viewModel.loadingMore().observe(getViewLifecycleOwner(), ignored -> updateMore());
        viewModel.nextPageFailed().observe(getViewLifecycleOwner(), ignored -> updateMore());
        viewModel.topics().observe(getViewLifecycleOwner(), values -> {
            topics.clear();
            topics.addAll(values);
            java.util.List<String> labels = new java.util.ArrayList<>();
            labels.add(getString(R.string.vocabulary_topic_all));
            for (com.example.englishaicoach.domain.model.VocabularyItem.Topic topic : topics) {
                labels.add(topic.name);
            }
            binding.topicFilter.setAdapter(new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_dropdown_item, labels));
            String selectedId = viewModel.currentQuery().topicId;
            if (selectedId != null) {
                for (int index = 0; index < topics.size(); index++) {
                    if (selectedId.equals(topics.get(index).id)) {
                        binding.topicFilter.setSelection(index + 1);
                        break;
                    }
                }
            }
        });
        new ConnectivityMonitor(requireContext()).observe(getViewLifecycleOwner(), available ->
                viewModel.setOnline(Boolean.TRUE.equals(available)));
        viewModel.loadTopics();
        if (viewModel.state().getValue() == null
                || viewModel.state().getValue().getStatus() == UiState.Status.INITIAL) {
            submitSearch();
        }
    }

    private void submitSearch() {
        if (binding == null) return;
        String cefr = binding.cefrFilter.getSelectedItemPosition() == 0 ? null
                : binding.cefrFilter.getSelectedItem().toString();
        int selectedTopic = binding.topicFilter.getSelectedItemPosition();
        String topicId = selectedTopic > 0 && selectedTopic <= topics.size()
                ? topics.get(selectedTopic - 1).id : null;
        viewModel.search(binding.searchInput.getText().toString(), cefr, topicId,
                binding.posFilter.getText().toString());
    }

    private void render(UiState<java.util.List<com.example.englishaicoach.domain.model.VocabularyItem>> state) {
        binding.loading.setVisibility(state.getStatus() == UiState.Status.LOADING
                ? View.VISIBLE : View.GONE);
        int message = 0;
        switch (state.getStatus()) {
            case EMPTY: message = R.string.vocabulary_empty; break;
            case ERROR: message = R.string.vocabulary_error; break;
            case OFFLINE: message = R.string.vocabulary_offline_empty; break;
            default: break;
        }
        binding.statusText.setVisibility(message == 0 ? View.GONE : View.VISIBLE);
        if (message != 0) binding.statusText.setText(message);
        binding.retryButton.setVisibility(state.getStatus() == UiState.Status.ERROR
                || state.getStatus() == UiState.Status.OFFLINE ? View.VISIBLE : View.GONE);
        if (state.getStatus() == UiState.Status.SUCCESS) adapter.submit(state.getData());
        else if (state.getStatus() == UiState.Status.EMPTY) adapter.submit(java.util.Collections.emptyList());
        updateMore();
    }

    private void updateMore() {
        if (binding == null) return;
        boolean failed = Boolean.TRUE.equals(viewModel.nextPageFailed().getValue());
        binding.loadMore.setVisibility(viewModel.hasNext() || failed ? View.VISIBLE : View.GONE);
        binding.loadMore.setEnabled(!Boolean.TRUE.equals(viewModel.loadingMore().getValue()));
        binding.loadMore.setText(failed ? R.string.vocabulary_retry : R.string.vocabulary_load_more);
    }

    @Override public void onDestroyView() {
        if (pendingSearch != null) handler.removeCallbacks(pendingSearch);
        binding = null;
        super.onDestroyView();
    }
}
