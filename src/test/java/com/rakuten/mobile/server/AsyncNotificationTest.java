package com.rakuten.mobile.server;

import com.rakuten.mobile.server.domain.Survey;
import com.rakuten.mobile.server.notifications.NotificationListener;
import com.rakuten.mobile.server.repo.SurveyRepository;
import com.rakuten.mobile.server.service.SurveyService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that publishing a survey triggers the async notification.
 */
@SpringBootTest
class AsyncNotificationTest {

    @Autowired SurveyRepository repo;
    @Autowired SurveyService service;

    @Mock
    NotificationListener listener;

    @Test
    @Transactional
    void publishEmitsAsyncNotification() {
        // seed a survey (status DRAFT by default)
        var s = new Survey();
        s.setId(UUID.randomUUID());
        s.setTenantId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        s.setTitle("Async test");
        repo.saveAndFlush(s);

        // publish (should fire event after commit)
        service.publish(s.getId());

        // Because the listener is @Async and AFTER_COMMIT, wait briefly for invocation
        await().atMost(3, SECONDS).untilAsserted(() ->
                Mockito.verify(listener, Mockito.atLeastOnce())
                        .onSurveyPublished(ArgumentMatchers.any()));
    }
}