package com.jobflow.job_tracker_service.validation;

import com.jobflow.job_tracker_service.TestUtil;
import com.jobflow.job_tracker_service.jobApplication.JobApplicationCreateUpdateDto;
import com.jobflow.job_tracker_service.jobApplication.Source;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder;
import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SourceDetailsValidatorTest {

    @Mock
    private ConstraintValidatorContext validatorContext;

    @Mock
    private ConstraintViolationBuilder violationBuilder;

    @Mock
    private NodeBuilderCustomizableContext customizableContext;

    @InjectMocks
    private SourceDetailsValidator validator;

    private JobApplicationCreateUpdateDto createUpdateDto;

    @BeforeEach
    public void setup() {
        createUpdateDto = TestUtil.createJobApplicationCreateUpdateDto();
    }

    @Test
    public void isValid_ifSourceOtherAndSourceDetailsFilled_returnTrue() {
        createUpdateDto.setSource(Source.OTHER);
        createUpdateDto.setSourceDetails("some-details");

        boolean result = validator.isValid(createUpdateDto, validatorContext);

        assertTrue(result);

        verifyNoInteractions(validatorContext);
    }

    @Test
    public void isValid_ifSourceNotOtherAndSourceDetailsNull_returnTrue() {
        createUpdateDto.setSource(Source.LINKEDIN);
        createUpdateDto.setSourceDetails(null);

        boolean result = validator.isValid(createUpdateDto, validatorContext);

        assertTrue(result);

        verifyNoInteractions(validatorContext);
    }

    @Test
    public void isValid_ifSourceOtherAndSourceDetailsNull_returnFalse() {
        when(validatorContext.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);
        when(violationBuilder.addPropertyNode(anyString())).thenReturn(customizableContext);
        createUpdateDto.setSource(Source.OTHER);
        createUpdateDto.setSourceDetails(null);

        boolean result = validator.isValid(createUpdateDto, validatorContext);

        assertFalse(result);

        verify(validatorContext, times(1)).disableDefaultConstraintViolation();
        verify(validatorContext, times(1)).buildConstraintViolationWithTemplate("Source details are required when source is OTHER");
        verify(violationBuilder, times(1)).addPropertyNode("sourceDetails");
    }

    @Test
    public void isValid_ifSourceOtherAndSourceDetailsBlank_returnFalse() {
        when(validatorContext.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);
        when(violationBuilder.addPropertyNode(anyString())).thenReturn(customizableContext);
        createUpdateDto.setSource(Source.OTHER);
        createUpdateDto.setSourceDetails("");

        boolean result = validator.isValid(createUpdateDto, validatorContext);

        assertFalse(result);

        verify(validatorContext, times(1)).disableDefaultConstraintViolation();
        verify(validatorContext, times(1)).buildConstraintViolationWithTemplate("Source details are required when source is OTHER");
        verify(violationBuilder, times(1)).addPropertyNode("sourceDetails");
    }

    @Test
    public void isValid_ifSourceNotOtherAndSourceDetailsNotNull_returnFalse() {
        when(validatorContext.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);
        when(violationBuilder.addPropertyNode(anyString())).thenReturn(customizableContext);
        createUpdateDto.setSource(Source.LINKEDIN);
        createUpdateDto.setSourceDetails("some-details");

        boolean result = validator.isValid(createUpdateDto, validatorContext);

        assertFalse(result);

        verify(validatorContext, times(1)).disableDefaultConstraintViolation();
        verify(validatorContext, times(1)).buildConstraintViolationWithTemplate("Source details must not be filled when source is not OTHER");
        verify(violationBuilder, times(1)).addPropertyNode("sourceDetails");
    }
}