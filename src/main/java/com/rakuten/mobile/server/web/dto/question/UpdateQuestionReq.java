package com.rakuten.mobile.server.web.dto.question;

import com.rakuten.mobile.server.domain.QuestionType;

import java.util.List;

public record UpdateQuestionReq(
        String text,                                  // <— optional partial update
        QuestionType type,
        Boolean required,
        List<CreateQuestionReq.OptionReq> options     // replaces options if provided (for choice types)
) {}
