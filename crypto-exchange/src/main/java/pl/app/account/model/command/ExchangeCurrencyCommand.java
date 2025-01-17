package pl.app.account.model.command;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import pl.app.validation.annotation.ValidCurrency;
import pl.app.validation.annotation.ValidPeselAge;

import java.math.BigDecimal;

public record ExchangeCurrencyCommand(@Pattern(regexp = "\\d{11}", message = "PESEL_MUST_BE_VALID")
                                      @ValidPeselAge String pesel,
                                      @ValidCurrency(name = "currency") String from,
                                      @ValidCurrency(name = "currency") String to,
                                      @DecimalMin(value = "0", inclusive = false, message = "AMOUNT_MUST_POSITIVE") BigDecimal amount) {
}
