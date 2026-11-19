package com.example.tracing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureObservability
@ExtendWith(OutputCaptureExtension.class)
class TraceControllerTest {

    private static final String TRACE_ID = "4bf92f3577b34da6a3ce929d0e0e4736";
    private static final String CLIENT_SPAN_ID = "00f067aa0ba902b7";
    private static final String TRACEPARENT = "00-" + TRACE_ID + "-" + CLIENT_SPAN_ID + "-01";

    @Autowired
    TestRestTemplate rest;

    private ResponseEntity<String> get(String path) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("traceparent", TRACEPARENT);
        return rest.exchange(path, HttpMethod.GET, new HttpEntity<>(headers), String.class);
    }

    @Test
    void helloPropagatesTraceId(CapturedOutput output) {
        ResponseEntity<String> res = get("/hello");
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).isEqualTo("hello");
        assertThat(res.getHeaders().getFirst("traceresponse")).startsWith("00-" + TRACE_ID + "-").endsWith("-01");
        assertLogged(output, "/hello -> returning 200");
    }

    @Test
    void errorReturns500WithTraceId(CapturedOutput output) {
        ResponseEntity<String> res = get("/error-endpoint");
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(res.getHeaders().getFirst("traceresponse")).startsWith("00-" + TRACE_ID + "-");
        assertLogged(output, "/error-endpoint -> returning 500");
    }

    private void assertLogged(CapturedOutput output, String message) {
        assertThat(output.getOut().lines().filter(l -> l.contains(message)))
                .singleElement(org.assertj.core.api.InstanceOfAssertFactories.STRING)
                .contains("traceId=" + TRACE_ID)
                .contains("parentSpanId=" + CLIENT_SPAN_ID);
    }
}
