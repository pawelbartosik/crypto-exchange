package pl.app.validation.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import pl.app.account.model.enums.CurrencyCode;
import pl.app.validation.annotation.ValidCurrency;

public class CurrencyValidator implements ConstraintValidator<ValidCurrency, String> {

    @Override
    public boolean isValid(String currencyCode, ConstraintValidatorContext context) {
        if (currencyCode == null) {
            return false;
        }
        try {
            CurrencyCode.valueOf(currencyCode);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
