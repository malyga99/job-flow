package com.jobflow.job_tracker_service.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SourceDetailsValidator.class)
@Documented
public @interface ValidSourceDetails {
    String message() default "Invalid source details";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
