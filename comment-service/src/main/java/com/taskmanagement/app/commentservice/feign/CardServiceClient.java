package com.taskmanagement.app.commentservice.feign;


import com.taskmanagement.app.commentservice.dto.CardResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "card-service" , path = "/cards")
public interface CardServiceClient {
    @GetMapping("/{cardId}")
    ResponseEntity<CardResponse> getById(@PathVariable Long cardId,@RequestHeader("Authorization") String token);
}
