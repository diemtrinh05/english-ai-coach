package com.example.englishaicoach.feature.assessment;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import com.example.englishaicoach.R;
import com.example.englishaicoach.domain.model.Assessment;

final class AssessmentResultRenderer {
    private AssessmentResultRenderer() { }
    static void render(Context context, TextView level, TextView counts, Assessment.Result result) {
        String heading = context.getString(R.string.assessment_result_level, result.level.code, result.level.name);
        level.setText(result.level.description == null ? heading
                : context.getString(R.string.assessment_result_detail, heading, result.level.description));
        String score = result.score == null ? context.getString(R.string.assessment_result_score_missing)
                : context.getString(R.string.assessment_result_score, result.score.stripTrailingZeros().toPlainString());
        counts.setText(context.getString(R.string.assessment_result_counts, score, result.correct, result.answered));
        level.setVisibility(View.VISIBLE); counts.setVisibility(View.VISIBLE);
    }
}
