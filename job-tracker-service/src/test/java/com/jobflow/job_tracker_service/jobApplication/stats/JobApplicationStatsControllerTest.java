package com.jobflow.job_tracker_service.jobApplication.stats;

import com.jobflow.job_tracker_service.TestUtil;
import com.jobflow.job_tracker_service.exception.JobApplicationServiceException;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class JobApplicationStatsControllerTest {

    @Mock
    private JobApplicationStatsService jobApplicationStatsService;

    @InjectMocks
    private JobApplicationStatsController jobApplicationStatsController;

    private MockMvc mockMvc;

    private JobApplicationStatsDto statsDto;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(jobApplicationStatsController)
                .setControllerAdvice(new GlobalHandler())
                .build();

        statsDto = TestUtil.createStatsDto();
    }

    @Test
    public void getStats_returnJobApplicationStats() throws Exception {
        when(jobApplicationStatsService.getStats()).thenReturn(statsDto);

        mockMvc.perform(get("/api/v1/job-applications/stats")
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(statsDto.getTotal()))
                .andExpect(jsonPath("$.last7Days").value(statsDto.getLast7Days()))
                .andExpect(jsonPath("$.last30Days").value(statsDto.getLast30Days()))
                .andExpect(jsonPath("$.uniqueCompanies").value(statsDto.getUniqueCompanies()))
                .andExpect(jsonPath("$.topCompany.name").value(statsDto.getTopCompany().getName()))
                .andExpect(jsonPath("$.topCompany.total").value(statsDto.getTopCompany().getTotal()))
                .andExpect(jsonPath("$.topPosition.name").value(statsDto.getTopPosition().getName()))
                .andExpect(jsonPath("$.topPosition.total").value(statsDto.getTopPosition().getTotal()))
                .andExpect(jsonPath("$.byStatus.size()").value(statsDto.getByStatus().size()));

        verify(jobApplicationStatsService, times(1)).getStats();
    }

    @Test
    public void getStats_jobApplicationServiceException_returnInternalServerError() throws Exception {
        var jobApplicationServiceException = new JobApplicationServiceException("Job application service exception");
        when(jobApplicationStatsService.getStats()).thenThrow(jobApplicationServiceException);

        mockMvc.perform(get("/api/v1/job-applications/stats")
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value(jobApplicationServiceException.getMessage()))
                .andExpect(jsonPath("$.status").value(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                .andExpect(jsonPath("$.time").exists());

        verify(jobApplicationStatsService, times(1)).getStats();
    }

}