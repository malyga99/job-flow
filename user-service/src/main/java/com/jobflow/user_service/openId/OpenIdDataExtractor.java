package com.jobflow.user_service.openId;

public interface OpenIdDataExtractor<T> {

    String extractData(T data, String key);

    OpenIdUserInfo extractUserInfo(T data);
}
