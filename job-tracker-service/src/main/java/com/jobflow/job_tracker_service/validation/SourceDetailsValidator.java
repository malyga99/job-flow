package com.jobflow.job_tracker_service.validation;

import com.jobflow.job_tracker_service.jobApplication.JobApplicationCreateUpdateDto;
import com.jobflow.job_tracker_service.jobApplication.Source;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SourceDetailsValidator implements ConstraintValidator<ValidSourceDetails, JobApplicationCreateUpdateDto> {

    @Override
    public boolean isValid(JobApplicationCreateUpdateDto value, ConstraintValidatorContext context) {
        boolean isSourceOther = value.getSource() == Source.OTHER;
        String sourceDetails = value.getSourceDetails();

        if (isSourceOther && (sourceDetails == null || sourceDetails.isBlank())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Source details are required when source is OTHER")
                    .addPropertyNode("sourceDetails")
                    .addConstraintViolation();
            return false;
        }

        if (!isSourceOther && sourceDetails != null ) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Source details must not be filled when source is not OTHER")
                    .addPropertyNode("sourceDetails")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }

}
