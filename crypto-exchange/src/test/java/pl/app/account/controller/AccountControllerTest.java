//package pl.app.account.controller;
//
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import jakarta.annotation.PostConstruct;
//import jakarta.persistence.EntityManager;
//import org.hibernate.SessionFactory;
//import org.hibernate.stat.Statistics;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.ArgumentCaptor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.SpyBean;
//import org.springframework.data.domain.Page;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.MvcResult;
//import pl.app.Main;
//import pl.app.account.model.Account;
//import pl.app.account.model.SubAccount;
//import pl.app.account.model.command.CreateAccountCommand;
//import pl.app.account.model.command.ExchangeCurrencyCommand;
//import pl.app.account.model.command.UpdateAccountCommand;
//import pl.app.account.model.dto.AccountDto;
//import pl.app.account.model.dto.SubAccountDto;
//import pl.app.account.model.enums.CurrencyCode;
//import pl.app.account.repository.AccountRepository;
//import pl.app.account.service.AccountService;
//import pl.app.currency.exception.CurrencyConversionException;
//import pl.app.currency.service.CurrencyRateProvider;
//
//import java.math.BigDecimal;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.reset;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@SpringBootTest(classes = Main.class)
//@AutoConfigureMockMvc
//class AccountControllerTest {
//
//    @Autowired
//    private MockMvc postman;
//
//    @SpyBean
//    private AccountService accountService;
//
//    @SpyBean
//    private CurrencyRateProvider currencyRateProvider;
//
//    @SpyBean
//    private AccountRepository accountRepository;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Autowired
//    private EntityManager entityManager;
//    private Statistics statistics;
//
//    @PostConstruct
//    public void init() {
//        SessionFactory sessionFactory = entityManager.getEntityManagerFactory().unwrap(SessionFactory.class);
//        sessionFactory.getStatistics().setStatisticsEnabled(true);
//        statistics = sessionFactory.getStatistics();
//    }
//
//    @BeforeEach
//    public void clear() {
//        reset(accountRepository);
//        accountRepository.deleteAll();
//        statistics.clear();
//    }
//
//    @Test
//    public void shouldCreateAccount() throws Exception {
//        //given:
//        CreateAccountCommand command = new CreateAccountCommand("02311278901", "Jan", "Kowalski", BigDecimal.valueOf(1000));
//
//        //when:
//        postman.perform(post("/api/account")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(command)))
//                .andExpect(status().isCreated());
//
//        //then:
//        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
//        verify(accountRepository, times(1)).save(accountCaptor.capture());
//        verify(accountService, times(1)).createAccount(command);
//        Account account = accountCaptor.getValue();
//        assertEquals(command.pesel(), account.getPesel());
//        assertEquals(command.name(), account.getName());
//        assertEquals(command.surname(), account.getSurname());
//    }
//
//    @Test
//    public void shouldThrowExceptionOnCreateAccountWithInvalidAge() throws Exception {
//        //given:
//        CreateAccountCommand command = new CreateAccountCommand("08311278901", "Jan", "Kowalski", BigDecimal.valueOf(1000));
//
//        //when:
//        postman.perform(post("/api/account")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(command)))
//                .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    public void shouldThrowExceptionOnCreateAccountWithInvalidBalance() throws Exception {
//        //given:
//        CreateAccountCommand command = new CreateAccountCommand("02311278901", "Jan", "Kowalski", BigDecimal.valueOf(-1));
//
//        //when:
//        postman.perform(post("/api/account")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(command)))
//                .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    public void shouldThrowExceptionOnCreateExistingAccount() throws Exception {
//        //given:
//        Account account = new Account("02311278901", "Jan", "Kowalski", BigDecimal.valueOf(1000));
//        accountRepository.save(account);
//
//        //when:
//        CreateAccountCommand command = new CreateAccountCommand("02311278901", "Jan", "Kowalski", BigDecimal.valueOf(1000));
//        postman.perform(post("/api/account")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(command)))
//                .andExpect(status().isConflict());
//    }
//
//    @Test
//    public void shouldGetAccount() throws Exception {
//        //given:
//        Account account = new Account("02311278901", "Jan", "Kowalski", BigDecimal.valueOf(0));
//        when(accountRepository.findByPeselWithSubAccounts("02311278901")).thenReturn(Optional.of(account));
//
//        //when:
//        MvcResult result = postman.perform(get("/api/account/{pesel}", account.getPesel())
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andReturn();
//
//        //then:
//        String response = result.getResponse().getContentAsString();
//        AccountDto responseAccount = objectMapper.readValue(response, AccountDto.class);
//
//        long executionCount = statistics.getQueryExecutionCount();
//        assertEquals(1L, executionCount);
//
//        assertAccount(responseAccount, account);
//        verify(accountRepository, times(1)).findByPeselWithSubAccounts(account.getPesel());
//        verify(accountService, times(1)).getAccount(account.getPesel());
//    }
//
//    @Test
//    public void shouldThrowExceptionOnGetAccount() throws Exception {
//        when(accountRepository.findByPeselWithSubAccounts("02311278901")).thenReturn(Optional.empty());
//
//        postman.perform(get("/api/account/{pesel}", "02311278901")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isNotFound());
//
//        verify(accountRepository, times(1)).findByPeselWithSubAccounts("02311278901");
//        verify(accountService, times(1)).getAccount("02311278901");
//    }
//
//    @Test
//    public void shouldGetAccounts() throws Exception {
//        //given:
//        Account a1 = new Account("02311278901", "Jan", "Kowalski", BigDecimal.valueOf(1000));
//        Account a2 = new Account("90311278902", "Monika", "Kowalska", BigDecimal.valueOf(3000));
//        List<Account> accounts = Arrays.asList(a1, a2);
//        accountRepository.saveAll(accounts);
//
//        //when:
//        MvcResult result = postman.perform(get("/api/account")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andReturn();
//
//        //then:
//        String response = result.getResponse().getContentAsString();
//        Page<AccountDto> responseAccounts = objectMapper.readValue(response, new TypeReference<RestResponsePage<AccountDto>>() {
//        });
//
//        long executionCount = statistics.getQueryExecutionCount();
//        assertEquals(1L, executionCount);
//
//        assertEquals(accounts.size(), responseAccounts.getTotalElements());
//        verify(accountService, times(1)).getAccounts(any());
//        verify(accountRepository, times(1)).findAllWithSubAccounts(any());
//    }
//
//    @Test
//    public void shouldUpdateAccountData() throws Exception {
//        //given:
//        Account account = new Account("02311278901", "Jan", "Kowalski", BigDecimal.valueOf(1000));
//        accountRepository.save(account);
//        UpdateAccountCommand command = new UpdateAccountCommand("02311278901", "Jan", "Nowacki");
//
//        //when:
//        MvcResult result = postman.perform(put("/api/account/{pesel}", account.getPesel())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(command)))
//                .andExpect(status().isOk())
//                .andReturn();
//
//        //then:
//        String response = result.getResponse().getContentAsString();
//        AccountDto resultAccount = objectMapper.readValue(response, AccountDto.class);
//
//        assertEquals(command.pesel(), resultAccount.pesel());
//        assertEquals(command.name(), resultAccount.name());
//        assertEquals(command.surname(), resultAccount.surname());
//
//        verify(accountRepository, times(1)).findByPeselWithSubAccounts("02311278901");
//        verify(accountService, times(1)).updateAccountData("02311278901", command);
//    }
//
//    @Test
//    public void shouldThrowConflictExceptionOnUpdateAccountData() throws Exception {
//        //given:
//        Account account = new Account("02311278901", "Jan", "Kowalski", BigDecimal.valueOf(1000));
//        accountRepository.save(account);
//        UpdateAccountCommand command = new UpdateAccountCommand("02311278902", "Jan", "Nowacki");
//
//        //when:
//        postman.perform(put("/api/account/{pesel}", account.getPesel())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(command)))
//                .andExpect(status().isConflict());
//
//        verify(accountRepository, times(1)).findByPeselWithSubAccounts("02311278901");
//        verify(accountService, times(1)).updateAccountData("02311278901", command);
//    }
//
//    @Test
//    public void shouldDeleteAccount() throws Exception {
//        //given:
//        Account account = new Account("02311278901", "Jan", "Kowalski", BigDecimal.valueOf(0));
//        accountRepository.save(account);
//
//        //when:
//        postman.perform(delete("/api/account/{pesel}", account.getPesel())
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isNoContent());
//
//        //then:
//        verify(accountRepository, times(1)).findByPeselWithSubAccounts("02311278901");
//        verify(accountService, times(1)).deleteAccount("02311278901");
//    }
//
//    @Test
//    public void shouldThrowExceptionOnDeleteNotEmptyAccount() throws Exception {
//        //given:
//        Account account = new Account("02311278901", "Jan", "Kowalski", BigDecimal.valueOf(1000));
//        accountRepository.save(account);
//
//        //when:
//        postman.perform(delete("/api/account/{pesel}", account.getPesel())
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isConflict());
//
//        //then:
//        verify(accountRepository, times(1)).findByPeselWithSubAccounts("02311278901");
//        verify(accountService, times(1)).deleteAccount("02311278901");
//    }
//
//    @Test
//    public void shouldExchangeCurrency() throws Exception {
//        //given:
//        Account account = new Account("02311278901", "Jan", "Kowalski", BigDecimal.valueOf(10000));
//        accountRepository.save(account);
//        ExchangeCurrencyCommand command = new ExchangeCurrencyCommand("02311278901", "USD", "ETH", BigDecimal.valueOf(10000));
//        when(currencyRateProvider.getExchangeRate(CurrencyCode.USD, CurrencyCode.ETH))
//                .thenReturn(BigDecimal.valueOf(0.0003));
//
//        //when:
//        MvcResult result = postman.perform(put("/api/account/{pesel}/exchange", account.getPesel())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(command)))
//                .andExpect(status().isOk())
//                .andReturn();
//
//        //then:
//        String response = result.getResponse().getContentAsString();
//        AccountDto resultAccount = objectMapper.readValue(response, AccountDto.class);
//        SubAccountDto ethResult = resultAccount.subAccounts().stream()
//                .filter(s -> s.currency().equals(CurrencyCode.ETH))
//                .findFirst()
//                .orElseThrow(() -> new AssertionError("ETH SubAccount not found"));
//
//        SubAccountDto usdResult = resultAccount.subAccounts().stream()
//                .filter(s -> s.currency().equals(CurrencyCode.USD))
//                .findFirst()
//                .orElseThrow(() -> new AssertionError("USD SubAccount not found"));
//
//        assertEquals(account.getPesel(), resultAccount.pesel());
//        assertEquals(account.getName(), resultAccount.name());
//        assertEquals(account.getSurname(), resultAccount.surname());
//        assertEquals(3, resultAccount.subAccounts().size());
//        assertEquals(0, BigDecimal.valueOf(0).compareTo(usdResult.amount()));
//        assertEquals(0, BigDecimal.valueOf(3).compareTo(ethResult.amount()));
//
//        verify(accountRepository, times(1)).findByPeselWithSubAccountsWithLock("02311278901");
//        verify(accountService, times(1)).exchangeCurrency("02311278901", new ExchangeCurrencyCommand("02311278901", "USD", "ETH", BigDecimal.valueOf(10000)));
//    }
//
//    @Test
//    public void shouldThrowConflictExceptionOnExchangeCurrency() throws Exception {
//        //given:
//        Account account = new Account("02311278901", "Jan", "Kowalski", BigDecimal.valueOf(1000));
//        accountRepository.save(account);
//        ExchangeCurrencyCommand command = new ExchangeCurrencyCommand("02311278902", "USD", "BTC", BigDecimal.valueOf(100));
//
//        //when:
//        postman.perform(put("/api/account/{pesel}/exchange", account.getPesel())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(command)))
//                .andExpect(status().isConflict());
//
//        //then:
//        verify(accountRepository, times(1)).findByPeselWithSubAccountsWithLock("02311278901");
//        verify(accountService, times(1)).exchangeCurrency("02311278901", command);
//    }
//
//    @Test
//    public void shouldThrowExceptionOnExchangeCurrencyWithNotEnoughMoney() throws Exception {
//        //given:
//        Account account = new Account("02311278901", "Jan", "Kowalski", BigDecimal.valueOf(1000));
//        accountRepository.save(account);
//        ExchangeCurrencyCommand command = new ExchangeCurrencyCommand("02311278901", "USD", "BTC", BigDecimal.valueOf(1001));
//
//        //when:
//        postman.perform(put("/api/account/{pesel}/exchange", account.getPesel())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(command)))
//                .andExpect(status().isBadRequest());
//
//        //then:
//        verify(accountRepository, times(1)).findByPeselWithSubAccountsWithLock("02311278901");
//        verify(accountService, times(1)).exchangeCurrency("02311278901", command);
//    }
//
//    @Test
//    public void shouldThrowServiceUnavailableExceptionOnExchangeCurrency() throws Exception {
//        //given:
//        Account account = new Account("02311278901", "Jan", "Kowalski", BigDecimal.valueOf(1000));
//        accountRepository.save(account);
//        ExchangeCurrencyCommand command = new ExchangeCurrencyCommand("02311278901", "USD", "ETH", BigDecimal.valueOf(100));
//        when(currencyRateProvider.getExchangeRate(CurrencyCode.USD, CurrencyCode.ETH))
//                .thenThrow(new CurrencyConversionException());
//
//        //when:
//        postman.perform(put("/api/account/{pesel}/exchange", account.getPesel())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(command)))
//                .andExpect(status().isServiceUnavailable());
//
//        //then:
//        verify(accountRepository, times(1)).findByPeselWithSubAccountsWithLock("02311278901");
//        verify(accountService, times(1)).exchangeCurrency("02311278901", command);
//    }
//
//    private void assertAccount(AccountDto accountDto, Account account) {
//        assertEquals(accountDto.pesel(), account.getPesel());
//        assertEquals(accountDto.name(), account.getName());
//        assertEquals(accountDto.surname(), account.getSurname());
//        assertEquals(accountDto.subAccounts().size(), account.getSubAccounts().size());
//
//        accountDto.subAccounts().forEach(subAccountDto -> {
//            SubAccount correspondingSubAccount = account.getSubAccounts().stream()
//                    .filter(subAccount -> subAccount.getCurrency().equals(subAccountDto.currency()))
//                    .findFirst()
//                    .orElseThrow(() -> new AssertionError("Matching SubAccount not found for currency: " + subAccountDto.currency()));
//            assertSubAccount(subAccountDto, correspondingSubAccount);
//        });
//    }
//
//    private void assertSubAccount(SubAccountDto subAccountDto, SubAccount subAccount) {
//        assertEquals(subAccountDto.currency(), subAccount.getCurrency());
//        assertEquals(0, subAccountDto.amount().compareTo(subAccount.getAmount()));
//    }
//}