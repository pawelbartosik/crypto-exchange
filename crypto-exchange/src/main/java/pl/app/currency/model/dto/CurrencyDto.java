package pl.app.currency.model.dto;

import pl.app.currency.model.Currency;

public record CurrencyDto(int id, String name, String code) {

    public static CurrencyDto fromCurrency(Currency currency) {
        return new CurrencyDto(currency.getId(), currency.getName(), currency.getCode());
    }
}
