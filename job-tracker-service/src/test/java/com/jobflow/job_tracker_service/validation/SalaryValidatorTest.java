package com.jobflow.job_tracker_service.validation;

import com.jobflow.job_tracker_service.TestUtil;
import com.jobflow.job_tracker_service.jobApplication.Currency;
import com.jobflow.job_tracker_service.jobApplication.JobApplicationCreateUpdateDto;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalaryValidatorTest {

    @Mock
    private ConstraintValidatorContext validatorContext;

    @Mock
    private ConstraintViolationBuilder violationBuilder;

    @Mock
    private NodeBuilderCustomizableContext customizableContext;

    @InjectMocks
    private SalaryValidator validator;

    private JobApplicationCreateUpdateDto createUpdateDto;

    @BeforeEach
    public void setup() {
        createUpdateDto = TestUtil.createJobApplicationCreateUpdateDto();
    }

    @Test
    public void isValid_ifSalaryNotNullAndCurrencyNotNull_returnTrue() {
        createUpdateDto.setSalaryMin(100);
        createUpdateDto.setSalaryMax(300);
        createUpdateDto.setCurrency(Currency.RUB);

        boolean result = validator.isValid(createUpdateDto, validatorContext);

        assertTrue(result);

        verifyNoInteractions(validatorContext);
    }

    @Test
    public void isValid_ifSalaryNullAndCurrencyNull_returnTrue() {
        createUpdateDto.setSalaryMin(null);
        createUpdateDto.setSalaryMax(null);
        createUpdateDto.setCurrency(null);

        boolean result = validator.isValid(createUpdateDto, validatorContext);

        assertTrue(result);

        verifyNoInteractions(validatorContext);
    }

    @Test
    public void isValid_ifMaxSalaryGreaterThanMinSalary_returnTrue() {
        createUpdateDto.setSalaryMin(100);
        createUpdateDto.setSalaryMax(300);
        createUpdateDto.setCurrency(Currency.RUB);

        boolean result = validator.isValid(createUpdateDto, validatorContext);

        assertTrue(result);

        verifyNoInteractions(validatorContext);
    }

    @Test
    public void isValid_ifMinSalaryNotNullAndCurrencyNull_returnFalse() {
        when(validatorContext.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);
        when(violationBuilder.addPropertyNode(anyString())).thenReturn(customizableContext);
        createUpdateDto.setSalaryMin(100);
        createUpdateDto.setSalaryMax(null);
        createUpdateDto.setCurrency(null);

        boolean result = validator.isValid(createUpdateDto, validatorContext);

        assertFalse(result);

        verify(validatorContext, times(1)).disableDefaultConstraintViolation();
        verify(validatorContext, times(1)).buildConstraintViolationWithTemplate("Currency is required when any salary value is filled");
        verify(violationBuilder, times(1)).addPropertyNode("currency");
    }

    @Test
    public void isValid_ifMaxSalaryNotNullAndCurrencyNull_returnFalse() {
        when(validatorContext.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);
        when(violationBuilder.addPropertyNode(anyString())).thenReturn(customizableContext);
        createUpdateDto.setSalaryMin(null);
        createUpdateDto.setSalaryMax(300);
        createUpdateDto.setCurrency(null);

        boolean result = validator.isValid(createUpdateDto, validatorContext);

        assertFalse(result);

        verify(validatorContext, times(1)).disableDefaultConstraintViolation();
        verify(validatorContext, times(1)).buildConstraintViolationWithTemplate("Currency is required when any salary value is filled");
        verify(violationBuilder, times(1)).addPropertyNode("currency");
    }

    @Test
    public void isValid_ifBothSalaryNotNullAndCurrencyNull_returnFalse() {
        when(validatorContext.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);
        when(violationBuilder.addPropertyNode(anyString())).thenReturn(customizableContext);
        createUpdateDto.setSalaryMin(100);
        createUpdateDto.setSalaryMax(300);
        createUpdateDto.setCurrency(null);

        boolean result = validator.isValid(createUpdateDto, validatorContext);

        assertFalse(result);

        verify(validatorContext, times(1)).disableDefaultConstraintViolation();
        verify(validatorContext, times(1)).buildConstraintViolationWithTemplate("Currency is required when any salary value is filled");
        verify(violationBuilder, times(1)).addPropertyNode("currency");
    }

    @Test
    public void isValid_ifSalaryNullAndCurrencyNotNull_returnFalse() {
        when(validatorContext.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);
        when(violationBuilder.addPropertyNode(anyString())).thenReturn(customizableContext);
        createUpdateDto.setSalaryMin(null);
        createUpdateDto.setSalaryMax(null);
        createUpdateDto.setCurrency(Currency.RUB);

        boolean result = validator.isValid(createUpdateDto, validatorContext);

        assertFalse(result);

        verify(validatorContext, times(1)).disableDefaultConstraintViolation();
        verify(validatorContext, times(1)).buildConstraintViolationWithTemplate("Currency must not be filled when none of the salary values are filled");
        verify(violationBuilder, times(1)).addPropertyNode("currency");
    }

    @Test
    public void isValid_ifMaxSalaryLessThanMinSalary() {
        when(validatorContext.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);
        when(violationBuilder.addPropertyNode(anyString())).thenReturn(customizableContext);
        createUpdateDto.setSalaryMin(300);
        createUpdateDto.setSalaryMax(100);
        createUpdateDto.setCurrency(Currency.RUB);

        boolean result = validator.isValid(createUpdateDto, validatorContext);

        assertFalse(result);

        verify(validatorContext, times(1)).disableDefaultConstraintViolation();
        verify(validatorContext, times(1)).buildConstraintViolationWithTemplate("Max salary must be greater than or equal to the min salary");
        verify(violationBuilder, times(1)).addPropertyNode("salaryMax");
    }

}