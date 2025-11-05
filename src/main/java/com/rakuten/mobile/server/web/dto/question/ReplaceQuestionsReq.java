package com.rakuten.mobile.server.web.dto.question;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/** For bulk replace: whole survey question list */
public record ReplaceQuestionsReq(
        @NotNull @Valid List<CreateQuestionReq> questions
) {}
