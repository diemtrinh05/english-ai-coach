package com.example.englishaicoach.onboarding;

import com.example.englishaicoach.onboarding.dto.CefrLevelResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cefr-levels")
public class CefrLevelController {

    private final CefrLevelService service;

    public CefrLevelController(CefrLevelService service) {
        this.service = service;
    }

    @GetMapping
    public List<CefrLevelResponse> listCefrLevels() {
        return service.listLevels();
    }
}
