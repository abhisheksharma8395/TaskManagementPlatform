package com.taskmanagement.app.labelservice.feign;

import com.taskmanagement.app.labelservice.dto.BoardResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "board-service", path = "/boards")
public interface BoardServiceClient {
    @GetMapping("/{boardId}")
    ResponseEntity<BoardResponse> getBoardById(@PathVariable Long boardId, @RequestHeader("Authorization") String token);
}