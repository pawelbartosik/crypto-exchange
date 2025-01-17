package pl.app.account.service;

import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PessimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.app.account.exception.AccountAlreadyExistException;
import pl.app.account.exception.AccountConflictException;
import pl.app.account.exception.AccountLockedException;
import pl.app.account.exception.AccountNotEmptyException;
import pl.app.account.exception.AccountNotFoundException;
import pl.app.account.exception.AccountUpdateConflictException;
import pl.app.account.exception.NotEnoughMoneyException;
import pl.app.account.exception.SubAccountAlreadyExistException;
import pl.app.account.exception.SubAccountNotFoundException;
import pl.app.account.model.Account;
import pl.app.account.model.SubAccount;
import pl.app.account.model.command.CreateAccountCommand;
import pl.app.account.model.command.CreateSubAccountCommand;
import pl.app.account.model.command.ExchangeCurrencyCommand;
import pl.app.account.model.command.TransactionCommand;
import pl.app.account.model.command.UpdateAccountCommand;
import pl.app.account.model.dto.AccountDto;
import pl.app.account.model.view.AccountView;
import pl.app.account.repository.AccountRepository;
import pl.app.account.repository.AccountViewRepository;
import pl.app.account.repository.SubAccountRepository;
import pl.app.currency.model.Currency;
import pl.app.currency.service.CurrencyService;
import pl.app.rate.service.CurrencyRateProvider;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountViewRepository accountViewRepository;
    private final SubAccountRepository subAccountRepository;
    private final CurrencyRateProvider currencyRateProvider;
    private final CurrencyService currencyService;

    @Transactional(readOnly = true)
    public Page<AccountView> getAccounts(Pageable pageable) {
        return accountViewRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public AccountDto getAccount(String pesel) {
        return accountRepository.findByPeselWithSubAccounts(pesel)
                .map(AccountDto::fromAccount)
                .orElseThrow(AccountNotFoundException::new);
    }

    @Transactional
    public AccountDto createAccount(CreateAccountCommand command) {
        try {
            return AccountDto.fromAccount(accountRepository
                    .save(new Account(command.pesel(), command.name(), command.surname())));
        } catch (Exception e) {
            throw new AccountAlreadyExistException();
        }
    }

    @Transactional
    public AccountDto updateAccountData(String pesel, UpdateAccountCommand command) {
        try {
            Account account = accountRepository.findByPesel(pesel)
                    .orElseThrow(AccountNotFoundException::new);
            if (!account.getPesel().equals(command.pesel())) {
                throw new AccountConflictException();
            }

            account.setName(command.name());
            account.setSurname(command.surname());
            return AccountDto.fromAccount(account);
        } catch (OptimisticLockException e) {
            throw new AccountUpdateConflictException();
        } catch (PessimisticLockException | LockTimeoutException e) {
            throw new AccountLockedException();
        }
    }

    @Transactional
    public void deleteAccount(String pesel) {
        try {
            Account account = accountRepository.findByPeselWithSubAccountsWithLock(pesel)
                    .orElseThrow(AccountNotFoundException::new);

            if (account.getSubAccounts()
                    .stream()
                    .map(SubAccount::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .compareTo(BigDecimal.ZERO) > 0) {
                throw new AccountNotEmptyException();
            }

            accountRepository.deleteAccountByPesel(pesel);
        } catch (PessimisticLockException | LockTimeoutException e) {
            throw new AccountLockedException();
        }
    }

    @Transactional
    public AccountDto createSubAccount(String pesel, CreateSubAccountCommand command) {
        Account account = getAccountAndVerifyWithCommand(pesel, command.pesel());

        if (account.getSubAccounts().stream().anyMatch(subAccount -> subAccount.getCurrency().getCode().equals(command.currency()))) {
            throw new SubAccountAlreadyExistException();
        }

        Currency currency = currencyService.getCurrency(command.currency());
        SubAccount subAccount = new SubAccount(account, currency, BigDecimal.ZERO);
        account.getSubAccounts().add(subAccount);

        return AccountDto.fromAccount(account);
    }

    @Transactional
    public AccountDto deposit(String pesel, TransactionCommand command) {
        Account account = getAccountAndVerifyWithCommand(pesel, command.pesel());

        SubAccount subAccount = subAccountRepository.findByPeselAndCurrencyWithLock(pesel, command.currency())
                .orElseThrow(SubAccountNotFoundException::new);

        subAccount.setAmount(subAccount.getAmount().add(command.amount()));

        return AccountDto.fromAccount(account);
    }

    @Transactional
    public AccountDto withdraw(String pesel, TransactionCommand command) {
        Account account = getAccountAndVerifyWithCommand(pesel, command.pesel());

        SubAccount subAccount = subAccountRepository.findByPeselAndCurrencyWithLock(pesel, command.currency())
                .orElseThrow(SubAccountNotFoundException::new);

        if (subAccount.getAmount().compareTo(command.amount()) < 0) {
            throw new NotEnoughMoneyException();
        }

        subAccount.setAmount(subAccount.getAmount().subtract(command.amount()));

        return AccountDto.fromAccount(account);
    }

    @Transactional
    public AccountDto exchangeCurrency(String pesel, ExchangeCurrencyCommand command) {
        try {
            Account account = getAccountAndVerifyWithCommand(pesel, command.pesel());

            int fromId = SubAccount.getSubAccount(account, command.from()).getId();
            int toId = SubAccount.getSubAccount(account, command.to()).getId();
            SubAccount from;
            SubAccount to;

            if (fromId < toId) {
                from = subAccountRepository.findByIdWithLock(fromId).orElseThrow(SubAccountNotFoundException::new);
                to = subAccountRepository.findByIdWithLock(toId).orElseThrow(SubAccountNotFoundException::new);
            } else {
                to = subAccountRepository.findByIdWithLock(toId).orElseThrow(SubAccountNotFoundException::new);
                from = subAccountRepository.findByIdWithLock(fromId).orElseThrow(SubAccountNotFoundException::new);
            }

            if (from.getAmount().compareTo(command.amount()) < 0) {
                throw new NotEnoughMoneyException();
            }

            BigDecimal rate = currencyRateProvider.getExchangeRate(from.getCurrency().getCode(), to.getCurrency().getCode());

            from.setAmount(from.getAmount().subtract(command.amount()));
            to.setAmount(to.getAmount().add(command.amount().multiply(rate)));

            return AccountDto.fromAccount(account);
        } catch (PessimisticLockException | LockTimeoutException e) {
            throw new AccountLockedException();
        }
    }

    private Account getAccountAndVerifyWithCommand(String pesel, String commandPesel) {
        Account account = accountRepository.findByPeselWithSubAccounts(pesel)
                .orElseThrow(AccountNotFoundException::new);

        if (!account.getPesel().equals(commandPesel)) {
            throw new AccountConflictException();
        }

        return account;
    }
}
