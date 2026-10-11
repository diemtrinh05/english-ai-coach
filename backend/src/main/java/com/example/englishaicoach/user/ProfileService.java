package com.example.englishaicoach.user;

import com.example.englishaicoach.auth.dto.AuthUserSummary;
import com.example.englishaicoach.common.exception.ApiErrorCodes;
import com.example.englishaicoach.common.exception.ApiException;
import com.example.englishaicoach.user.dto.UpdateProfileRequest;
import com.example.englishaicoach.user.dto.UserProfileResponse;
import java.time.Clock;
import java.time.ZoneId;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {
    private final ProfileRepository profiles;
    private final Clock clock;
    public ProfileService(ProfileRepository profiles, Clock clock) { this.profiles = profiles; this.clock = clock; }

    @Transactional(readOnly = true)
    public AuthUserSummary currentUser(UUID userId) {
        return profiles.findUser(userId).orElseThrow(ProfileService::unauthorized);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse currentProfile(UUID userId) {
        currentUser(userId);
        return profiles.findProfile(userId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                ApiErrorCodes.NOT_FOUND, "Chưa có hồ sơ học tập. Vui lòng hoàn tất thông tin cá nhân."));
    }

    @Transactional
    public UserProfileResponse update(UUID userId, UpdateProfileRequest request) {
        if (!profiles.lockUser(userId)) { throw unauthorized(); }
        if (!ZoneId.getAvailableZoneIds().contains(request.timezone())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ApiErrorCodes.VALIDATION_ERROR,
                    "Múi giờ không hợp lệ.");
        }
        profiles.update(userId, request, clock.instant());
        return profiles.findProfile(userId).orElseThrow(() -> new IllegalStateException("Không tìm thấy profile sau cập nhật."));
    }

    private static ApiException unauthorized() {
        return new ApiException(HttpStatus.UNAUTHORIZED, ApiErrorCodes.UNAUTHORIZED,
                "Phiên đăng nhập không hợp lệ. Vui lòng đăng nhập lại.");
    }
}
