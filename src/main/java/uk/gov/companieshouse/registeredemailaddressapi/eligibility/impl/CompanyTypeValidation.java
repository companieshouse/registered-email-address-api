package uk.gov.companieshouse.registeredemailaddressapi.eligibility.impl;

import java.util.HashMap;
import java.util.Set;

import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.registeredemailaddressapi.eligibility.EligibilityRule;
import uk.gov.companieshouse.registeredemailaddressapi.eligibility.EligibilityStatusCode;
import uk.gov.companieshouse.registeredemailaddressapi.exception.EligibilityException;
import uk.gov.companieshouse.registeredemailaddressapi.utils.ApiLogger;

public class CompanyTypeValidation implements EligibilityRule<CompanyProfileApi> {

    private final Set<String> companyTypes;

    public CompanyTypeValidation(Set<String> companyTypes) {
        this.companyTypes = companyTypes;
    }

    @Override
    public void validate(CompanyProfileApi profileToValidate) throws EligibilityException {
        var logMap = new HashMap<String, Object>();
        logMap.put("companyProfile", profileToValidate);
        ApiLogger.info(String.format("Validating Company Type Should Use for: %s", profileToValidate.getCompanyNumber()), logMap);

        if (!companyTypes.contains(profileToValidate.getType())) {
            ApiLogger.info(String.format("Company Type validation Should Use failed for: %s", profileToValidate.getCompanyNumber()), logMap);
            throw new EligibilityException(EligibilityStatusCode.INVALID_COMPANY_TYPE);
        }

        ApiLogger.info(String.format("Company Type validation Should Use passed for: %s", profileToValidate.getCompanyNumber()), logMap);
    }
}
