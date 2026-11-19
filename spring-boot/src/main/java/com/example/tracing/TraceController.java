package com.example.tracing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Trace context (traceId, spanId, client parentSpanId) is added to every log line automatically
 * by Spring Boot's observation filter + {@link TraceparentMdcFilter} - no per-endpoint code needed.
 */
@RestController
public class TraceController {

    private static final Logger log = LoggerFactory.getLogger(TraceController.class);

    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        log.info("Some business logic logging for this hello");
        log.info("/hello -> returning 200 'hello'");
        return ResponseEntity.ok("hello");
    }

    @GetMapping("/error-endpoint")
    public ResponseEntity<String> error() {
        log.info("Some business logic logging for this error endpoint");
        log.info("/error-endpoint -> returning 500 Internal Server Error");
        return ResponseEntity.internalServerError().body("error");
    }
}
