package com.example.tracing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.micrometer.tracing.test.autoconfigure.AutoConfigureTracing;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.web.servlet.client.RestTestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTracing
@AutoConfigureRestTestClient
@ExtendWith(OutputCaptureExtension.class)
class TraceControllerTest {

    private static final String TRACE_ID = "4bf92f3577b34da6a3ce929d0e0e4736";
    private static final String CLIENT_SPAN_ID = "00f067aa0ba902b7";
    private static final String TRACEPARENT = "00-" + TRACE_ID + "-" + CLIENT_SPAN_ID + "-01";

    @Autowired
    RestTestClient client;

    @Test
    void helloPropagatesTraceId(CapturedOutput output) {
        client.get().uri("/hello").header("traceparent", TRACEPARENT)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("X-Trace-Id", TRACE_ID)
                .expectBody(String.class).isEqualTo("hello");
        assertLogged(output, "/hello -> returning 200");
    }

    @Test
    void errorReturns500WithTraceId(CapturedOutput output) {
        client.get().uri("/error-endpoint").header("traceparent", TRACEPARENT)
                .exchange()
                .expectStatus().isEqualTo(500)
                .expectHeader().valueEquals("X-Trace-Id", TRACE_ID);
        assertLogged(output, "/error-endpoint -> returning 500");
    }

    private void assertLogged(CapturedOutput output, String message) {
        assertThat(output.getOut().lines().filter(l -> l.contains(message)))
                .singleElement(STRING)
                .contains("traceId=" + TRACE_ID)
                .contains("parentSpanId=" + CLIENT_SPAN_ID);
    }
}
