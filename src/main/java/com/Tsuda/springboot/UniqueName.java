package com.Tsuda.springboot;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;

@Documented
@Constraint(validatedBy = UniqueNameValidator.class)
@Target({ ElementType.TYPE,ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@ReportAsSingleViolation
public @interface UniqueName {

	String message() default " The typed name has already been used.";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

}
