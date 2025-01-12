package pl.app.account.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import pl.app.account.exception.AccountNotEmptyException;
import pl.app.account.exception.AccountNotFoundException;
import pl.app.account.model.Account;
import pl.app.account.model.SubAccount;
import pl.app.account.model.command.CreateAccountCommand;
import pl.app.account.model.command.ExchangeCurrencyCommand;
import pl.app.account.model.command.UpdateAccountCommand;
import pl.app.account.model.dto.AccountDto;
import pl.app.account.model.dto.SubAccountDto;
import pl.app.account.model.enums.CurrencyCode;
import pl.app.account.repository.AccountRepository;
import pl.app.currency.service.CurrencyRateProvider;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CurrencyRateProvider currencyRateProvider;

    @InjectMocks
    private AccountService accountService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void shouldCreateAccount() {
        //given:
        CreateAccountCommand command = new CreateAccountCommand("02315678901", "Jan", "Kowalski", BigDecimal.valueOf(1000));
        Account account = new Account("12315678901", "Jan", "Kowalski", BigDecimal.valueOf(1000));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        //when:
        AccountDto result = accountService.createAccount(command);

        //then:
        assertAccount(result, account);
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    public void shouldGetAccount() {
        //given:
        Account account = new Account("02315678901", "Jan", "Kowalski", BigDecimal.valueOf(1000));
        when(accountRepository.findByPeselWithSubAccounts("02315678901")).thenReturn(Optional.of(account));

        //when:
        AccountDto result = accountService.getAccount("02315678901");

        //then:
        assertAccount(result, account);
        verify(accountRepository, times(1)).findByPeselWithSubAccounts("02315678901");
    }

    @Test
    public void shouldThrowExceptionOnGetAccount() {
        //given:
        when(accountRepository.findByPeselWithSubAccounts("02315678901")).thenReturn(Optional.empty());

        //when:
        //then:
        assertThrows(AccountNotFoundException.class, () -> accountService.getAccount("02315678901"));
        verify(accountRepository, times(1)).findByPeselWithSubAccounts("02315678901");
    }

    @Test
    public void shouldGetAccounts() {
        //given:
        Account a1 = new Account("02315678901", "Jan", "Kowalski", BigDecimal.valueOf(1000));
        Account a2 = new Account("02315678902", "Monika", "Kowalska", BigDecimal.valueOf(3000));
        Pageable pageable = mock(Pageable.class);
        List<Account> expected = Arrays.asList(a1, a2);
        when(accountRepository.findAllWithSubAccounts(pageable)).thenReturn(new PageImpl<>(expected, PageRequest.of(0, expected.size()), expected.size()));


        //when:
        Page<AccountDto> result = accountService.getAccounts(pageable);

        //then:
        assertEquals(expected.size(), result.getTotalElements());
        verify(accountRepository, times(1)).findAllWithSubAccounts(pageable);
        assertAccount(result.getContent().get(0), a1);
        assertAccount(result.getContent().get(1), a2);
    }

    @Test
    public void shouldUpdateAccountData() {
        //given:
        Account account = new Account("02315678901", "Jan", "Kowalski", BigDecimal.valueOf(1000));
        when(accountRepository.findByPeselWithSubAccounts("02315678901")).thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(account);

        //when:
        AccountDto result = accountService.updateAccountData("02315678901", new UpdateAccountCommand("02315678901", "Jan", "Kowalski"));

        //then:
        assertAccount(result, account);
        verify(accountRepository, times(1)).findByPeselWithSubAccounts("02315678901");
    }

    @Test
    public void shouldDeleteAccount() {
        //given:
        Account account = new Account("02315678901", "Jan", "Kowalski", BigDecimal.valueOf(0));
        when(accountRepository.findByPeselWithSubAccounts("02315678901")).thenReturn(Optional.of(account));

        //when:
        accountService.deleteAccount("02315678901");

        //then:
        verify(accountRepository, times(1)).findByPeselWithSubAccounts("02315678901");
        verify(accountRepository, times(1)).deleteAccountByPesel("02315678901");
    }

    @Test
    public void shouldThrowExceptionOnDeleteAccount() {
        //given:
        Account account = new Account("02315678901", "Jan", "Kowalski", BigDecimal.valueOf(1000));
        when(accountRepository.findByPeselWithSubAccounts("02315678901")).thenReturn(Optional.of(account));

        //when:
        //then:
        assertThrows(AccountNotEmptyException.class, () -> accountService.deleteAccount("02315678901"));
        verify(accountRepository, times(1)).findByPeselWithSubAccounts("02315678901");
    }

    @Test
    public void shouldExchangeCurrency() {
        //given:
        Account account = new Account("02315678901", "Jan", "Kowalski", BigDecimal.valueOf(1000));
        ExchangeCurrencyCommand command = new ExchangeCurrencyCommand("02315678901", "USD", "BTC", BigDecimal.valueOf(1000));
        when(accountRepository.findByPeselWithSubAccountsForExchange("02315678901")).thenReturn(Optional.of(account));
        when(currencyRateProvider.getExchangeRate(CurrencyCode.USD, CurrencyCode.BTC))
                .thenReturn(BigDecimal.valueOf(0.00001));

        //when:
        AccountDto result = accountService.exchangeCurrency("02315678901", command);

        //then:
        SubAccountDto btcResult = result.subAccounts().stream()
                .filter(s -> s.currency().equals(CurrencyCode.BTC))
                .findFirst()
                .orElseThrow(() -> new AssertionError("BTC SubAccount not found"));

        SubAccountDto usdResult = result.subAccounts().stream()
                .filter(s -> s.currency().equals(CurrencyCode.USD))
                .findFirst()
                .orElseThrow(() -> new AssertionError("USD SubAccount not found"));

        assertEquals(0, BigDecimal.valueOf(0).compareTo(usdResult.amount()));
        assertEquals(0, BigDecimal.valueOf(0.01).compareTo(btcResult.amount()));
        verify(accountRepository, times(1)).findByPeselWithSubAccountsForExchange("02315678901");
        verify(currencyRateProvider, times(1)).getExchangeRate(CurrencyCode.USD, CurrencyCode.BTC);
    }

    private void assertAccount(AccountDto accountDto, Account account) {
        assertEquals(accountDto.pesel(), account.getPesel());
        assertEquals(accountDto.name(), account.getName());
        assertEquals(accountDto.surname(), account.getSurname());
        assertEquals(accountDto.subAccounts().size(), account.getSubAccounts().size());

        accountDto.subAccounts().forEach(subAccountDto -> {
            SubAccount correspondingSubAccount = account.getSubAccounts().stream()
                    .filter(subAccount -> subAccount.getCurrency().equals(subAccountDto.currency()))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("Matching SubAccount not found for currency: " + subAccountDto.currency()));
            assertSubAccount(subAccountDto, correspondingSubAccount);
        });
    }

    private void assertSubAccount(SubAccountDto subAccountDto, SubAccount subAccount) {
        assertEquals(subAccountDto.currency(), subAccount.getCurrency());
        assertEquals(0, subAccountDto.amount().compareTo(subAccount.getAmount()));
    }
}