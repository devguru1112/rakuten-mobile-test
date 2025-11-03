package com.rakuten.mobile.server.web.dto;

import java.util.List;
import java.util.UUID;

/**
 * Data Transfer Object representing a response submission.
 * Contains a list of answers and the respondent's ID.
 */
public record SubmitResponseReq(List<Ans> answers, UUID respondentId) {
    public record Ans(UUID questionId, Object value) {}
}
