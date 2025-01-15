package pl.app.account.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.app.account.model.command.CreateAccountCommand;
import pl.app.account.model.command.ExchangeCurrencyCommand;
import pl.app.account.model.command.UpdateAccountCommand;
import pl.app.account.model.dto.AccountDto;
import pl.app.account.model.view.AccountView;
import pl.app.account.service.AccountService;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
@Slf4j
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    public ResponseEntity<Page<AccountDto>> getAccounts(@PageableDefault Pageable pageable) {
        log.info("Getting all accounts");
        return ResponseEntity.ok(accountService.getAccounts(pageable));
    }

    @GetMapping("/{pesel}")
    public ResponseEntity<AccountView> getAccount(@PathVariable String pesel) {
        log.info("Getting account with pesel: {}", pesel);
        return ResponseEntity.ok(accountService.getAccountView(pesel));
    }

    @PostMapping
    public ResponseEntity<AccountDto> createAccount(@RequestBody @Valid CreateAccountCommand command) {
        log.info("Creating account: {}", command);
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.createAccount(command));
    }

    @PutMapping("/{pesel}")
    public ResponseEntity<AccountDto> updateAccountData(@PathVariable String pesel, @RequestBody @Valid UpdateAccountCommand command) {
        log.info("Updating account with pesel: {}", pesel);
        return ResponseEntity.ok(accountService.updateAccountData(pesel, command));
    }

    @DeleteMapping("/{pesel}")
    public ResponseEntity<Void> deleteAccount(@PathVariable String pesel) {
        log.info("Deleting account with pesel: {}", pesel);
        accountService.deleteAccount(pesel);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{pesel}/exchange")
    public ResponseEntity<AccountDto> exchangeCurrency(@PathVariable String pesel, @RequestBody @Valid ExchangeCurrencyCommand command) {
        log.info("Exchanging currency for account with pesel: {}", pesel);
        return ResponseEntity.ok(accountService.exchangeCurrency(pesel, command));
    }
}
