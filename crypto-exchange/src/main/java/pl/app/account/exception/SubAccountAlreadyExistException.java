package pl.app.account.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.CONFLICT, reason = "SubAccount already exist")
public class SubAccountAlreadyExistException extends RuntimeException {
}
