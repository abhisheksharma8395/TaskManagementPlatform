package com.taskmanagement.app.labelservice.feign;

import com.taskmanagement.app.labelservice.dto.BoardResponse;
import com.taskmanagement.app.labelservice.exception.BadRequestException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "board-service", path = "/boards")
public interface BoardServiceClient {

    @GetMapping("/{boardId}")
    ResponseEntity<BoardResponse> getById(@PathVariable Long boardId, @RequestHeader("Authorization") String token);

}
