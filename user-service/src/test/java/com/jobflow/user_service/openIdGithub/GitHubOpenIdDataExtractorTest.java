package com.jobflow.user_service.openIdGithub;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobflow.user_service.exception.OpenIdServiceException;
import com.jobflow.user_service.openId.OpenIdUserInfo;
import com.jobflow.user_service.user.AuthProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GitHubOpenIdDataExtractorTest {

    private static final String KEY = "key";

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private GitHubOpenIdDataExtractor openIdDataExtractor;

    @Mock
    private JsonNode dataNode;

    @Mock
    private JsonNode keyNode;

    @Test
    public void extractData_returnValue() throws JsonProcessingException {
        when(objectMapper.readTree("data")).thenReturn(dataNode);
        when(dataNode.get(KEY)).thenReturn(keyNode);
        when(keyNode.asText()).thenReturn("value");

        String result = openIdDataExtractor.extractData("data", KEY);

        assertNotNull(result);
        assertEquals("value", result);
    }

    @Test
    public void extractData_valueNotFound_returnNull() throws JsonProcessingException {
        when(objectMapper.readTree("data")).thenReturn(dataNode);
        when(dataNode.get(KEY)).thenReturn(null);

        String result = openIdDataExtractor.extractData("data", KEY);

        assertNull(result);
    }
    @Test
    public void extractData_failedExtract_throwExc() throws JsonProcessingException {
        JsonProcessingException jsonProcessingException = new JsonProcessingException("Json exception"){};
        when(objectMapper.readTree("data")).thenThrow(jsonProcessingException);

        var openIdServiceException = assertThrows(OpenIdServiceException.class, () -> openIdDataExtractor.extractData("data", KEY));
        assertEquals("Failed to extract value: " + KEY + " from json: " + jsonProcessingException.getMessage(), openIdServiceException.getMessage());
    }

    @Test
    public void extractUserInfo_returnUserInfo() throws JsonProcessingException {
        when(objectMapper.readTree("data")).thenReturn(dataNode);
        when(dataNode.get(anyString())).thenReturn(keyNode);
        when(keyNode.asText()).thenReturn("123");

        OpenIdUserInfo result = openIdDataExtractor.extractUserInfo("data");

        assertNotNull(result);
        assertEquals("123", result.getFirstname());
        assertNull(result.getLastname());
        assertEquals("123", result.getAvatarUrl());
        assertEquals("123", result.getAuthProviderId());
        assertEquals(AuthProvider.GITHUB, result.getAuthProvider());
    }
}