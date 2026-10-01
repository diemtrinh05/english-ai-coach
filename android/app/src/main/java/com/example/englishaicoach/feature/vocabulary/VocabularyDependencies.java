package com.example.englishaicoach.feature.vocabulary;

import android.content.Context;

import com.example.englishaicoach.BuildConfig;
import com.example.englishaicoach.core.auth.EncryptedTokenStore;
import com.example.englishaicoach.core.auth.SessionManager;
import com.example.englishaicoach.core.network.ApiClient;
import com.example.englishaicoach.data.api.VocabularyApi;
import com.example.englishaicoach.data.repository.RetrofitVocabularyRepository;
import com.example.englishaicoach.domain.usecase.BrowseVocabularyUseCase;

final class VocabularyDependencies {
    private static BrowseVocabularyUseCase useCase;

    private VocabularyDependencies() { }

    static synchronized BrowseVocabularyUseCase useCase(Context context) {
        if (useCase == null) {
            SessionManager sessions = new SessionManager(new EncryptedTokenStore(
                    context.getApplicationContext()));
            ApiClient client = new ApiClient(BuildConfig.API_BASE_URL, sessions);
            useCase = new BrowseVocabularyUseCase(new RetrofitVocabularyRepository(
                    client.create(VocabularyApi.class)));
        }
        return useCase;
    }
}
