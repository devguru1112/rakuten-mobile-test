package com.rakuten.mobile.server.web.dto.question;

import com.rakuten.mobile.server.domain.OptionChoice;
import com.rakuten.mobile.server.domain.Question;
import com.rakuten.mobile.server.domain.QuestionType;

import java.util.List;
import java.util.UUID;

public record QuestionRes(
        UUID id,
        UUID surveyId,
        String text,              // <— expose text
        QuestionType type,
        boolean required,
        int position,
        List<OptionRes> options
) {
    public static QuestionRes from(Question q, List<OptionChoice> opts) {
        return new QuestionRes(
                q.getId(),
                q.getSurveyId(),
                q.getText(),
                q.getType(),
                q.isRequired(),
                q.getPosition(),
                opts.stream().map(OptionRes::from).toList()
        );
    }
}
