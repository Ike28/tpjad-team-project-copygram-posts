package com.ubb.tpjad.copygram_posts.controller;

import com.ubb.tpjad.copygram_posts.api.dto.ErrorResponseDto;
import com.ubb.tpjad.copygram_posts.api.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@Slf4j
@ControllerAdvice
public class CopygramPostsControllerAdvice {
    @ExceptionHandler(value = {
            InvalidPostException.class,
            InvalidCommentException.class,
            InvalidLikeActionException.class
    })
    public ResponseEntity<ErrorResponseDto> badRequest(Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponseDto.builder()
                .message(e.getMessage())
                .build());
    }

    @ExceptionHandler(LimitExceededException.class)
    public ResponseEntity<ErrorResponseDto> limitExceeded(LimitExceededException e) {
        return ResponseEntity.status(HttpStatus.CONTENT_TOO_LARGE).body(ErrorResponseDto.builder()
                .message("Limit specified is too high, check provided limit")
                .limit(e.getLimit())
                .build());
    }

    @ExceptionHandler(UnauthorizedDeletionException.class)
    public ResponseEntity<ErrorResponseDto> unauthorizedDeletion(UnauthorizedDeletionException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponseDto.builder()
                .message(e.getMessage())
                .build());
    }
}
