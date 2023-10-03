package uk.gov.companieshouse.registeredemailaddressapi.unit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.registeredemailaddressapi.eligibility.EligibilityRule;
import uk.gov.companieshouse.registeredemailaddressapi.eligibility.EligibilityStatusCode;
import uk.gov.companieshouse.registeredemailaddressapi.eligibility.impl.CompanyStatusValidation;
import uk.gov.companieshouse.registeredemailaddressapi.eligibility.impl.CompanyTypeValidation;
import uk.gov.companieshouse.registeredemailaddressapi.exception.CompanyNotFoundException;
import uk.gov.companieshouse.registeredemailaddressapi.exception.EligibilityException;
import uk.gov.companieshouse.registeredemailaddressapi.exception.ServiceException;
import uk.gov.companieshouse.registeredemailaddressapi.service.CompanyProfileService;
import uk.gov.companieshouse.registeredemailaddressapi.service.EligibilityService;

@ExtendWith(MockitoExtension.class)
class EligibilityServiceTest {


    private List<EligibilityRule<CompanyProfileApi>> eligibilityRules;

    @Mock
    private CompanyProfileService companyProfileService;

    private EligibilityService eligibilityService;

    @BeforeEach
    void init() {
        CompanyTypeValidation companyTypeValidation = new CompanyTypeValidation(Set.of("ltd"));
        CompanyStatusValidation companyStatusValidation = new CompanyStatusValidation(Set.of("active"));

        eligibilityRules = List.of(companyTypeValidation, companyStatusValidation);

        eligibilityService = new EligibilityService(eligibilityRules, companyProfileService);
    }

    @Test
    void testWithNoErrors() throws ServiceException {
        // GIVEN
        CompanyProfileApi companyProfileApi = new CompanyProfileApi();
        companyProfileApi.setCompanyStatus("active");
        companyProfileApi.setType("ltd");

        // WHEN
        var responseBody = eligibilityService.checkCompanyEligibility(companyProfileApi);

        // THEN
        assertNotNull(responseBody);
        assertEquals(EligibilityStatusCode.COMPANY_VALID_FOR_SERVICE, responseBody.getEligibilityStatusCode());
    }

    @Test
    void testInvalidCompanyStatus() throws EligibilityException, ServiceException {
        // GIVEN
        CompanyProfileApi companyProfileApi = new CompanyProfileApi();
        companyProfileApi.setCompanyStatus("inactive");
        companyProfileApi.setType("ltd");

        // WHEN
        var responseBody = eligibilityService.checkCompanyEligibility(companyProfileApi);

        // THEN
        assertNotNull(responseBody);
        assertEquals(EligibilityStatusCode.INVALID_COMPANY_STATUS, responseBody.getEligibilityStatusCode());
    }

    @Test
    void testInvalidCompanyType() throws EligibilityException, ServiceException {
        // GIVEN
        CompanyProfileApi companyProfileApi = new CompanyProfileApi();
        companyProfileApi.setCompanyStatus("active");
        companyProfileApi.setType("plc");

        // WHEN
        var responseBody = eligibilityService.checkCompanyEligibility(companyProfileApi);

        // THEN
        assertNotNull(responseBody);
        assertEquals(EligibilityStatusCode.INVALID_COMPANY_TYPE, responseBody.getEligibilityStatusCode());
    }

    @Test
    void withNoErrors() throws ServiceException, CompanyNotFoundException {
        // GIVEN

        CompanyProfileApi companyProfileApi = new CompanyProfileApi();
        companyProfileApi.setCompanyStatus("active");
        companyProfileApi.setType("ltd");

        var companyNumber = "123";

        // WHEN

        when(companyProfileService.getCompanyProfile(companyNumber)).thenReturn(companyProfileApi);

        var eligibilityStatusCode = eligibilityService.checkCompanyEligibility(companyNumber);

        // THEN

        assertEquals(EligibilityStatusCode.COMPANY_VALID_FOR_SERVICE, eligibilityStatusCode);
    }
}
