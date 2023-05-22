package uk.gov.companieshouse.registeredemailaddressapi.exception;

public class SubmissionNotFoundException extends Exception {

    public SubmissionNotFoundException(String message) {
        super(message);
    }
    public SubmissionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
