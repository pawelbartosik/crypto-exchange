package pl.app.currency.model.command;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

public record CreateCurrencyCommand(@NotEmpty(message = "NAME_CANNOT_BE_EMPTY") String name,
                                    @Pattern(regexp = "^[A-Z]{3}$", message = "CODE_MUST_BE_VALID") String code) {
}
