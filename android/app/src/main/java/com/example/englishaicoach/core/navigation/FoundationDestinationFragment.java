package com.example.englishaicoach.core.navigation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.englishaicoach.databinding.FragmentFoundationDestinationBinding;

public final class FoundationDestinationFragment extends Fragment {
    private static final String ARG_DESTINATION = "destination";
    private FragmentFoundationDestinationBinding binding;

    public static FoundationDestinationFragment forDestination(MainDestination destination) {
        FoundationDestinationFragment fragment = new FoundationDestinationFragment();
        Bundle arguments = new Bundle();
        arguments.putString(ARG_DESTINATION, destination.name());
        fragment.setArguments(arguments);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentFoundationDestinationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        String name = requireArguments().getString(ARG_DESTINATION, MainDestination.HOME.name());
        MainDestination destination = MainDestination.valueOf(name);
        binding.destinationTitle.setText(destination.getTitleId());
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
