package com.jobflow.user_service.register;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobflow.user_service.TestUtil;
import com.jobflow.user_service.exception.*;
import com.jobflow.user_service.handler.GlobalHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RegisterControllerTest {

    private final static ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private RegisterService registerService;

    @InjectMocks
    private RegisterController registerController;

    private MockMvc mockMvc;

    private RegisterRequest registerRequest;

    private ConfirmCodeRequest confirmCodeRequest;

    private ResendCodeRequest resendCodeRequest;

    private RegisterResponse registerResponse;

    private String registerRequestJson;

    private String confirmCodeRequestJson;

    private String resendCodeRequestJson;

    private MockMultipartFile userPart;

    @BeforeEach
    public void setup() throws JsonProcessingException {
        mockMvc = MockMvcBuilders.standaloneSetup(registerController)
                .setControllerAdvice(new GlobalHandler())
                .build();
        registerRequest = TestUtil.createRegisterRequest();
        registerRequestJson = objectMapper.writeValueAsString(registerRequest);
        registerResponse = new RegisterResponse(TestUtil.ACCESS_TOKEN, TestUtil.REFRESH_TOKEN);

        userPart = new MockMultipartFile("user", "", MediaType.APPLICATION_JSON_VALUE, registerRequestJson.getBytes());

        confirmCodeRequest = TestUtil.createConfirmCodeRequest();
        confirmCodeRequestJson = objectMapper.writeValueAsString(confirmCodeRequest);

        resendCodeRequest = TestUtil.createResendCodeRequest();
        resendCodeRequestJson = objectMapper.writeValueAsString(resendCodeRequest);
    }

    @Test
    public void register_sendCodeSuccessfully() throws Exception {
        doNothing().when(registerService).register(registerRequest, null);

        mockMvc.perform(multipart("/api/v1/register")
                        .file(userPart)
                        .contentType(MULTIPART_FORM_DATA)
                        .accept(APPLICATION_JSON)
                        .content(registerRequestJson))
                .andExpect(status().isOk());

        verify(registerService, times(1)).register(registerRequest, null);
    }

    @Test
    public void register_withAvatar_sendCodeSuccessfully() throws Exception {
        doNothing().when(registerService).register(eq(registerRequest), any(MultipartFile.class));
        MockMultipartFile avatarPart = new MockMultipartFile("avatar", "", MediaType.IMAGE_PNG_VALUE, "dummy_file".getBytes());

        mockMvc.perform(multipart("/api/v1/register")
                        .file(userPart)
                        .file(avatarPart)
                        .contentType(MULTIPART_FORM_DATA)
                        .accept(APPLICATION_JSON)
                        .content(registerRequestJson))
                .andExpect(status().isOk());

        verify(registerService, times(1)).register(eq(registerRequest), any(MultipartFile.class));
    }

    @Test
    public void register_invalidData_returnBadRequest() throws Exception {
        RegisterRequest invalidRequest = new RegisterRequest("", "", "", "", null);
        String invalidRequestJson = objectMapper.writeValueAsString(invalidRequest);
        MockMultipartFile invalidUserPart = new MockMultipartFile("user", "", MediaType.APPLICATION_JSON_VALUE, invalidRequestJson.getBytes());

        mockMvc.perform(multipart("/api/v1/register")
                        .file(invalidUserPart)
                        .contentType(MULTIPART_FORM_DATA)
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
        doThrow(userAlreadyExistsException).when(registerService).register(registerRequest, null);

        mockMvc.perform(multipart("/api/v1/register")
                        .file(userPart)
                        .contentType(MULTIPART_FORM_DATA)
                        .accept(APPLICATION_JSON)
                        .content(registerRequestJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(userAlreadyExistsException.getMessage()))
                .andExpect(jsonPath("$.status").value(HttpStatus.CONFLICT.value()))
                .andExpect(jsonPath("$.time").exists());


        verify(registerService, times(1)).register(registerRequest, null);
    }

    @Test
    public void register_emailServiceException_returnInternalServerError() throws Exception {
        var emailServiceException = new EmailServiceException("Email service exception");
        doThrow(emailServiceException).when(registerService).register(registerRequest, null);

        mockMvc.perform(multipart("/api/v1/register")
                        .file(userPart)
                        .contentType(MULTIPART_FORM_DATA)
                        .accept(APPLICATION_JSON)
                        .content(registerRequestJson))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value(emailServiceException.getMessage()))
                .andExpect(jsonPath("$.status").value(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                .andExpect(jsonPath("$.time").exists());


        verify(registerService, times(1)).register(registerRequest, null);
    }

    @Test
    public void register_fileServiceException_returnInternalServerError() throws Exception {
        var fileServiceException = new FileServiceException("File service exception");
        doThrow(fileServiceException).when(registerService).register(registerRequest, null);

        mockMvc.perform(multipart("/api/v1/register")
                        .file(userPart)
                        .contentType(MULTIPART_FORM_DATA)
                        .accept(APPLICATION_JSON)
                        .content(registerRequestJson))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value(fileServiceException.getMessage()))
                .andExpect(jsonPath("$.status").value(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                .andExpect(jsonPath("$.time").exists());


        verify(registerService, times(1)).register(registerRequest, null);
    }

    @Test
    public void resendCode_resendCodeSuccessfully() throws Exception {
        doNothing().when(registerService).resendCode(resendCodeRequest);

        mockMvc.perform(post("/api/v1/register/resend")
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .content(resendCodeRequestJson))
                .andExpect(status().isOk());

        verify(registerService, times(1)).resendCode(resendCodeRequest);
    }

    @Test
    public void resendCode_invalidData_returnBadRequest() throws Exception {
        ResendCodeRequest invalidRequest = new ResendCodeRequest("");
        String invalidRequestJson = objectMapper.writeValueAsString(invalidRequest);

        mockMvc.perform(post("/api/v1/register/resend")
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
    public void resendCode_expiredCode_returnGone() throws Exception {
        var verificationCodeExpiredException = new VerificationCodeExpiredException("Expired code");
        doThrow(verificationCodeExpiredException).when(registerService).resendCode(resendCodeRequest);

        mockMvc.perform(post("/api/v1/register/resend")
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .content(resendCodeRequestJson))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.message").value(verificationCodeExpiredException.getMessage()))
                .andExpect(jsonPath("$.status").value(HttpStatus.GONE.value()))
                .andExpect(jsonPath("$.time").exists());

        verify(registerService, times(1)).resendCode(resendCodeRequest);
    }

    @Test
    public void confirmCode_returnRegisterResponse() throws Exception {
        when(registerService.confirmCode(confirmCodeRequest)).thenReturn(registerResponse);

        mockMvc.perform(post("/api/v1/register/confirm")
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .content(confirmCodeRequestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(registerResponse.getAccessToken()))
                .andExpect(jsonPath("$.refreshToken").value(registerResponse.getRefreshToken()));

        verify(registerService, times(1)).confirmCode(confirmCodeRequest);
    }

    @Test
    public void confirmCode_invalidData_returnBadRequest() throws Exception {
        ConfirmCodeRequest invalidRequest = new ConfirmCodeRequest("", 0);
        String invalidRequestJson = objectMapper.writeValueAsString(invalidRequest);

        mockMvc.perform(post("/api/v1/register/confirm")
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
    public void confirmCode_invalidCode_returnBadRequest() throws Exception {
        var invalidVerificationCodeException = new InvalidVerificationCodeException("Invalid code");
        when(registerService.confirmCode(confirmCodeRequest)).thenThrow(invalidVerificationCodeException);

        mockMvc.perform(post("/api/v1/register/confirm")
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .content(confirmCodeRequestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(invalidVerificationCodeException.getMessage()))
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.time").exists());

        verify(registerService, times(1)).confirmCode(confirmCodeRequest);
    }

    @Test
    public void confirmCode_expiredCode_returnGone() throws Exception {
        var verificationCodeExpiredException = new VerificationCodeExpiredException("Expired code");
        when(registerService.confirmCode(confirmCodeRequest)).thenThrow(verificationCodeExpiredException);

        mockMvc.perform(post("/api/v1/register/confirm")
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .content(confirmCodeRequestJson))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.message").value(verificationCodeExpiredException.getMessage()))
                .andExpect(jsonPath("$.status").value(HttpStatus.GONE.value()))
                .andExpect(jsonPath("$.time").exists());

        verify(registerService, times(1)).confirmCode(confirmCodeRequest);
    }
}