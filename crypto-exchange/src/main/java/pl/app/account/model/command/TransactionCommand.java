package pl.app.account.model.command;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import pl.app.validation.annotation.ValidCurrency;
import pl.app.validation.annotation.ValidPeselAge;

import java.math.BigDecimal;

public record TransactionCommand(
        @Pattern(regexp = "\\d{11}", message = "PESEL_MUST_BE_VALID")
        @ValidPeselAge String pesel,
        @ValidCurrency String currency,
        @DecimalMin(value = "0", message = "AMOUNT_CANNOT_BE_NEGATIVE") BigDecimal amount) {
}
