package pl.app.validation.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import pl.app.validation.annotation.ValidPeselAge;

import java.time.LocalDate;
import java.time.Period;

public class PeselAgeValidator implements ConstraintValidator<ValidPeselAge, String> {

    @Override
    public boolean isValid(String pesel, ConstraintValidatorContext context) {
        if (pesel == null || pesel.length() != 11) {
            return false;
        }

        int year = Integer.parseInt(pesel.substring(0, 2));
        int month = Integer.parseInt(pesel.substring(2, 4));
        int day = Integer.parseInt(pesel.substring(4, 6));

        if (month > 20) {
            month -= 20;
            year += 2000;
        } else {
            year += 1900;
        }

        try {
            LocalDate birthDate = LocalDate.of(year, month, day);
            LocalDate currentDate = LocalDate.now();
            return Period.between(birthDate, currentDate).getYears() >= 18;
        } catch (Exception e) {
            return false;
        }
    }
}
