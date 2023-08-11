package uk.gov.companieshouse.registeredemailaddressapi.exception;

public class SubmissionAlreadyExistsException extends Exception {
    public SubmissionAlreadyExistsException(String message) {
        super(message);
    }
    public SubmissionAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
