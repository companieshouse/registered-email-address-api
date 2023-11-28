package uk.gov.companieshouse.registeredemailaddressapi.unit.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.registeredemailaddressapi.controller.EligibilityController;
import uk.gov.companieshouse.registeredemailaddressapi.eligibility.EligibilityStatusCode;
import uk.gov.companieshouse.registeredemailaddressapi.exception.CompanyNotFoundException;
import uk.gov.companieshouse.registeredemailaddressapi.exception.ServiceException;
import uk.gov.companieshouse.registeredemailaddressapi.model.response.CompanyValidationResponse;
import uk.gov.companieshouse.registeredemailaddressapi.service.CompanyProfileService;
import uk.gov.companieshouse.registeredemailaddressapi.service.EligibilityService;

/**
 *
 * Test was originally copied from test of same name in the confirmation-statement-api project.
 * @author paulparlett
 *
 */
@ExtendWith(MockitoExtension.class)
class EligibilityControllerTest {

    private static final String COMPANY_NUMBER = "11111111";
    private static final String ERIC_REQUEST_ID = "XaBcDeF12345";

    @Mock
    private CompanyProfileService companyProfileService;

    @Mock
    private EligibilityService eligibilityService;

    @InjectMocks
    private EligibilityController eligibilityController;

    @Test
    void testSuccessfulGetEligibility() throws ServiceException, CompanyNotFoundException {
        // GIVEN
        CompanyProfileApi companyProfileApi = new CompanyProfileApi();
        companyProfileApi.setCompanyStatus("AcceptValue");

        CompanyValidationResponse companyValidationResponse = new CompanyValidationResponse();
        companyValidationResponse.setEligibilityStatusCode(EligibilityStatusCode.COMPANY_VALID_FOR_SERVICE);

        given(companyProfileService.getCompanyProfile(COMPANY_NUMBER)).willReturn(companyProfileApi);
        given(eligibilityService.checkCompanyEligibility(companyProfileApi)).willReturn(companyValidationResponse);

        // WHEN
        ResponseEntity<CompanyValidationResponse> response = eligibilityController.getEligibility(COMPANY_NUMBER, ERIC_REQUEST_ID);

        // THEN
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(EligibilityStatusCode.COMPANY_VALID_FOR_SERVICE, response.getBody().getEligibilityStatusCode());

    }

    @Test
    void testFailedGetEligibility() throws ServiceException, CompanyNotFoundException {
        // GIVEN
        CompanyProfileApi companyProfileApi = new CompanyProfileApi();
        companyProfileApi.setCompanyStatus("FailureValue");

        CompanyValidationResponse companyValidationResponse = new CompanyValidationResponse();
        companyValidationResponse.setEligibilityStatusCode(EligibilityStatusCode.INVALID_COMPANY_STATUS);

        given(companyProfileService.getCompanyProfile(COMPANY_NUMBER)).willReturn(companyProfileApi);
        given(eligibilityService.checkCompanyEligibility(companyProfileApi)).willReturn(companyValidationResponse);

        // WHEN
        ResponseEntity<CompanyValidationResponse> response = eligibilityController.getEligibility(COMPANY_NUMBER, ERIC_REQUEST_ID);

        // THEN
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(EligibilityStatusCode.INVALID_COMPANY_STATUS, response.getBody().getEligibilityStatusCode());
    }

    @Test
    void testServiceExceptionGetEligibility() throws ServiceException, CompanyNotFoundException {
        // GIVEN
        given(companyProfileService.getCompanyProfile(COMPANY_NUMBER)).willThrow(new ServiceException("", new Exception()));

        // WHEN
        ResponseEntity<CompanyValidationResponse> response = eligibilityController.getEligibility(COMPANY_NUMBER, ERIC_REQUEST_ID);

        // THEN
        assertEquals(500, response.getStatusCodeValue());
    }

    @Test
    void testUncheckedExceptionGetEligibility() throws ServiceException, CompanyNotFoundException {
        // GIVEN
        given(companyProfileService.getCompanyProfile(COMPANY_NUMBER)).willThrow(new RuntimeException("runtime exception"));

        // WHEN
        ResponseEntity<CompanyValidationResponse> response = eligibilityController.getEligibility(COMPANY_NUMBER, ERIC_REQUEST_ID);

        // THEN
        assertEquals(500, response.getStatusCodeValue());
    }

    @Test
    void testCompanyNotFound() throws ServiceException, CompanyNotFoundException {
        // GIVEN
        given(companyProfileService.getCompanyProfile(COMPANY_NUMBER)).willThrow(new CompanyNotFoundException("", new Exception()));

        // WHEN
        ResponseEntity<CompanyValidationResponse> response = eligibilityController.getEligibility(COMPANY_NUMBER, ERIC_REQUEST_ID);

        // THEN
        assertEquals(404, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(EligibilityStatusCode.COMPANY_NOT_FOUND, response.getBody().getEligibilityStatusCode());
    }
}
