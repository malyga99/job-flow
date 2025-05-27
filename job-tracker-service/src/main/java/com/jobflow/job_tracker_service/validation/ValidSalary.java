package com.jobflow.job_tracker_service.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SalaryValidator.class)
@Documented
public @interface ValidSalary {
    String message() default "Invalid salary";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
