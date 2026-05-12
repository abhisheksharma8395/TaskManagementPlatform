package com.taskmanagement.app.listservice.feign;

import com.taskmanagement.app.listservice.dto.BoardResponse;
import com.taskmanagement.app.listservice.exception.BadRequestException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "board-service", path = "/boards", fallback = BoardServiceClient.Fallback.class)
public interface BoardServiceClient {

    @GetMapping("/{boardId}")
    ResponseEntity<BoardResponse> getBoardById(@PathVariable Long boardId, @RequestHeader("Authorization") String token);

    @Component
    class Fallback implements BoardServiceClient {
        @Override
        public ResponseEntity<BoardResponse> getBoardById(Long boardId, String token) {
            throw new BadRequestException("Board service is currently unavailable. Please try again later.");
        }
    }
}
