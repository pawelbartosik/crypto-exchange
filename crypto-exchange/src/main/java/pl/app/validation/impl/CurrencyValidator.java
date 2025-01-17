package pl.app.validation.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import pl.app.currency.repository.CurrencyRepository;
import pl.app.validation.annotation.ValidCurrency;

@RequiredArgsConstructor
public class CurrencyValidator implements ConstraintValidator<ValidCurrency, String> {

    private final CurrencyRepository currencyRepository;
    private String currencyCode;

    @Override
    public void initialize(ValidCurrency constraintAnnotation) {
        this.currencyCode = constraintAnnotation.name();
    }

    @Override
    public boolean isValid(String currencyCode, ConstraintValidatorContext context) {
        return currencyRepository.existsByCode(currencyCode);
    }
}
