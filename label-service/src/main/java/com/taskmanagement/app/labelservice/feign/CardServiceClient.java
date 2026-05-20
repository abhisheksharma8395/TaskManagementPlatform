package com.taskmanagement.app.labelservice.feign;

import com.taskmanagement.app.labelservice.dto.CardResponse;
import com.taskmanagement.app.labelservice.exception.BadRequestException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "card-service", path = "/cards")
public interface CardServiceClient {

    @GetMapping("/{cardId}")
    ResponseEntity<CardResponse> getCardById(@PathVariable Long cardId, @RequestHeader("Authorization") String token);

}
