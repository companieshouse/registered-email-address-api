package uk.gov.companieshouse.registeredemailaddressapi.unit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doThrow;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.registeredemailaddressapi.eligibility.EligibilityRule;
import uk.gov.companieshouse.registeredemailaddressapi.eligibility.EligibilityStatusCode;
import uk.gov.companieshouse.registeredemailaddressapi.exception.EligibilityException;
import uk.gov.companieshouse.registeredemailaddressapi.exception.ServiceException;
import uk.gov.companieshouse.registeredemailaddressapi.model.response.CompanyValidationResponse;
import uk.gov.companieshouse.registeredemailaddressapi.service.EligibilityService;

@ExtendWith(MockitoExtension.class)
class EligibilityServiceTest {


    private List<EligibilityRule<CompanyProfileApi>> eligibilityRules;

    @Mock EligibilityRule<CompanyProfileApi> eligibilityRule;

    private EligibilityService eligibilityService;

    @BeforeEach
    void init() {
        eligibilityRules = new ArrayList<>();
        eligibilityRules.add(eligibilityRule);
        eligibilityService = new EligibilityService(eligibilityRules);
    }

    @Test
    void tesWithNoErrors() throws ServiceException {
        CompanyProfileApi companyProfileApi = new CompanyProfileApi();
        companyProfileApi.setCompanyStatus("AcceptValue");
        var responseBody = eligibilityService.checkCompanyEligibility(companyProfileApi);
        assertNotNull(responseBody);
        assertEquals(EligibilityStatusCode.COMPANY_VALID_FOR_SERVICE, responseBody.getEligibilityStatusCode());
    }

    @Test
    void testInvalidCompanyStatus() throws EligibilityException, ServiceException {
        var responseBody = getValidationErrorResponse(EligibilityStatusCode.INVALID_COMPANY_STATUS);
        assertNotNull(responseBody);
        assertEquals(EligibilityStatusCode.INVALID_COMPANY_STATUS, responseBody.getEligibilityStatusCode());
    }

    @Test
    void testInvalidCompanyType() throws EligibilityException, ServiceException {
        var responseBody = getValidationErrorResponse(EligibilityStatusCode.INVALID_COMPANY_TYPE);
        assertNotNull(responseBody);
        assertEquals(EligibilityStatusCode.INVALID_COMPANY_TYPE, responseBody.getEligibilityStatusCode());

    }

    private CompanyValidationResponse getValidationErrorResponse(EligibilityStatusCode reason) throws EligibilityException, ServiceException {
        CompanyProfileApi companyProfileApi = new CompanyProfileApi();
        doThrow(new EligibilityException(reason)).when(eligibilityRule).validate(companyProfileApi);
        return eligibilityService.checkCompanyEligibility(companyProfileApi);
    }
}
