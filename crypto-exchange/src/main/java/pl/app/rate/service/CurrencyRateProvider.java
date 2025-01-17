package pl.app.rate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pl.app.account.model.enums.CurrencyCode;
import pl.app.rate.exception.CurrencyConversionException;

import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CurrencyRateProvider {

    private static final String API_URL = "https://min-api.cryptocompare.com/data/price?fsym={from}&tsyms={to}";

    private final RestTemplate restTemplate;

    public BigDecimal getExchangeRate(CurrencyCode fromCurrency, CurrencyCode toCurrency) {
        Map<String, Object> response = restTemplate.getForObject(
                API_URL,
                Map.class,
                fromCurrency,
                toCurrency
        );

        if (response != null && response.containsKey(toCurrency.toString())) {
            return new BigDecimal(response.get(toCurrency.toString()).toString());
        } else {
            throw new CurrencyConversionException();
        }
    }
}
