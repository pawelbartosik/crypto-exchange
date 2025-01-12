package pl.app.account.model.dto;

import pl.app.account.model.Account;

import java.util.List;

public record AccountDto(Integer id, String pesel, String name, String surname, List<SubAccountDto> subAccounts) {

    public static AccountDto fromAccount(Account account) {
        return new AccountDto(account.getId(), account.getPesel(), account.getName(), account.getSurname(),
                account.getSubAccounts().stream().map(SubAccountDto::fromSubAccount).toList());
    }
}
