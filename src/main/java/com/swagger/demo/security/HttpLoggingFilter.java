package com.swagger.demo.config;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class HttpLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(HttpLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Wrap request and response to cache their content
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();

        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            long timeTaken = System.currentTimeMillis() - startTime;
            logRequest(requestWrapper);
            logResponse(responseWrapper, timeTaken);
            responseWrapper.copyBodyToResponse();
        }
    }

    private void logRequest(ContentCachingRequestWrapper request) {
        String body = getContentAsString(request.getContentAsByteArray(), request.getCharacterEncoding());
        Map<String, List<String>> headers = Collections.list(request.getHeaderNames())
                .stream()
                .collect(Collectors.toMap(h -> h, h -> Collections.list(request.getHeaders(h))));

        StringBuilder msg = new StringBuilder();
        msg.append("\n--- HTTP Request ---\n");
        msg.append("Method: ").append(request.getMethod()).append("\n");
        msg.append("URI: ").append(request.getRequestURI());
        if (request.getQueryString() != null) {
            msg.append('?').append(request.getQueryString());
        }
        msg.append("\n");
        msg.append("Headers: ").append(headers).append("\n");
        if (!body.isEmpty()) {
            msg.append("Body: ").append(body).append("\n");
        }
        msg.append("--------------------\n");

        log.info(msg.toString());
    }

    private void logResponse(ContentCachingResponseWrapper response, long timeTaken) {
        String body = getContentAsString(response.getContentAsByteArray(), response.getCharacterEncoding());
        Map<String, Collection<String>> headers = response.getHeaderNames()
                .stream()
                .collect(Collectors.toMap(h -> h, response::getHeaders));

        StringBuilder msg = new StringBuilder();
        msg.append("\n--- HTTP Response ---\n");
        msg.append("Status: ").append(response.getStatus()).append("\n");
        msg.append("Time Taken: ").append(timeTaken).append("ms\n");
        msg.append("Headers: ").append(headers).append("\n");
        if (!body.isEmpty()) {
            msg.append("Body: ").append(body).append("\n");
        }
        msg.append("---------------------\n");

        log.info(msg.toString());
    }

    private String getContentAsString(byte[] buf, String charsetName) {
        if (buf == null || buf.length == 0) {
            return "";
        }
        try {
            return new String(buf, 0, buf.length, charsetName);
        } catch (UnsupportedEncodingException ex) {
            return "Unsupported encoding";
        }
    }
}
