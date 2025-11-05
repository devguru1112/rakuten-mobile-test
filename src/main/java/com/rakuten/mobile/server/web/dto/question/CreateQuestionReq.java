package com.rakuten.mobile.server.web.dto.question;

import com.rakuten.mobile.server.domain.QuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateQuestionReq(
        @NotBlank String text,            // <— uses text, not title
        @NotNull QuestionType type,
        boolean required,
        List<OptionReq> options           // for choice types; ignored otherwise
) {
    public record OptionReq(@NotBlank String label, String value) {}
}
