package com.example.tracing;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Makes the client's trace context available to every log statement of every endpoint,
 * so controllers don't need {@code @RequestHeader("traceparent")}.
 * <p>
 * Spring Boot's {@code ServerHttpObservationFilter} (HIGHEST_PRECEDENCE + 1) already extracts the
 * incoming {@code traceparent} and puts {@code traceId}/{@code spanId} into the MDC. This filter runs
 * after it and adds:
 * <ul>
 *   <li>{@code parentSpanId} - the span id the client sent (its span is the parent of ours)</li>
 *   <li>{@code traceparent} - the raw incoming header, as received</li>
 * </ul>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 5)
public class TraceparentMdcFilter extends OncePerRequestFilter {

    public static final String TRACEPARENT_HEADER = "traceparent";
    public static final String MDC_PARENT_SPAN_ID = "parentSpanId";
    public static final String MDC_TRACEPARENT = "traceparent";

    private final Tracer tracer;

    public TraceparentMdcFilter(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Span span = tracer.currentSpan();
        String parentSpanId = span != null ? span.context().parentId() : null;
        String traceparent = request.getHeader(TRACEPARENT_HEADER);
        try {
            if (parentSpanId != null) {
                MDC.put(MDC_PARENT_SPAN_ID, parentSpanId);
            }
            if (traceparent != null) {
                MDC.put(MDC_TRACEPARENT, traceparent);
            }
            chain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_PARENT_SPAN_ID);
            MDC.remove(MDC_TRACEPARENT);
        }
    }
}
