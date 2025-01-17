package pl.app.currency.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.app.currency.exception.CurrencyAlreadyExistException;
import pl.app.currency.exception.CurrencyConflictException;
import pl.app.currency.exception.CurrencyNotFoundException;
import pl.app.currency.model.Currency;
import pl.app.currency.model.command.CreateCurrencyCommand;
import pl.app.currency.model.command.UpdateCurrencyCommand;
import pl.app.currency.model.dto.CurrencyDto;
import pl.app.currency.repository.CurrencyRepository;

@Service
@RequiredArgsConstructor
public class CurrencyService {

    private final CurrencyRepository currencyRepository;

    @Transactional(readOnly = true)
    public Page<CurrencyDto> getCurrencies(Pageable pageable) {
        return currencyRepository.findAll(pageable)
                .map(CurrencyDto::fromCurrency);
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "currencies", key = "#code")
    public CurrencyDto getCurrencyDto(String code) {
        return currencyRepository.findByCode(code)
                .map(CurrencyDto::fromCurrency)
                .orElseThrow(CurrencyNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public Currency getCurrency(String code) {
        return currencyRepository.findByCode(code)
                .orElseThrow(CurrencyNotFoundException::new);
    }

    @Transactional
    public CurrencyDto createCurrency(CreateCurrencyCommand command) {
        try {
            return CurrencyDto.fromCurrency(currencyRepository.save(new Currency(command.name(), command.code())));
        } catch (Exception e) {
            throw new CurrencyAlreadyExistException();
        }
    }

    @Transactional
    @CacheEvict(cacheNames = "currencies", key = "#code")
    public CurrencyDto updateCurrencyData(String code, UpdateCurrencyCommand command) {
        Currency currency = getCurrency(code);

        if (!currency.getName().equals(command.previousName()) || !currency.getCode().equals(command.previousCode())) {
            throw new CurrencyConflictException();
        }

        currency.setName(command.name());
        currency.setCode(command.code());

        return CurrencyDto.fromCurrency(currency);
    }

    @Transactional
    @CacheEvict(cacheNames = "currencies", key = "#code")
    public void deleteCurrency(String code) {
        if (currencyRepository.deleteByCode(code) < 1) {
            throw new CurrencyNotFoundException();
        }
    }
}
