package pl.app.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import pl.app.validation.impl.CurrencyValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = CurrencyValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCurrency {
    String message() default "INVALID_CURRENCY";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String name();
}