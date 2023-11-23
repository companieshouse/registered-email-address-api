package uk.gov.companieshouse.registeredemailaddressapi.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import uk.gov.companieshouse.registeredemailaddressapi.eligibility.EligibilityStatusCode;

public class CompanyValidationResponse {

    @JsonProperty("eligibility_status_code")
    private EligibilityStatusCode eligibilityStatusCode;

    public CompanyValidationResponse() {
    }

    public CompanyValidationResponse(EligibilityStatusCode eligibilityStatusCode) {
        this.eligibilityStatusCode = eligibilityStatusCode;
    }

    public EligibilityStatusCode getEligibilityStatusCode() {
        return eligibilityStatusCode;
    }

    public void setEligibilityStatusCode(EligibilityStatusCode eligibilityStatusCode) {
        this.eligibilityStatusCode = eligibilityStatusCode;
    }
}
