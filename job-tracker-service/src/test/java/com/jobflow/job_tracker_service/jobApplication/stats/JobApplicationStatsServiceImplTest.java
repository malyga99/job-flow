package com.jobflow.job_tracker_service.jobApplication.stats;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobflow.job_tracker_service.TestUtil;
import com.jobflow.job_tracker_service.exception.JobApplicationServiceException;
import com.jobflow.job_tracker_service.jobApplication.Status;
import com.jobflow.job_tracker_service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JobApplicationStatsServiceImplTest {

    @Mock
    private JobApplicationStatsRepository statsRepository;

    @Mock
    private UserService userService;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private PopularCompanyProjection popularCompanyProjection;

    @Mock
    private PopularPositionProjection popularPositionProjection;
    @Mock
    private StatusCountProjection statusCountProjections;

    @Spy
    @InjectMocks
    private JobApplicationStatsServiceImpl statsService;

    private JobApplicationStatsDto statsDto;

    @BeforeEach
    public void setup() {
        statsDto = TestUtil.createStatsDto();
    }

    @Test
    public void getStats_statsHasInCache_returnStatsFromCache() throws JsonProcessingException {
        when(userService.getCurrentUserId()).thenReturn(1L);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(StatsCacheKeyUtils.keyForUser(1L))).thenReturn("expectedJson");
        when(objectMapper.readValue("expectedJson", JobApplicationStatsDto.class)).thenReturn(statsDto);

        JobApplicationStatsDto result = statsService.getStats();

        assertNotNull(result);
        assertEquals(statsDto, result);

        verify(objectMapper, times(1)).readValue("expectedJson", JobApplicationStatsDto.class);
        verify(valueOperations, never()).set(anyString(), anyString(), any(Duration.class));
        verifyNoInteractions(statsRepository);
    }

    @Test
    public void getStats_deserializeFailed_throwExc() throws JsonProcessingException {
        var jsonException = new JsonProcessingException("Json exception"){};
        when(userService.getCurrentUserId()).thenReturn(1L);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(StatsCacheKeyUtils.keyForUser(1L))).thenReturn("expectedJson");
        when(objectMapper.readValue("expectedJson", JobApplicationStatsDto.class)).thenThrow(jsonException);

        var jobApplicationServiceException = assertThrows(JobApplicationServiceException.class, () -> statsService.getStats());
        assertEquals("Failed to deserialize job application stats", jobApplicationServiceException.getMessage());
    }

    @Test
    public void getStats_statsHasNotInCache_returnBuildedStatsAndSaveInCache() throws JsonProcessingException {
        when(userService.getCurrentUserId()).thenReturn(1L);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(StatsCacheKeyUtils.keyForUser(1L))).thenReturn(null);
        when(objectMapper.writeValueAsString(any(JobApplicationStatsDto.class))).thenReturn("expectedJson");

        when(statsRepository.countAllByUserId(1L)).thenReturn(1L);
        when(statsRepository.countSince(eq(1L), any(LocalDate.class))).thenReturn(1L);
        when(statsRepository.countUniqueCompanies(1L)).thenReturn(1L);
        when(statsRepository.findTopCompany(1L)).thenReturn(Optional.of(popularCompanyProjection));
        when(statsRepository.findTopPosition(1L)).thenReturn(Optional.of(popularPositionProjection));
        when(statsRepository.countByStatus(1L)).thenReturn(List.of(statusCountProjections));

        when(popularCompanyProjection.getCompany()).thenReturn("Google");
        when(popularCompanyProjection.getTotal()).thenReturn(1L);
        when(popularPositionProjection.getPosition()).thenReturn("Backend");
        when(popularPositionProjection.getTotal()).thenReturn(1L);
        when(statusCountProjections.getStatus()).thenReturn(Status.REJECTED);
        when(statusCountProjections.getTotal()).thenReturn(1L);

        JobApplicationStatsDto result = statsService.getStats();

        assertNotNull(result);
        assertEquals(1L, result.getTotal());
        assertEquals(1L, result.getLast7Days());
        assertEquals(1L, result.getLast30Days());
        assertEquals(1L, result.getUniqueCompanies());
        assertEquals("Google", result.getTopCompany().getName());
        assertEquals(1L, result.getTopCompany().getTotal());
        assertEquals("Backend", result.getTopPosition().getName());
        assertEquals(1L, result.getTopPosition().getTotal());
        assertEquals(1L, result.getByStatus().get(Status.REJECTED));

        verify(objectMapper, times(1)).writeValueAsString(any(JobApplicationStatsDto.class));
        verify(valueOperations, times(1)).set(
                StatsCacheKeyUtils.keyForUser(1L),
                "expectedJson",
                Duration.ofHours(1L)
        );
    }

    @Test
    public void getStats_serializeFailed_throwExc() throws JsonProcessingException {
        var jsonException = new JsonProcessingException("Json exception"){};
        when(userService.getCurrentUserId()).thenReturn(1L);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(StatsCacheKeyUtils.keyForUser(1L))).thenReturn(null);
        when(objectMapper.writeValueAsString(any(JobApplicationStatsDto.class))).thenThrow(jsonException);

        var jobApplicationServiceException = assertThrows(JobApplicationServiceException.class, () -> statsService.getStats());
        assertEquals("Failed to serialize job application stats", jobApplicationServiceException.getMessage());
    }

    @Test
    public void getStats_withoutTopCompanyOrTopPosition_setNull() throws JsonProcessingException {
        when(userService.getCurrentUserId()).thenReturn(1L);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(StatsCacheKeyUtils.keyForUser(1L))).thenReturn(null);
        when(objectMapper.writeValueAsString(any(JobApplicationStatsDto.class))).thenReturn("expectedJson");

        when(statsRepository.findTopCompany(1L)).thenReturn(Optional.empty());
        when(statsRepository.findTopPosition(1L)).thenReturn(Optional.empty());
        when(statsRepository.countByStatus(1L)).thenReturn(List.of(statusCountProjections));

        when(statusCountProjections.getStatus()).thenReturn(Status.REJECTED);
        when(statusCountProjections.getTotal()).thenReturn(1L);

        JobApplicationStatsDto result = statsService.getStats();

        assertNotNull(result);
        assertNull(result.getTopCompany());
        assertNull(result.getTopPosition());
        assertEquals(1L, result.getByStatus().get(Status.REJECTED));

        verify(objectMapper, times(1)).writeValueAsString(any(JobApplicationStatsDto.class));
        verify(valueOperations, times(1)).set(
                StatsCacheKeyUtils.keyForUser(1L),
                "expectedJson",
                Duration.ofHours(1L)
        );
    }
}