package pl.app.account.model.dto;

import pl.app.account.model.SubAccount;
import pl.app.account.model.enums.CurrencyCode;

import java.math.BigDecimal;

public record SubAccountDto(CurrencyCode currency, BigDecimal amount) {

    public static SubAccountDto fromSubAccount(SubAccount subAccount) {
        return new SubAccountDto(subAccount.getCurrency(), subAccount.getAmount());
    }
}
