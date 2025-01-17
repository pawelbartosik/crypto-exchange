package pl.app.currency.controller;

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
import pl.app.currency.model.command.CreateCurrencyCommand;
import pl.app.currency.model.command.UpdateCurrencyCommand;
import pl.app.currency.model.dto.CurrencyDto;
import pl.app.currency.service.CurrencyService;

@RestController
@RequestMapping("/api/currency")
@RequiredArgsConstructor
@Slf4j
public class CurrencyController {

    private final CurrencyService currencyService;

    @GetMapping
    public ResponseEntity<Page<CurrencyDto>> getCurrencies(@PageableDefault Pageable pageable) {
        log.info("Getting all currencies");
        return ResponseEntity.ok(currencyService.getCurrencies(pageable));
    }

    @GetMapping("/{code}")
    public ResponseEntity<CurrencyDto> getCurrency(@PathVariable String code) {
        log.info("Getting currency with code: {}", code);
        return ResponseEntity.ok(currencyService.getCurrency(code));
    }

    @PostMapping
    public ResponseEntity<CurrencyDto> createCurrency(@RequestBody @Valid CreateCurrencyCommand command) {
        log.info("Creating currency: {}", command);
        return ResponseEntity.status(HttpStatus.CREATED).body(currencyService.createCurrency(command));
    }

    @PutMapping("/{code}")
    public ResponseEntity<CurrencyDto> updateCurrencyData(@PathVariable String code, @RequestBody @Valid UpdateCurrencyCommand command) {
        log.info("Updating currency with code: {}", code);
        return ResponseEntity.ok(currencyService.updateCurrencyData(code, command));
    }

    @DeleteMapping("/{code}")
    public ResponseEntity<Void> deleteCurrency(@PathVariable String code) {
        log.info("Deleting currency with code: {}", code);
        currencyService.deleteCurrency(code);
        return ResponseEntity.noContent().build();
    }
}
