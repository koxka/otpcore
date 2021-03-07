package hu.otp.ticketing.core.common.exception;

public class MissingHeaderException extends RuntimeException {

    public MissingHeaderException(String message) {
        super(message);
    }
}
