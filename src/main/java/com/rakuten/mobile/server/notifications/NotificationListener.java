package com.rakuten.mobile.server.notifications;

import com.rakuten.mobile.server.events.SurveyPublishedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class NotificationListener {

    /**
     * Handle the event asynchronously **after** the publishing transaction commits.
     * Replace the log with a call to a queue/email/SMS client as needed.
     */
    @Async
    @TransactionalEventListener
    public void onSurveyPublished(SurveyPublishedEvent e) {
        // Simulate queue publish; in real life, inject a Kafka/SQS client and send here.
        log.info("NOTIFY: survey published. tenant={}, survey={}, title={}",
                e.tenantId(), e.surveyId(), e.title());
    }
}
