package pl.app.account.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(reason = "Account is not empty", code = HttpStatus.CONFLICT)
public class AccountNotEmptyException extends RuntimeException {
}
