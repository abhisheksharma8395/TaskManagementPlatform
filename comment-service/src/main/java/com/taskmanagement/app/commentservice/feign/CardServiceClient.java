package com.taskmanagement.app.commentservice.feign;

import com.taskmanagement.app.commentservice.dto.CardResponse;
import com.taskmanagement.app.commentservice.exception.BadRequestException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "card-service", path = "/cards", fallback = CardServiceClient.Fallback.class)
public interface CardServiceClient {

    @GetMapping("/{cardId}")
    ResponseEntity<CardResponse> getCardById(@PathVariable Long cardId, @RequestHeader("Authorization") String token);

    @Component
    class Fallback implements CardServiceClient {
        @Override
        public ResponseEntity<CardResponse> getCardById(Long cardId, String token) {
            throw new BadRequestException("Card service is currently unavailable. Please try again later.");
        }
    }
}
