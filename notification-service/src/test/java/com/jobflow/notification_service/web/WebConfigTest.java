package com.jobflow.notification_service.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class WebConfigTest {

    @InjectMocks
    private WebConfig webConfig;

    //for JaCoCo coverage (100%)
    @Test
    public void restTemplateBean_createRestTemplate() {
        RestTemplate restTemplate = webConfig.restTemplate();

        assertNotNull(restTemplate);
    }

}