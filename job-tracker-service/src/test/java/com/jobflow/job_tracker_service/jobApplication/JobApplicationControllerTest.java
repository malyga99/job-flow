package com.jobflow.job_tracker_service.jobApplication;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jobflow.job_tracker_service.TestUtil;
import com.jobflow.job_tracker_service.handler.GlobalHandler;
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
class JobApplicationControllerTest {

    private final static ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Mock
    private JobApplicationService jobApplicationService;

    @InjectMocks
    private JobApplicationController jobApplicationController;

    private MockMvc mockMvc;

    private JobApplicationCreateUpdateDto createUpdateDto;

    private String createUpdateDtoJson;

    private JobApplicationDto jobApplicationDto;

    @BeforeEach
    public void setup() throws JsonProcessingException {
        mockMvc = MockMvcBuilders.standaloneSetup(jobApplicationController)
                .setControllerAdvice(new GlobalHandler())
                .build();

        createUpdateDto = TestUtil.createJobApplicationCreateUpdateDto();
        createUpdateDtoJson = objectMapper.writeValueAsString(createUpdateDto);

        jobApplicationDto = TestUtil.createJobApplicationDto();
    }

    @Test
    public void create_returnCreatedJobApplication() throws Exception {
        when(jobApplicationService.create(createUpdateDto)).thenReturn(jobApplicationDto);

        mockMvc.perform(post("/api/v1/job-applications")
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .content(createUpdateDtoJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(jobApplicationDto.getId()))
                .andExpect(jsonPath("$.userId").value(jobApplicationDto.getUserId()))
                .andExpect(jsonPath("$.company").value(jobApplicationDto.getCompany()))
                .andExpect(jsonPath("$.position").value(jobApplicationDto.getPosition()))
                .andExpect(jsonPath("$.link").value(jobApplicationDto.getLink()))
                .andExpect(jsonPath("$.source").value(jobApplicationDto.getSource().toString()))
                .andExpect(jsonPath("$.sourceDetails").value(jobApplicationDto.getSourceDetails()))
                .andExpect(jsonPath("$.salaryMin").value(jobApplicationDto.getSalaryMin()))
                .andExpect(jsonPath("$.salaryMax").value(jobApplicationDto.getSalaryMax()))
                .andExpect(jsonPath("$.currency").value(jobApplicationDto.getCurrency().toString()))
                .andExpect(jsonPath("$.status").value(jobApplicationDto.getStatus().toString()))
                .andExpect(jsonPath("$.comment").value(jobApplicationDto.getComment()))
                .andExpect(jsonPath("$.appliedAt").exists())
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());

        verify(jobApplicationService, times(1)).create(createUpdateDto);
    }

    @Test
    public void create_invalidData_returnBadRequest() throws Exception {
        createUpdateDto.setCompany(null);
        createUpdateDto.setStatus(null);
        createUpdateDto.setAppliedAt(null);
        createUpdateDtoJson = objectMapper.writeValueAsString(createUpdateDto);

        mockMvc.perform(post("/api/v1/job-applications")
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .content(createUpdateDtoJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.time").exists())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()));

        verifyNoInteractions(jobApplicationService);
    }

    @Test
    public void create_ifSourceOtherAndSourceDetailsNull_returnBadRequests() throws Exception {
        createUpdateDto.setSource(Source.OTHER);
        createUpdateDto.setSourceDetails(null);
        createUpdateDtoJson = objectMapper.writeValueAsString(createUpdateDto);

        mockMvc.perform(post("/api/v1/job-applications")
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .content(createUpdateDtoJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("sourceDetails: Source details are required when source is OTHER"))
                .andExpect(jsonPath("$.time").exists())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()));

        verifyNoInteractions(jobApplicationService);
    }

    @Test
    public void create_ifSourceOtherAndSourceDetailsBlank_returnBadRequests() throws Exception {
        createUpdateDto.setSource(Source.OTHER);
        createUpdateDto.setSourceDetails("");
        createUpdateDtoJson = objectMapper.writeValueAsString(createUpdateDto);

        mockMvc.perform(post("/api/v1/job-applications")
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .content(createUpdateDtoJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("sourceDetails: Source details are required when source is OTHER"))
                .andExpect(jsonPath("$.time").exists())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()));

        verifyNoInteractions(jobApplicationService);
    }

    @Test
    public void create_ifSourceNotOtherAndSourceDetailsNotNull_returnBadRequests() throws Exception{
        createUpdateDto.setSource(Source.LINKEDIN);
        createUpdateDto.setSourceDetails("some-details");
        createUpdateDtoJson = objectMapper.writeValueAsString(createUpdateDto);

        mockMvc.perform(post("/api/v1/job-applications")
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .content(createUpdateDtoJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("sourceDetails: Source details must not be filled when source is not OTHER"))
                .andExpect(jsonPath("$.time").exists())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()));

        verifyNoInteractions(jobApplicationService);
    }

    @Test
    public void create_ifSalaryNotNullAndCurrencyNull_returnBadRequests() throws Exception{
        createUpdateDto.setSalaryMin(100);
        createUpdateDto.setSalaryMax(300);
        createUpdateDto.setCurrency(null);
        createUpdateDtoJson = objectMapper.writeValueAsString(createUpdateDto);

        mockMvc.perform(post("/api/v1/job-applications")
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .content(createUpdateDtoJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("currency: Currency is required when any salary value is filled"))
                .andExpect(jsonPath("$.time").exists())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()));

        verifyNoInteractions(jobApplicationService);
    }

    @Test
    public void create_ifSalaryNullAndCurrencyNotNull_returnBadRequests() throws Exception{
        createUpdateDto.setSalaryMin(null);
        createUpdateDto.setSalaryMax(null);
        createUpdateDto.setCurrency(Currency.RUB);
        createUpdateDtoJson = objectMapper.writeValueAsString(createUpdateDto);

        mockMvc.perform(post("/api/v1/job-applications")
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .content(createUpdateDtoJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("currency: Currency must not be filled when none of the salary values are filled"))
                .andExpect(jsonPath("$.time").exists())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()));

        verifyNoInteractions(jobApplicationService);
    }

    @Test
    public void create_ifMaxSalaryLessThanMinSalary_returnBadRequests() throws Exception{
        createUpdateDto.setSalaryMin(300);
        createUpdateDto.setSalaryMax(100);
        createUpdateDto.setCurrency(Currency.RUB);
        createUpdateDtoJson = objectMapper.writeValueAsString(createUpdateDto);

        mockMvc.perform(post("/api/v1/job-applications")
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .content(createUpdateDtoJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("salaryMax: Max salary must be greater than or equal to the min salary"))
                .andExpect(jsonPath("$.time").exists())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()));

        verifyNoInteractions(jobApplicationService);
    }
}