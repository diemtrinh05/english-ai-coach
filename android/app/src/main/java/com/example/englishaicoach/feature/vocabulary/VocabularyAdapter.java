package com.example.englishaicoach.feature.vocabulary;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.englishaicoach.R;
import com.example.englishaicoach.databinding.ItemVocabularyBinding;
import com.example.englishaicoach.domain.model.VocabularyItem;

import java.util.ArrayList;
import java.util.List;

final class VocabularyAdapter extends RecyclerView.Adapter<VocabularyAdapter.Holder> {
    interface Selection { void open(String id); }
    private final List<VocabularyItem> items = new ArrayList<>();
    private final Selection selection;

    VocabularyAdapter(Selection selection) { this.selection = selection; }

    void submit(List<VocabularyItem> values) {
        items.clear();
        items.addAll(values);
        notifyDataSetChanged();
    }

    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemVocabularyBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override public void onBindViewHolder(@NonNull Holder holder, int position) {
        VocabularyItem item = items.get(position);
        holder.binding.word.setText(item.word);
        holder.binding.meaning.setText(item.meaningVi == null ? item.meaningEn : item.meaningVi);
        holder.binding.metadata.setText(holder.binding.getRoot().getContext().getString(
                R.string.vocabulary_detail_meta, item.cefr,
                item.topics.isEmpty() ? (item.partOfSpeech == null ? "" : item.partOfSpeech)
                        : item.topics.get(0).name));
        holder.binding.getRoot().setOnClickListener(ignored -> selection.open(item.id));
        holder.binding.getRoot().setContentDescription(item.word + ", "
                + (item.meaningVi == null ? "" : item.meaningVi));
    }

    @Override public int getItemCount() { return items.size(); }

    static final class Holder extends RecyclerView.ViewHolder {
        final ItemVocabularyBinding binding;
        Holder(ItemVocabularyBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
