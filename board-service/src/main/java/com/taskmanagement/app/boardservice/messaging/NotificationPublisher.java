package com.taskmanagement.app.boardservice.messaging;

import com.taskmanagement.app.boardservice.dto.NotificationEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NotificationPublisher {

    private static final String EXCHANGE    = "notification.exchange";
    private static final String ROUTING_KEY = "notification.routingkey";

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void publish(NotificationEvent event) {
        try {
            rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, event);
            log.info("[RabbitMQ] Published notification: type={}, recipientId={}",
                    event.getType(), event.getRecipientId());
        } catch (Exception e) {
            // Never let a notification failure break the main business flow
            log.error("[RabbitMQ] Failed to publish notification event: {}", e.getMessage());
        }
    }
}