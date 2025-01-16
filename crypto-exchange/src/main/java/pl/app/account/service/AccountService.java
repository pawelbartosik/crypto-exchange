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
import pl.app.account.exception.SubAccountNotFoundException;
import pl.app.account.model.Account;
import pl.app.account.model.SubAccount;
import pl.app.account.model.command.CreateAccountCommand;
import pl.app.account.model.command.ExchangeCurrencyCommand;
import pl.app.account.model.command.TransactionCommand;
import pl.app.account.model.command.UpdateAccountCommand;
import pl.app.account.model.dto.AccountDto;
import pl.app.account.model.enums.CurrencyCode;
import pl.app.account.model.view.AccountView;
import pl.app.account.repository.AccountRepository;
import pl.app.account.repository.AccountViewRepository;
import pl.app.account.repository.SubAccountRepository;
import pl.app.currency.service.CurrencyRateProvider;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountViewRepository accountViewRepository;
    private final SubAccountRepository subAccountRepository;
    private final CurrencyRateProvider currencyRateProvider;

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
                    .save(new Account(command.pesel(), command.name(), command.surname(), command.balanceUSD())));
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
    public AccountDto deposit(String pesel, TransactionCommand command) {
        Account account = accountRepository.findByPeselWithSubAccounts(pesel)
                .orElseThrow(AccountNotFoundException::new);

        if (!account.getPesel().equals(command.pesel())) {
            throw new AccountConflictException();
        }

        SubAccount subAccount = subAccountRepository.findByPeselAndCurrencyWithLock(pesel, CurrencyCode.valueOf(command.currency()))
                .orElseThrow(SubAccountNotFoundException::new);

        subAccount.setAmount(subAccount.getAmount().add(command.amount()));

        return AccountDto.fromAccount(account);
    }

    @Transactional
    public AccountDto withdraw(String pesel, TransactionCommand command) {
        Account account = accountRepository.findByPeselWithSubAccounts(pesel)
                .orElseThrow(AccountNotFoundException::new);

        if (!account.getPesel().equals(command.pesel())) {
            throw new AccountConflictException();
        }

        SubAccount subAccount = subAccountRepository.findByPeselAndCurrencyWithLock(pesel, CurrencyCode.valueOf(command.currency()))
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
            Account account = accountRepository.findByPeselWithSubAccounts(pesel)
                    .orElseThrow(AccountNotFoundException::new);

            if (!account.getPesel().equals(command.pesel())) {
                throw new AccountConflictException();
            }

            SubAccount from = SubAccount.getSubAccount(account, CurrencyCode.valueOf(command.from()));
            SubAccount to = SubAccount.getSubAccount(account, CurrencyCode.valueOf(command.to()));
            SubAccount first = from.getId() < to.getId() ? from : to;
            SubAccount second = from.getId() < to.getId() ? to : from;
            subAccountRepository.findByIdWithLock(first.getId());
            subAccountRepository.findByIdWithLock(second.getId());

            if (from.getAmount().compareTo(command.amount()) < 0) {
                throw new NotEnoughMoneyException();
            }

            BigDecimal rate = currencyRateProvider.getExchangeRate(from.getCurrency(), to.getCurrency());

            from.setAmount(from.getAmount().subtract(command.amount()));
            to.setAmount(to.getAmount().add(command.amount().multiply(rate)));

            return AccountDto.fromAccount(account);
        } catch (PessimisticLockException | LockTimeoutException e) {
            throw new AccountLockedException();
        }
    }
}
