package hu.otp.ticketing.core.endpoint;

import hu.otp.ticketing.core.common.exception.MissingHeaderException;
import hu.otp.ticketing.core.common.exception.UnauthorizedException;
import hu.otp.ticketing.core.endpoint.rest.model.UnauthorizedResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice(basePackageClasses = {CoreEndpoint.class})
public class CoreControllerAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<UnauthorizedResponse> handleUnauthorizedException(UnauthorizedException e) {
        UnauthorizedResponse rp = new UnauthorizedResponse();
        rp.setReason(e.getReason());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(rp);
    }

    @ExceptionHandler(MissingHeaderException.class)
    public ResponseEntity<String> handleUnauthorizedException(MissingHeaderException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
}
