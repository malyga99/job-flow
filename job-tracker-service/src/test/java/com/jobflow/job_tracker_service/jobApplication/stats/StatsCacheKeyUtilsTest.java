package com.jobflow.job_tracker_service.jobApplication.stats;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

class StatsCacheKeyUtilsTest {

    @Test
    public void keyForUser_returnCorrectlyKey() {
        String result = StatsCacheKeyUtils.keyForUser(1L);

        assertNotNull(result);
        assertEquals("jobAppStats:user:" + 1L, result);
    }
}