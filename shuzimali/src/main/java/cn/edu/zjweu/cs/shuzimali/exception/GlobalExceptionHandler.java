package cn.edu.zjweu.cs.shuzimali.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;


@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception e) {
        log.error("Unhandled exception", e);
        Map<String, Object> body = new HashMap<>();
        body.put("message", e.getMessage());
        body.put("timestamp", Instant.now().toString());
        body.put("status", 500);
        return ResponseEntity.status(500)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }
}
