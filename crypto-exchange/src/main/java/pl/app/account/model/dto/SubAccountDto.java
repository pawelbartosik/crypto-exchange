package pl.app.account.model.dto;

import pl.app.account.model.SubAccount;
import pl.app.currency.model.Currency;

import java.math.BigDecimal;

public record SubAccountDto(Currency currency, BigDecimal amount) {

    public static SubAccountDto fromSubAccount(SubAccount subAccount) {
        return new SubAccountDto(subAccount.getCurrency(), subAccount.getAmount());
    }
}
