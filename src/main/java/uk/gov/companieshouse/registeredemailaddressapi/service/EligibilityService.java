package uk.gov.companieshouse.registeredemailaddressapi.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.registeredemailaddressapi.eligibility.EligibilityRule;
import uk.gov.companieshouse.registeredemailaddressapi.eligibility.EligibilityStatusCode;
import uk.gov.companieshouse.registeredemailaddressapi.exception.CompanyNotFoundException;
import uk.gov.companieshouse.registeredemailaddressapi.exception.EligibilityException;
import uk.gov.companieshouse.registeredemailaddressapi.exception.ServiceException;
import uk.gov.companieshouse.registeredemailaddressapi.model.response.CompanyValidationResponse;
import uk.gov.companieshouse.registeredemailaddressapi.utils.ApiLogger;

@Service
public class EligibilityService {

    private final List<EligibilityRule<CompanyProfileApi>> eligibilityRules;

    private final CompanyProfileService companyProfileService;

    @Autowired
    public EligibilityService(@Qualifier("rea-update-eligibility-rules") List<EligibilityRule<CompanyProfileApi>> eligibilityRules, CompanyProfileService companyProfileService){
        this.eligibilityRules = eligibilityRules;
        this.companyProfileService = companyProfileService;
    }

    public boolean checkCompanyEligibility(String companyNumber) throws ServiceException, CompanyNotFoundException, EligibilityException {
        var companyProfile = companyProfileService.getCompanyProfile(companyNumber);

        for (EligibilityRule<CompanyProfileApi> eligibilityRule : eligibilityRules) {
            eligibilityRule.validate(companyProfile);
        }

        return true;
    }

    public CompanyValidationResponse checkCompanyEligibility(CompanyProfileApi companyProfile) throws ServiceException {
        var response = new CompanyValidationResponse();
        try {
            for (EligibilityRule<CompanyProfileApi> eligibilityRule : eligibilityRules) {
                eligibilityRule.validate(companyProfile);
            }
        } catch (EligibilityException e) {
            ApiLogger.info(String.format("Company %s ineligible to use the service because %s",  companyProfile.getCompanyNumber(), e.getEligibilityStatusCode()));
            response.setEligibilityStatusCode(e.getEligibilityStatusCode());
            return response;
        }
        response.setEligibilityStatusCode(EligibilityStatusCode.COMPANY_VALID_FOR_SERVICE);
        return response;
    }
}
