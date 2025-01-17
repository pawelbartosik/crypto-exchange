package pl.app.currency.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.CONFLICT, reason = "Currency already exist")
public class CurrencyAlreadyExistException extends RuntimeException {
}
