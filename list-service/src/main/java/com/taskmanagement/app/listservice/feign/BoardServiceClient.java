package com.taskmanagement.app.listservice.feign;

import com.taskmanagement.app.listservice.dto.BoardResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "board-service", path = "/boards")
public interface BoardServiceClient {
    @GetMapping("/{boardId}")
    BoardResponse getBoardById(@PathVariable Long boardId, @RequestHeader("Authorization") String token);
}