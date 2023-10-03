package uk.gov.companieshouse.registeredemailaddressapi.exception;

public class CompanyNotFoundException extends Exception{
    public CompanyNotFoundException(String message) {
        super(message);
    }
    public CompanyNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
