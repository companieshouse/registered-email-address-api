package uk.gov.companieshouse.registeredemailaddressapi.unit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.model.company.RegisteredEmailAddressJson;
import uk.gov.companieshouse.registeredemailaddressapi.eligibility.EligibilityRule;
import uk.gov.companieshouse.registeredemailaddressapi.eligibility.EligibilityStatusCode;
import uk.gov.companieshouse.registeredemailaddressapi.eligibility.impl.CompanyEmailValidation;
import uk.gov.companieshouse.registeredemailaddressapi.eligibility.impl.CompanyStatusValidation;
import uk.gov.companieshouse.registeredemailaddressapi.eligibility.impl.CompanyTypeValidation;
import uk.gov.companieshouse.registeredemailaddressapi.exception.EligibilityException;
import uk.gov.companieshouse.registeredemailaddressapi.exception.ServiceException;
import uk.gov.companieshouse.registeredemailaddressapi.service.CompanyProfileService;
import uk.gov.companieshouse.registeredemailaddressapi.service.EligibilityService;
import uk.gov.companieshouse.registeredemailaddressapi.service.PrivateDataRetrievalService;

@ExtendWith(MockitoExtension.class)
class EligibilityServiceTest {


    private static final String COMPANY_NUMBER = "12345";

    private List<EligibilityRule<CompanyProfileApi>> eligibilityRules;

    @Mock
    private CompanyProfileService companyProfileService;

    @Mock
    private PrivateDataRetrievalService privateDataRetrievalService;

    private EligibilityService eligibilityService;

    @BeforeEach
    void init() {
        CompanyTypeValidation companyTypeValidation = new CompanyTypeValidation(Set.of("ltd"));
        CompanyStatusValidation companyStatusValidation = new CompanyStatusValidation(Set.of("active"));
        CompanyEmailValidation companyEmailValidation = new CompanyEmailValidation(privateDataRetrievalService);

        eligibilityRules = List.of(companyTypeValidation, companyStatusValidation, companyEmailValidation);

        eligibilityService = new EligibilityService(eligibilityRules, companyProfileService);
    }

    @Test
    void testWithNoErrors() throws ServiceException {
        // GIVEN
        CompanyProfileApi companyProfileApi = new CompanyProfileApi();
        companyProfileApi.setCompanyNumber(COMPANY_NUMBER);
        companyProfileApi.setCompanyStatus("active");
        companyProfileApi.setType("ltd");

        RegisteredEmailAddressJson registeredEmailAddressJson = new RegisteredEmailAddressJson();
        registeredEmailAddressJson.setRegisteredEmailAddress("info@acme.com");

        BDDMockito.given(privateDataRetrievalService.getRegisteredEmailAddress(COMPANY_NUMBER)).willReturn(registeredEmailAddressJson);

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
        companyProfileApi.setCompanyNumber(COMPANY_NUMBER);
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
        companyProfileApi.setCompanyNumber(COMPANY_NUMBER);
        companyProfileApi.setCompanyStatus("active");
        companyProfileApi.setType("plc");

        // WHEN
        var responseBody = eligibilityService.checkCompanyEligibility(companyProfileApi);

        // THEN
        assertNotNull(responseBody);
        assertEquals(EligibilityStatusCode.INVALID_COMPANY_TYPE, responseBody.getEligibilityStatusCode());
    }

    @Test
    void testInvalidNoRegisteredEmailAddressExistsNull() throws EligibilityException, ServiceException {
        // GIVEN
        CompanyProfileApi companyProfileApi = new CompanyProfileApi();
        companyProfileApi.setCompanyNumber(COMPANY_NUMBER);
        companyProfileApi.setCompanyStatus("active");
        companyProfileApi.setType("ltd");

        BDDMockito.given(privateDataRetrievalService.getRegisteredEmailAddress(COMPANY_NUMBER)).willReturn(null);

        // WHEN
        var responseBody = eligibilityService.checkCompanyEligibility(companyProfileApi);

        // THEN
        assertNotNull(responseBody);
        assertEquals(EligibilityStatusCode.INVALID_NO_REGISTERED_EMAIL_ADDRESS_EXISTS, responseBody.getEligibilityStatusCode());
    }

    @Test
    void testInvalidNoRegisteredEmailAddressExistsEmpty() throws EligibilityException, ServiceException {
        // GIVEN
        CompanyProfileApi companyProfileApi = new CompanyProfileApi();
        companyProfileApi.setCompanyNumber(COMPANY_NUMBER);
        companyProfileApi.setCompanyStatus("active");
        companyProfileApi.setType("ltd");

        RegisteredEmailAddressJson registeredEmailAddressJson = new RegisteredEmailAddressJson();
        registeredEmailAddressJson.setRegisteredEmailAddress(null);

        BDDMockito.given(privateDataRetrievalService.getRegisteredEmailAddress(COMPANY_NUMBER)).willReturn(registeredEmailAddressJson);

        // WHEN
        var responseBody = eligibilityService.checkCompanyEligibility(companyProfileApi);

        // THEN
        assertNotNull(responseBody);
        assertEquals(EligibilityStatusCode.INVALID_NO_REGISTERED_EMAIL_ADDRESS_EXISTS, responseBody.getEligibilityStatusCode());
    }

    @Test
    void testInvalidNoRegisteredEmailAddressExistsBlank() throws EligibilityException, ServiceException {
        // GIVEN
        CompanyProfileApi companyProfileApi = new CompanyProfileApi();
        companyProfileApi.setCompanyNumber(COMPANY_NUMBER);
        companyProfileApi.setCompanyStatus("active");
        companyProfileApi.setType("ltd");

        RegisteredEmailAddressJson registeredEmailAddressJson = new RegisteredEmailAddressJson();
        registeredEmailAddressJson.setRegisteredEmailAddress("");

        BDDMockito.given(privateDataRetrievalService.getRegisteredEmailAddress(COMPANY_NUMBER)).willReturn(registeredEmailAddressJson);

        // WHEN
        var responseBody = eligibilityService.checkCompanyEligibility(companyProfileApi);

        // THEN
        assertNotNull(responseBody);
        assertEquals(EligibilityStatusCode.INVALID_NO_REGISTERED_EMAIL_ADDRESS_EXISTS, responseBody.getEligibilityStatusCode());
    }

}
