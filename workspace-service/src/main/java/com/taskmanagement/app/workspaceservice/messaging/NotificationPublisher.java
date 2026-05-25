package com.taskmanagement.app.workspaceservice.messaging;


import com.taskmanagement.app.workspaceservice.dto.NotificationEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Component;



@Component
@Slf4j
public class NotificationPublisher {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.name}")
    private String exchange;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    public void publish(NotificationEvent event){
        try{
            rabbitTemplate.convertAndSend(exchange,routingKey,event);
            log.info("[RabbitMQ] Published notification: type={}, recipientId={}",
                    event.getType(), event.getRecipientId());
        } catch (Exception e) {
            // Never let a notification failure break the main business flow
            log.error("[RabbitMQ] Failed to publish notification event: {}", e.getMessage());
        }
    }
}
