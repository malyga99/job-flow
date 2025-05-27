package com.jobflow.job_tracker_service.validation;

import com.jobflow.job_tracker_service.jobApplication.Currency;
import com.jobflow.job_tracker_service.jobApplication.JobApplicationCreateUpdateDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SalaryValidator implements ConstraintValidator<ValidSalary, JobApplicationCreateUpdateDto> {

    @Override
    public boolean isValid(JobApplicationCreateUpdateDto value, ConstraintValidatorContext context) {
        Integer salaryMin = value.getSalaryMin();
        Integer salaryMax = value.getSalaryMax();
        Currency currency = value.getCurrency();

        boolean anySalaryProvider = salaryMin != null || salaryMax != null;
        boolean bothSalaryProvider = salaryMin != null && salaryMax != null;

        if (anySalaryProvider && currency == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Currency is required when any salary value is filled")
                    .addPropertyNode("currency")
                    .addConstraintViolation();
            return false;
        }

        if (!anySalaryProvider && currency != null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Currency must not be filled when none of the salary values are filled")
                    .addPropertyNode("currency")
                    .addConstraintViolation();
            return false;
        }

        if (bothSalaryProvider && (salaryMax < salaryMin)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Max salary must be greater than or equal to the min salary")
                    .addPropertyNode("salaryMax")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
