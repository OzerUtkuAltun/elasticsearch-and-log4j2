package com.ozerutkualtun.elastic.exception;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.ElasticsearchStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ElasticsearchStatusException.class)
    public ResponseEntity<ErrorResponse> handleElasticSearchStatusException(ElasticsearchStatusException e) {

        log.info(e.getMessage());
        return new ResponseEntity<>(new ErrorResponse(false, e.getMessage(), System.currentTimeMillis()), HttpStatus.BAD_REQUEST);
    }

}
