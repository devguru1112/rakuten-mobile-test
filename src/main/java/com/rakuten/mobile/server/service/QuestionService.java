package com.rakuten.mobile.server.service;

import com.rakuten.mobile.server.domain.OptionChoice;
import com.rakuten.mobile.server.domain.Question;
import com.rakuten.mobile.server.domain.QuestionType;
import com.rakuten.mobile.server.repo.OptionChoiceRepository;
import com.rakuten.mobile.server.repo.QuestionRepository;
import com.rakuten.mobile.server.tenancy.TenantContext;
import com.rakuten.mobile.server.web.dto.question.CreateQuestionReq;
import com.rakuten.mobile.server.web.dto.question.ReplaceQuestionsReq;
import com.rakuten.mobile.server.web.dto.question.UpdateQuestionReq;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class QuestionService {

    private final QuestionRepository qRepo;
    private final OptionChoiceRepository oRepo;

    public QuestionService(QuestionRepository qRepo, OptionChoiceRepository oRepo) {
        this.qRepo = qRepo;
        this.oRepo = oRepo;
    }

    @Transactional(readOnly = true)
    public List<Question> list(UUID surveyId) {
        return qRepo.findBySurveyIdOrderByPositionAsc(surveyId);
    }

    @Transactional
    public Question create(UUID surveyId, CreateQuestionReq req) {
        int nextPos = Math.toIntExact(qRepo.countBySurveyId(surveyId)) + 1;

        UUID tenant = UUID.fromString(TenantContext.required()); // ensure this returns a valid UUID string

        Question q = new Question();
        q.setId(UUID.randomUUID());
        q.setTenantId(tenant);
        q.setSurveyId(surveyId);
        q.setType(req.type());
        q.setText(req.text());         // <— set text
        q.setRequired(req.required());
        q.setPosition(nextPos);
        qRepo.save(q);

        createOrReplaceOptions(q, req.options());
        return q;
    }

    @Transactional
    public Question update(UUID questionId, UpdateQuestionReq req) {
        Question q = qRepo.findById(questionId).orElseThrow(() -> new IllegalArgumentException("Question not found"));

        if (req.text() != null) q.setText(req.text());  // <— update text if provided
        if (req.type() != null) q.setType(req.type());
        if (req.required() != null) q.setRequired(req.required());

        if (q.getType() == QuestionType.SINGLE_CHOICE || q.getType() == QuestionType.MULTI_CHOICE) {
            if (req.options() != null) {
                deleteAllOptions(q.getId());
                createOrReplaceOptions(q, req.options());
            }
        } else {
            // non-choice types should not have options
            deleteAllOptions(q.getId());
        }

        return q;
    }

    @Transactional
    public void delete(UUID surveyId, UUID questionId) {
        Question q = qRepo.findById(questionId).orElseThrow(() -> new IllegalArgumentException("Question not found"));
        if (!q.getSurveyId().equals(surveyId)) {
            throw new IllegalArgumentException("Question does not belong to survey");
        }
        deleteAllOptions(questionId);
        qRepo.deleteById(questionId);
        resequencePositions(surveyId);
    }

    @Transactional
    public List<Question> replaceAll(UUID surveyId, ReplaceQuestionsReq req) {
        // remove existing
        qRepo.findBySurveyIdOrderByPositionAsc(surveyId)
                .forEach(q -> {
                    deleteAllOptions(q.getId());
                    qRepo.deleteById(q.getId());
                });

        UUID tenant = UUID.fromString(TenantContext.required());
        int pos = 1;
        for (CreateQuestionReq cq : req.questions()) {
            Question q = new Question();
            q.setId(UUID.randomUUID());
            q.setTenantId(tenant);
            q.setSurveyId(surveyId);
            q.setType(cq.type());
            q.setText(cq.text());
            q.setRequired(cq.required());
            q.setPosition(pos++);
            qRepo.save(q);
            createOrReplaceOptions(q, cq.options());
        }
        return qRepo.findBySurveyIdOrderByPositionAsc(surveyId);
    }

    @Transactional(readOnly = true)
    public List<OptionChoice> options(UUID questionId) {
        return oRepo.findByQuestionIdOrderByPositionAsc(questionId);
    }

    /* helpers */

    private void createOrReplaceOptions(Question q, List<com.rakuten.mobile.server.web.dto.question.CreateQuestionReq.OptionReq> incoming) {
        if (q.getType() != QuestionType.SINGLE_CHOICE && q.getType() != QuestionType.MULTI_CHOICE) return;
        if (incoming == null) return;

        int i = 1;
        UUID tenant = UUID.fromString(TenantContext.required());
        for (com.rakuten.mobile.server.web.dto.question.CreateQuestionReq.OptionReq o : incoming) {
            OptionChoice oc = new OptionChoice();
            oc.setTenantId(tenant);
            oc.setId(UUID.randomUUID());
            oc.setQuestionId(q.getId());
            oc.setLabel(o.label());
            oc.setValue(o.value());
            oc.setPosition(i++);
            oRepo.save(oc);
        }
    }

    private void deleteAllOptions(UUID questionId) {
        oRepo.findByQuestionIdOrderByPositionAsc(questionId)
                .forEach(o -> oRepo.deleteById(o.getId()));
    }

    private void resequencePositions(UUID surveyId) {
        List<Question> qs = qRepo.findBySurveyIdOrderByPositionAsc(surveyId);
        int i = 1;
        for (Question q : qs) {
            if (q.getPosition() != i) {
                q.setPosition(i);
            }
            i++;
        }
    }
}
