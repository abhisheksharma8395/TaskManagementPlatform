package com.taskmanagement.app.notificationservice.messaging;

import com.taskmanagement.app.notificationservice.config.RabbitMQConfig;
import com.taskmanagement.app.notificationservice.dto.NotificationEvent;
import com.taskmanagement.app.notificationservice.dto.SendNotificationRequest;
import com.taskmanagement.app.notificationservice.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NotificationEventListener {

    @Autowired
    private NotificationService notificationService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public void handleNotificationEvent(NotificationEvent event) {
        log.info("[RabbitMQ] Received notification event: type={}, recipientId={}",
                event.getType(), event.getRecipientId());

        try {
            // Map the incoming event to the existing SendNotificationRequest
            // so all existing DB-save + email logic stays completely unchanged
            SendNotificationRequest request = new SendNotificationRequest();
            request.setRecipientId(event.getRecipientId());
            request.setRecipientEmail(event.getRecipientEmail());
            request.setActorId(event.getActorId());
            request.setType(event.getType());
            request.setTitle(event.getTitle());
            request.setMessage(event.getMessage());
            request.setRelatedId(event.getRelatedId());
            request.setRelatedType(event.getRelatedType());
            request.setDeepLinkUrl(event.getDeepLinkUrl());

            notificationService.send(request);

            log.info("[RabbitMQ] Successfully processed notification for recipientId={}", event.getRecipientId());
        } catch (Exception e) {
            log.error("[RabbitMQ] Failed to process notification event: {}", e.getMessage(), e);
        }
    }
}
