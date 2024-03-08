package uk.gov.companieshouse.registeredemailaddressapi.exception;

public class InvalidEmailAddressException extends Exception {
    public InvalidEmailAddressException(String message) {
        super(message);
    }
    public InvalidEmailAddressException(String message, Throwable cause) {
        super(message, cause);
    }
}
