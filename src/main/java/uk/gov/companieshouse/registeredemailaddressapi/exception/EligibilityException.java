package uk.gov.companieshouse.registeredemailaddressapi.exception;

import uk.gov.companieshouse.registeredemailaddressapi.eligibility.EligibilityStatusCode;

public class EligibilityException extends Exception {
    private final EligibilityStatusCode eligibilityStatusCode;

    public EligibilityException(EligibilityStatusCode eligibilityStatusCode, String message) {
        super(message);
        this.eligibilityStatusCode = eligibilityStatusCode;
    }

    public EligibilityStatusCode getEligibilityStatusCode() {
        return eligibilityStatusCode;
    }
}
