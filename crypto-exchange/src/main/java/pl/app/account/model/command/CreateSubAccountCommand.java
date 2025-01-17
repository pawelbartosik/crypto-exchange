package pl.app.account.model.command;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import pl.app.validation.annotation.ValidCurrency;
import pl.app.validation.annotation.ValidPeselAge;

public record CreateSubAccountCommand(@Pattern(regexp = "\\d{11}", message = "PESEL_MUST_BE_VALID")
                                      @ValidPeselAge String pesel,
                                      @NotEmpty(message = "NAME_CANNOT_BE_EMPTY") String name,
                                      @NotEmpty(message = "SURNAME_CANNOT_BE_EMPTY") String surname,
                                      @ValidCurrency(name = "currency") String currency) {
}
