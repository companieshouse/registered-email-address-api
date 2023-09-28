package uk.gov.companieshouse.registeredemailaddressapi.eligibility.impl;

import java.util.Set;

import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.registeredemailaddressapi.eligibility.EligibilityRule;
import uk.gov.companieshouse.registeredemailaddressapi.eligibility.EligibilityStatusCode;
import uk.gov.companieshouse.registeredemailaddressapi.exception.EligibilityException;
import uk.gov.companieshouse.registeredemailaddressapi.utils.ApiLogger;

public class CompanyStatusValidation implements EligibilityRule<CompanyProfileApi> {

    private final Set<String> allowedStatuses;

    public CompanyStatusValidation(Set<String> allowedStatuses) {
        this.allowedStatuses = allowedStatuses;
    }

    @Override
    public void validate(CompanyProfileApi profileToValidate) throws EligibilityException {
        ApiLogger.info(String.format("Validating Company Status for: %s", profileToValidate.getCompanyNumber()));
        var status = profileToValidate.getCompanyStatus();

        if (!allowedStatuses.contains(status)) {
            ApiLogger.info(String.format("Company Status validation failed for: %s", profileToValidate.getCompanyNumber()));
            throw new EligibilityException(EligibilityStatusCode.INVALID_COMPANY_STATUS);
        }
        ApiLogger.info(String.format("Company Status validation passed for: %s", profileToValidate.getCompanyNumber()));
    }
}
