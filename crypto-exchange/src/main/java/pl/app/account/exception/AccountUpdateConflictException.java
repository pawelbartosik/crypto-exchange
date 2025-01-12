package pl.app.account.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(reason = "Conflict during account update. Please try again.", code = HttpStatus.CONFLICT)
public class AccountUpdateConflictException extends RuntimeException {
}
