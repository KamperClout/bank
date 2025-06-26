package ru.baranov.calculatorservice.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = AgeValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Age {
    String message() default "Дата рождения должна быть не позднее 18 лет с текущего дня";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    int value() default 0;
}