package com.jobflow.user_service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

public final class TestUtil {

    private TestUtil() {
    }

    public static <T> HttpEntity<T> createRequest(T request, HttpHeaders headers) {
        return new HttpEntity<>(request, headers);
    }

    public static <T> HttpEntity<T> createRequest(T request) {
        return new HttpEntity<>(request);
    }
}
