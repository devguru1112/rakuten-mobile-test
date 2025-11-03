package com.rakuten.mobile.server.web;

import com.rakuten.mobile.server.domain.OptionChoice;
import com.rakuten.mobile.server.domain.Question;
import com.rakuten.mobile.server.service.QuestionService;
import com.rakuten.mobile.server.web.dto.QuestionDTO;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Controller to handle operations related to questions in a survey.
 * Provides an endpoint to replace all questions for a survey in one request.
 * The current implementation does not support partial updates.
 */
@RestController
@RequestMapping("/api/surveys/{surveyId}/questions")
public class QuestionController {

    private final QuestionService questions;

    public QuestionController(QuestionService questions) { this.questions = questions; }

    /**
     * Endpoint to replace all questions for a given survey in one request.
     * This will delete existing questions and insert the new ones provided in the request body.
     *
     * @param surveyId The ID of the survey to replace questions for.
     * @param body The list of QuestionDTO objects representing the new set of questions.
     * @return The list of newly created questions (without options, for simplicity).
     */
    @PostMapping
    public List<QuestionDTO> replace(@PathVariable UUID surveyId, @RequestBody List<QuestionDTO> body) {
        // Map incoming DTOs to entities; index-based map used to attach options to their new Question
        List<Question> newQs = new ArrayList<>();
        Map<Integer, List<OptionChoice>> optionsByIndex = new HashMap<>();

        // Iterate through the provided QuestionDTOs and map them to Question entities
        for (int i = 0; i < body.size(); i++) {
            var dto = body.get(i);

            Question q = new Question();
            q.setText(dto.text());
            q.setType(dto.type());
            q.setRequired(dto.required());
            q.setPosition(dto.position()); // if 0, service assigns i+1
            newQs.add(q);

            if (dto.options() != null) {
                List<OptionChoice> opts = new ArrayList<>();
                int pos = 0;
                for (var o : dto.options()) {
                    OptionChoice oc = new OptionChoice();
                    oc.setText(o.text());
                    oc.setPosition(o.position() == 0 ? ++pos : o.position());
                    opts.add(oc);
                }
                optionsByIndex.put(i, opts);
            }
        }

        var saved = questions.replaceAll(surveyId, newQs, optionsByIndex);
        // Map back to DTOs (omit options to keep response small)
        List<QuestionDTO> out = new ArrayList<>();
        for (Question q : saved) {
            out.add(new QuestionDTO(q.getId(), q.getType(), q.getText(), q.isRequired(), q.getPosition(), List.of()));
        }
        return out;
    }
}
