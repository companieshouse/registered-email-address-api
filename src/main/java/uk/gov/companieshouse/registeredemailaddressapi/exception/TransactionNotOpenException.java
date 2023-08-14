package uk.gov.companieshouse.registeredemailaddressapi.exception;

public class TransactionNotOpenException extends Exception {
    public TransactionNotOpenException(String message) {
        super(message);
    }
    public TransactionNotOpenException(String message, Throwable cause) {
        super(message, cause);
    }
}
