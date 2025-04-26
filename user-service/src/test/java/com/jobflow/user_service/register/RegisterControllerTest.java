package com.jobflow.user_service.register;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobflow.user_service.exception.EmailServiceException;
import com.jobflow.user_service.exception.UserAlreadyExistsException;
import com.jobflow.user_service.handler.GlobalHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RegisterControllerTest {

    @Mock
    private RegisterService registerService;

    @InjectMocks
    private RegisterController registerController;

    private MockMvc mockMvc;

    private RegisterRequest registerRequest;

    private String registerRequestJson;

    private final static ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() throws JsonProcessingException {
        mockMvc = MockMvcBuilders.standaloneSetup(registerController)
                .setControllerAdvice(new GlobalHandler())
                .build();
        registerRequest = new RegisterRequest("Ivan", "Ivanov", "IvanIvanov@gmail.com", "abcde");
        registerRequestJson = objectMapper.writeValueAsString(registerRequest);
    }

    @Test
    public void register_sendCodeSuccessfully() throws Exception {
        doNothing().when(registerService).register(registerRequest);

        mockMvc.perform(post("/api/v1/register")
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .content(registerRequestJson))
                .andExpect(status().isOk());

        verify(registerService, times(1)).register(registerRequest);
    }

    @Test
    public void register_invalidData_returnBadRequest() throws Exception {
        RegisterRequest invalidRequest = new RegisterRequest("", "", "", "");
        String invalidRequestJson = objectMapper.writeValueAsString(invalidRequest);

        mockMvc.perform(post("/api/v1/register")
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .content(invalidRequestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.time").exists())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()));

        verifyNoInteractions(registerService);
    }

    @Test
    public void register_userAlreadyExists_returnConflict() throws Exception {
        var userAlreadyExistsException = new UserAlreadyExistsException("User with login: " + registerRequest.getLogin() + " already exists");
        doThrow(userAlreadyExistsException).when(registerService).register(registerRequest);

        mockMvc.perform(post("/api/v1/register")
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .content(registerRequestJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(userAlreadyExistsException.getMessage()))
                .andExpect(jsonPath("$.status").value(HttpStatus.CONFLICT.value()))
                .andExpect(jsonPath("$.time").exists());


        verify(registerService, times(1)).register(registerRequest);
    }

    @Test
    public void register_emailServiceException_returnInternalServerError() throws Exception {
        var emailServiceException = new EmailServiceException("Email service exception");
        doThrow(emailServiceException).when(registerService).register(registerRequest);

        mockMvc.perform(post("/api/v1/register")
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .content(registerRequestJson))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value(emailServiceException.getMessage()))
                .andExpect(jsonPath("$.status").value(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                .andExpect(jsonPath("$.time").exists());


        verify(registerService, times(1)).register(registerRequest);
    }

}