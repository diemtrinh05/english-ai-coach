package com.example.englishaicoach.onboarding;

import com.example.englishaicoach.common.exception.ApiErrorCodes;
import com.example.englishaicoach.common.exception.ApiException;
import com.example.englishaicoach.onboarding.dto.*;
import java.time.Clock;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GoalService {
    private final GoalRepository goals;
    private final UserGoalRepository selections;
    private final Clock clock;
    public GoalService(GoalRepository goals, UserGoalRepository selections, Clock clock) {
        this.goals=goals; this.selections=selections; this.clock=clock;
    }

    @Transactional(readOnly=true)
    public List<GoalResponse> listGoals() { return goals.findActive(); }

    @Transactional(readOnly=true)
    public UserGoalsResponse currentGoals(UUID userId) {
        if(!selections.userExists(userId)) throw unauthorized();
        return new UserGoalsResponse(selections.findByUser(userId));
    }

    @Transactional
    public UserGoalsResponse replace(UUID userId, SetUserGoalsRequest request) {
        if(!selections.lockUser(userId)) throw unauthorized();
        var ids=request.goalIds();
        if(new HashSet<>(ids).size()!=ids.size()) throw invalid("Không được chọn mục tiêu trùng nhau.");
        if(request.primaryGoalId()!=null && !ids.contains(request.primaryGoalId())) throw invalid("Mục tiêu chính phải thuộc danh sách đã chọn.");
        var selected=goals.lockSelections(ids);
        if(selected.size()!=ids.size()) throw new ApiException(HttpStatus.NOT_FOUND,ApiErrorCodes.NOT_FOUND,"Không tìm thấy mục tiêu đã chọn.");
        if(selected.stream().anyMatch(g -> !g.active())) throw invalid("Chỉ được chọn mục tiêu đang hoạt động.");
        selections.replace(userId,ids,request.primaryGoalId(),clock.instant());
        return new UserGoalsResponse(selections.findByUser(userId));
    }

    private static ApiException invalid(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST,ApiErrorCodes.VALIDATION_ERROR,message);
    }
    private static ApiException unauthorized() {
        return new ApiException(HttpStatus.UNAUTHORIZED,ApiErrorCodes.UNAUTHORIZED,"Phiên đăng nhập không hợp lệ. Vui lòng đăng nhập lại.");
    }
}
