package uk.gov.companieshouse.registeredemailaddressapi.exception;

public class NoExistingEmailAddressException extends Exception {
    public NoExistingEmailAddressException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoExistingEmailAddressException(String message) {
        super(message);
    }
}
