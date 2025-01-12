package pl.app.account.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(reason = "Account is currently locked", code = HttpStatus.CONFLICT)
public class AccountLockedException extends RuntimeException {
}
