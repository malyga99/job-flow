package com.jobflow.job_tracker_service.jobApplication;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jobflow.job_tracker_service.TestUtil;
import com.jobflow.job_tracker_service.exception.JobApplicationNotFoundException;
import com.jobflow.job_tracker_service.exception.TooManyRequestsException;
import com.jobflow.job_tracker_service.exception.UserDontHavePermissionException;
import com.jobflow.job_tracker_service.handler.GlobalHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    private JobApplicationDto firstJobApplicationDto;

    private JobApplicationDto secondJobApplicationDto;

    private Page<JobApplicationDto> mockPage;

    @BeforeEach
    public void setup() throws JsonProcessingException {
        mockMvc = MockMvcBuilders.standaloneSetup(jobApplicationController)
                .setControllerAdvice(new GlobalHandler())
                .build();

        createUpdateDto = TestUtil.createJobApplicationCreateUpdateDto();
        createUpdateDtoJson = objectMapper.writeValueAsString(createUpdateDto);

        firstJobApplicationDto = TestUtil.createJobApplicationDto();
        secondJobApplicationDto = TestUtil.createJobApplicationDto();

        mockPage = new PageImpl<>(List.of(firstJobApplicationDto, secondJobApplicationDto), PageRequest.of(0, 10, Sort.by(Direction.DESC, "updatedAt")), 2);
    }

    @Test
    public void findMy_returnTwoJobApplications() throws Exception {
        var argumentCaptor = ArgumentCaptor.forClass(Pageable.class);
        when(jobApplicationService.findMy(any(Pageable.class))).thenReturn(mockPage);

        mockMvc.perform(get("/api/v1/job-applications/my")
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.content[0].id").value(firstJobApplicationDto.getId()))
                .andExpect(jsonPath("$.content[0].userId").value(firstJobApplicationDto.getUserId()))
                .andExpect(jsonPath("$.content[0].company").value(firstJobApplicationDto.getCompany()))
                .andExpect(jsonPath("$.content[1].id").value(secondJobApplicationDto.getId()))
                .andExpect(jsonPath("$.content[1].userId").value(secondJobApplicationDto.getUserId()))
                .andExpect(jsonPath("$.content[1].company").value(secondJobApplicationDto.getCompany()));

        verify(jobApplicationService, times(1)).findMy(argumentCaptor.capture());

        Pageable pageable = argumentCaptor.getValue();
        assertEquals(0, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());

        Order sortOrder = pageable.getSort().getOrderFor("updatedAt");
        assertNotNull(sortOrder);
        assertEquals(Direction.DESC, sortOrder.getDirection());
    }

    @Test
    public void findById_returnJobApplication() throws Exception {
        when(jobApplicationService.findById(1L)).thenReturn(firstJobApplicationDto);

        mockMvc.perform(get("/api/v1/job-applications/{id}", 1L)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(firstJobApplicationDto.getId()))
                .andExpect(jsonPath("$.userId").value(firstJobApplicationDto.getUserId()))
                .andExpect(jsonPath("$.company").value(firstJobApplicationDto.getCompany()))
                .andExpect(jsonPath("$.position").value(firstJobApplicationDto.getPosition()))
                .andExpect(jsonPath("$.link").value(firstJobApplicationDto.getLink()))
                .andExpect(jsonPath("$.source").value(firstJobApplicationDto.getSource().toString()))
                .andExpect(jsonPath("$.sourceDetails").value(firstJobApplicationDto.getSourceDetails()))
                .andExpect(jsonPath("$.salaryMin").value(firstJobApplicationDto.getSalaryMin()))
                .andExpect(jsonPath("$.salaryMax").value(firstJobApplicationDto.getSalaryMax()))
                .andExpect(jsonPath("$.currency").value(firstJobApplicationDto.getCurrency().toString()))
                .andExpect(jsonPath("$.status").value(firstJobApplicationDto.getStatus().toString()))
                .andExpect(jsonPath("$.comment").value(firstJobApplicationDto.getComment()))
                .andExpect(jsonPath("$.appliedAt").exists())
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());

        verify(jobApplicationService, times(1)).findById(1L);
    }

    @Test
    public void findById_jobApplicationNotFound_returnNotFound() throws Exception {
        var jobApplicationNotFound = new JobApplicationNotFoundException("Job application not found");
        when(jobApplicationService.findById(1L)).thenThrow(jobApplicationNotFound);

        mockMvc.perform(get("/api/v1/job-applications/{id}", 1L)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(jobApplicationNotFound.getMessage()))
                .andExpect(jsonPath("$.time").exists())
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()));

        verify(jobApplicationService, times(1)).findById(1L);
    }

    @Test
    public void findById_userDontHavePermission_returnForbidden() throws Exception {
        var userDontHavePermissionException = new UserDontHavePermissionException("User dont have permission");
        when(jobApplicationService.findById(1L)).thenThrow(userDontHavePermissionException);

        mockMvc.perform(get("/api/v1/job-applications/{id}", 1L)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(userDontHavePermissionException.getMessage()))
                .andExpect(jsonPath("$.time").exists())
                .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()));

        verify(jobApplicationService, times(1)).findById(1L);
    }

    @Test
    public void create_returnCreatedJobApplication() throws Exception {
        when(jobApplicationService.create(createUpdateDto)).thenReturn(firstJobApplicationDto);

        mockMvc.perform(post("/api/v1/job-applications")
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .content(createUpdateDtoJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(firstJobApplicationDto.getId()))
                .andExpect(jsonPath("$.userId").value(firstJobApplicationDto.getUserId()))
                .andExpect(jsonPath("$.company").value(firstJobApplicationDto.getCompany()))
                .andExpect(jsonPath("$.position").value(firstJobApplicationDto.getPosition()))
                .andExpect(jsonPath("$.link").value(firstJobApplicationDto.getLink()))
                .andExpect(jsonPath("$.source").value(firstJobApplicationDto.getSource().toString()))
                .andExpect(jsonPath("$.sourceDetails").value(firstJobApplicationDto.getSourceDetails()))
                .andExpect(jsonPath("$.salaryMin").value(firstJobApplicationDto.getSalaryMin()))
                .andExpect(jsonPath("$.salaryMax").value(firstJobApplicationDto.getSalaryMax()))
                .andExpect(jsonPath("$.currency").value(firstJobApplicationDto.getCurrency().toString()))
                .andExpect(jsonPath("$.status").value(firstJobApplicationDto.getStatus().toString()))
                .andExpect(jsonPath("$.comment").value(firstJobApplicationDto.getComment()))
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
    public void create_ifSourceOtherAndSourceDetailsNull_returnBadRequest() throws Exception {
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
    public void create_ifSourceOtherAndSourceDetailsBlank_returnBadRequest() throws Exception {
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
    public void create_ifSourceNotOtherAndSourceDetailsNotNull_returnBadRequest() throws Exception {
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
    public void create_ifSalaryNotNullAndCurrencyNull_returnBadRequest() throws Exception {
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
    public void create_ifSalaryNullAndCurrencyNotNull_returnBadRequest() throws Exception {
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
    public void create_ifMaxSalaryLessThanMinSalary_returnBadRequest() throws Exception {
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

    @Test
    public void create_tooManyRequests_returnTooManyRequests() throws Exception {
        var tooManyRequestsException = new TooManyRequestsException("Too many requests");
        when(jobApplicationService.create(createUpdateDto)).thenThrow(tooManyRequestsException);

        mockMvc.perform(post("/api/v1/job-applications")
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .content(createUpdateDtoJson))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.message").value(tooManyRequestsException.getMessage()))
                .andExpect(jsonPath("$.time").exists())
                .andExpect(jsonPath("$.status").value(HttpStatus.TOO_MANY_REQUESTS.value()));

        verify(jobApplicationService, times(1)).create(createUpdateDto);
    }

    @Test
    public void update_updateJobApplication() throws Exception {
        doNothing().when(jobApplicationService).update(1L, createUpdateDto);

        mockMvc.perform(put("/api/v1/job-applications/{id}", 1L)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .content(createUpdateDtoJson))
                .andExpect(status().isNoContent());

        verify(jobApplicationService, times(1)).update(1L, createUpdateDto);
    }

    @Test
    public void update_invalidData_returnBadRequest() throws Exception {
        createUpdateDto.setCompany(null);
        createUpdateDto.setStatus(null);
        createUpdateDto.setAppliedAt(null);
        createUpdateDtoJson = objectMapper.writeValueAsString(createUpdateDto);

        mockMvc.perform(put("/api/v1/job-applications/{id}", 1L)
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
    public void update_tooManyRequests_returnTooManyRequests() throws Exception {
        var tooManyRequestsException = new TooManyRequestsException("Too many requests");
        doThrow(tooManyRequestsException).when(jobApplicationService).update(1L, createUpdateDto);

        mockMvc.perform(put("/api/v1/job-applications/{id}", 1L)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .content(createUpdateDtoJson))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.time").exists())
                .andExpect(jsonPath("$.status").value(HttpStatus.TOO_MANY_REQUESTS.value()));

        verify(jobApplicationService, times(1)).update(1L, createUpdateDto);
    }

    @Test
    public void update_jobApplicationNotFound_returnNotFound() throws Exception {
        var jobApplicationNotFound = new JobApplicationNotFoundException("Job application not found");
        doThrow(jobApplicationNotFound).when(jobApplicationService).update(1L, createUpdateDto);

        mockMvc.perform(put("/api/v1/job-applications/{id}", 1L)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .content(createUpdateDtoJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(jobApplicationNotFound.getMessage()))
                .andExpect(jsonPath("$.time").exists())
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()));

        verify(jobApplicationService, times(1)).update(1L, createUpdateDto);
    }

    @Test
    public void update_userDontHavePermission_returnForbidden() throws Exception {
        var userDontHavePermissionException = new UserDontHavePermissionException("User dont have permission");
        doThrow(userDontHavePermissionException).when(jobApplicationService).update(1L, createUpdateDto);

        mockMvc.perform(put("/api/v1/job-applications/{id}", 1L)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .content(createUpdateDtoJson))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(userDontHavePermissionException.getMessage()))
                .andExpect(jsonPath("$.time").exists())
                .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()));

        verify(jobApplicationService, times(1)).update(1L, createUpdateDto);
    }

    @Test
    public void updateStatus_updateStatusJobApplication() throws Exception {
        doNothing().when(jobApplicationService).updateStatus(1L, Status.APPLIED);

        mockMvc.perform(patch("/api/v1/job-applications/{id}", 1L)
                        .param("status", Status.APPLIED.toString())
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(jobApplicationService, times(1)).updateStatus(1L, Status.APPLIED);
    }

    @Test
    public void updateStatus_tooManyRequests_returnTooManyRequests() throws Exception {
        var tooManyRequestsException = new TooManyRequestsException("Too many requests");
        doThrow(tooManyRequestsException).when(jobApplicationService).updateStatus(1L, Status.APPLIED);

        mockMvc.perform(patch("/api/v1/job-applications/{id}", 1L)
                        .param("status", Status.APPLIED.toString())
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.time").exists())
                .andExpect(jsonPath("$.status").value(HttpStatus.TOO_MANY_REQUESTS.value()));

        verify(jobApplicationService, times(1)).updateStatus(1L, Status.APPLIED);
    }

    @Test
    public void updateStatus_jobApplicationNotFound_returnNotFound() throws Exception {
        var jobApplicationNotFound = new JobApplicationNotFoundException("Job application not found");
        doThrow(jobApplicationNotFound).when(jobApplicationService).updateStatus(1L, Status.APPLIED);

        mockMvc.perform(patch("/api/v1/job-applications/{id}", 1L)
                        .param("status", Status.APPLIED.toString())
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(jobApplicationNotFound.getMessage()))
                .andExpect(jsonPath("$.time").exists())
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()));

        verify(jobApplicationService, times(1)).updateStatus(1L, Status.APPLIED);
    }

    @Test
    public void updateStatus_userDontHavePermission_returnForbidden() throws Exception {
        var userDontHavePermissionException = new UserDontHavePermissionException("User dont have permission");
        doThrow(userDontHavePermissionException).when(jobApplicationService).updateStatus(1L, Status.APPLIED);

        mockMvc.perform(patch("/api/v1/job-applications/{id}", 1L)
                        .param("status", Status.APPLIED.toString())
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(userDontHavePermissionException.getMessage()))
                .andExpect(jsonPath("$.time").exists())
                .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()));

        verify(jobApplicationService, times(1)).updateStatus(1L, Status.APPLIED);
    }

    @Test
    public void delete_deleteJobApplication() throws Exception {
        doNothing().when(jobApplicationService).delete(1L);

        mockMvc.perform(delete("/api/v1/job-applications/{id}", 1L)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(jobApplicationService, times(1)).delete(1L);
    }

    @Test
    public void delete_tooManyRequests_returnTooManyRequests() throws Exception {
        var tooManyRequestsException = new TooManyRequestsException("Too many requests");
        doThrow(tooManyRequestsException).when(jobApplicationService).delete(1L);

        mockMvc.perform(delete("/api/v1/job-applications/{id}", 1L)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.time").exists())
                .andExpect(jsonPath("$.status").value(HttpStatus.TOO_MANY_REQUESTS.value()));

        verify(jobApplicationService, times(1)).delete(1L);
    }

    @Test
    public void delete_jobApplicationNotFound_returnNotFound() throws Exception {
        var jobApplicationNotFound = new JobApplicationNotFoundException("Job application not found");
        doThrow(jobApplicationNotFound).when(jobApplicationService).delete(1L);

        mockMvc.perform(delete("/api/v1/job-applications/{id}", 1L)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(jobApplicationNotFound.getMessage()))
                .andExpect(jsonPath("$.time").exists())
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()));

        verify(jobApplicationService, times(1)).delete(1L);
    }

    @Test
    public void delete_userDontHavePermission_returnForbidden() throws Exception {
        var userDontHavePermissionException = new UserDontHavePermissionException("User dont have permission");
        doThrow(userDontHavePermissionException).when(jobApplicationService).delete(1L);

        mockMvc.perform(delete("/api/v1/job-applications/{id}", 1L)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(userDontHavePermissionException.getMessage()))
                .andExpect(jsonPath("$.time").exists())
                .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()));

        verify(jobApplicationService, times(1)).delete(1L);
    }
}