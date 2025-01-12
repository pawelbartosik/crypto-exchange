package pl.app.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import pl.app.validation.impl.PeselAgeValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PeselAgeValidator.class)
public @interface ValidPeselAge {
    String message() default "USER_IS_UNDERAGE";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
