package pl.app.account.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Not enough money for the transaction")
public class NotEnoughMoneyException extends RuntimeException {
}
