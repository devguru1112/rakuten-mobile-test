package com.rakuten.mobile.server.web.dto;

import com.rakuten.mobile.server.domain.Response;
import com.rakuten.mobile.server.domain.Survey;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;


public record ResponseRes(UUID id, UUID surveyId, UUID respondentId, Map<String, Object> answersJson, Instant submittedAt) {

    /**
     * Static method to map a Response entity to a ResponseRes DTO.
     *
     * @param res The Survey entity.
     * @return A SurveyRes DTO with the relevant survey data.
     */
    public static ResponseRes from(Response res) {
        return new ResponseRes(res.getId(), res.getSurveyId(), res.getRespondentId(), res.getAnswersJson(), res.getSubmittedAt());
    }
}