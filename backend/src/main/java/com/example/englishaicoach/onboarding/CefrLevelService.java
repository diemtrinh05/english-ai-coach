package com.example.englishaicoach.onboarding;

import com.example.englishaicoach.onboarding.dto.CefrLevelResponse;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CefrLevelService {

    private final CefrLevelRepository repository;

    public CefrLevelService(CefrLevelRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<CefrLevelResponse> listLevels() {
        return repository.findAllInCanonicalOrder().stream()
                .map(level -> new CefrLevelResponse(
                        level.id(), level.code(), level.name(), level.sortOrder()))
                .toList();
    }
}
