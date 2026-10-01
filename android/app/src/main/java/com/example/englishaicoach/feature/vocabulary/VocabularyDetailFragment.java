package com.example.englishaicoach.feature.vocabulary;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.englishaicoach.R;
import com.example.englishaicoach.core.network.ConnectivityMonitor;
import com.example.englishaicoach.core.ui.UiState;
import com.example.englishaicoach.databinding.FragmentVocabularyDetailBinding;
import com.example.englishaicoach.domain.model.VocabularyItem;

import java.util.List;

public final class VocabularyDetailFragment extends Fragment {
    private static final String ARG_ID = "vocabulary_id";
    private FragmentVocabularyDetailBinding binding;
    private VocabularyDetailViewModel viewModel;

    public static VocabularyDetailFragment forId(String id) {
        VocabularyDetailFragment fragment = new VocabularyDetailFragment();
        Bundle arguments = new Bundle();
        arguments.putString(ARG_ID, id);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater,
                                                  @Nullable ViewGroup container,
                                                  @Nullable Bundle state) {
        binding = FragmentVocabularyDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        viewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> type) {
                return type.cast(new VocabularyDetailViewModel(
                        VocabularyDependencies.useCase(requireContext())));
            }
        }).get(VocabularyDetailViewModel.class);
        binding.backButton.setOnClickListener(ignored ->
                requireActivity().getSupportFragmentManager().popBackStack());
        binding.retryButton.setOnClickListener(ignored -> viewModel.retry());
        viewModel.detail().observe(getViewLifecycleOwner(), this::renderDetail);
        viewModel.examples().observe(getViewLifecycleOwner(), this::renderExamples);
        new ConnectivityMonitor(requireContext()).observe(getViewLifecycleOwner(), available ->
                viewModel.setOnline(Boolean.TRUE.equals(available)));
        if (viewModel.detail().getValue() == null
                || viewModel.detail().getValue().getStatus() == UiState.Status.INITIAL) {
            viewModel.load(requireArguments().getString(ARG_ID));
        }
    }

    private void renderDetail(UiState<VocabularyItem> state) {
        binding.loading.setVisibility(state.getStatus() == UiState.Status.LOADING
                ? View.VISIBLE : View.GONE);
        binding.content.setVisibility(state.getStatus() == UiState.Status.SUCCESS
                ? View.VISIBLE : View.GONE);
        int message = state.getStatus() == UiState.Status.OFFLINE
                ? R.string.vocabulary_offline_empty
                : state.getStatus() == UiState.Status.ERROR
                ? R.string.vocabulary_detail_error : 0;
        binding.statusText.setVisibility(message == 0 ? View.GONE : View.VISIBLE);
        if (message != 0) binding.statusText.setText(message);
        binding.retryButton.setVisibility(message == 0 ? View.GONE : View.VISIBLE);
        if (state.getStatus() != UiState.Status.SUCCESS) return;
        VocabularyItem item = state.getData();
        binding.word.setText(item.word);
        binding.phonetic.setText(item.phoneticIpa);
        binding.meaning.setText(item.meaningVi == null ? item.meaningEn : item.meaningVi);
        binding.metadata.setText(getString(R.string.vocabulary_detail_meta, item.cefr,
                item.partOfSpeech == null ? "" : item.partOfSpeech));
    }

    private void renderExamples(UiState<List<VocabularyItem.Example>> state) {
        binding.examplesContainer.removeAllViews();
        int message = 0;
        switch (state.getStatus()) {
            case LOADING: message = R.string.vocabulary_loading; break;
            case EMPTY: message = R.string.vocabulary_examples_empty; break;
            case ERROR: message = R.string.vocabulary_examples_error; break;
            case OFFLINE: message = R.string.vocabulary_offline_empty; break;
            default: break;
        }
        binding.examplesStatus.setVisibility(message == 0 ? View.GONE : View.VISIBLE);
        if (message != 0) binding.examplesStatus.setText(message);
        if (state.getStatus() != UiState.Status.SUCCESS) return;
        for (VocabularyItem.Example example : state.getData()) {
            com.google.android.material.textview.MaterialTextView text =
                    new com.google.android.material.textview.MaterialTextView(requireContext());
            text.setText(example.text);
            text.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyLarge);
            text.setPadding(0, getResources().getDimensionPixelSize(R.dimen.spacing_medium), 0, 0);
            binding.examplesContainer.addView(text);
            if (example.translation != null) {
                com.google.android.material.textview.MaterialTextView translation =
                        new com.google.android.material.textview.MaterialTextView(requireContext());
                translation.setText(getString(R.string.vocabulary_example_translation,
                        example.translation));
                binding.examplesContainer.addView(translation);
            }
        }
    }

    @Override public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
