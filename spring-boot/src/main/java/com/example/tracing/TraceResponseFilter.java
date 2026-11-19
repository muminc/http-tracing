package com.example.tracing;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Adds the W3C Trace Context Level 2 {@code traceresponse} header to every response.
 * Format: {@code 00-<trace-id>-<server-span-id>-<flags>}.
 * <p>
 * Runs after Spring's {@code ServerHttpObservationFilter} (HIGHEST_PRECEDENCE + 1), which
 * extracts the incoming {@code traceparent} and opens the server span.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class TraceResponseFilter extends OncePerRequestFilter {

    public static final String TRACE_RESPONSE_HEADER = "traceresponse";

    private final Tracer tracer;

    public TraceResponseFilter(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Span span = tracer.currentSpan();
        if (span != null) {
            TraceContext ctx = span.context();
            String flags = Boolean.TRUE.equals(ctx.sampled()) ? "01" : "00";
            response.setHeader(TRACE_RESPONSE_HEADER,
                    "00-" + ctx.traceId() + "-" + ctx.spanId() + "-" + flags);
        }
        chain.doFilter(request, response);
    }
}
