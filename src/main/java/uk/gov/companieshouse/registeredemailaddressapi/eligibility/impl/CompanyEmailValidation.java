package uk.gov.companieshouse.registeredemailaddressapi.eligibility.impl;

import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.registeredemailaddressapi.eligibility.EligibilityRule;
import uk.gov.companieshouse.registeredemailaddressapi.eligibility.EligibilityStatusCode;
import uk.gov.companieshouse.registeredemailaddressapi.exception.EligibilityException;
import uk.gov.companieshouse.registeredemailaddressapi.exception.ServiceException;
import uk.gov.companieshouse.registeredemailaddressapi.service.PrivateEmailDataRetrievalService;
import uk.gov.companieshouse.registeredemailaddressapi.utils.ApiLogger;

public class CompanyEmailValidation implements EligibilityRule<CompanyProfileApi> {

    private static void handleValidationFailure(CompanyProfileApi profileToValidate) throws EligibilityException {
        String message = String.format("company number: %s has no existing Registered Email Address", profileToValidate.getCompanyNumber());
        ApiLogger.info(message);
        throw new EligibilityException(EligibilityStatusCode.INVALID_NO_REGISTERED_EMAIL_ADDRESS_EXISTS, message);
    }

    private final PrivateEmailDataRetrievalService privateEmailDataRetrievalService;

    public CompanyEmailValidation(PrivateEmailDataRetrievalService privateEmailDataRetrievalService) {
        this.privateEmailDataRetrievalService = privateEmailDataRetrievalService;
    }

    @Override
    public void validate(CompanyProfileApi profileToValidate) throws EligibilityException, ServiceException {
        ApiLogger.info(String.format("Validating Company Email for: %s", profileToValidate.getCompanyNumber()));

        var registeredEmailAddressJson = privateEmailDataRetrievalService.getRegisteredEmailAddress(profileToValidate.getCompanyNumber());

        if (registeredEmailAddressJson == null) {
            // all because the OracleQueryApi will return 404 for a non-existent email :-(
            handleValidationFailure(profileToValidate);
        }

        var rea = registeredEmailAddressJson.getRegisteredEmailAddress();

        if (rea == null || rea.isBlank()) {
            handleValidationFailure(profileToValidate);
        }

        ApiLogger.info(String.format("Company Email validation passed for: %s", profileToValidate.getCompanyNumber()));
    }
}
