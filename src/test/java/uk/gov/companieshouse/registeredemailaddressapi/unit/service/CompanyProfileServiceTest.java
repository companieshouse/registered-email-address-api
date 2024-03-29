package uk.gov.companieshouse.registeredemailaddressapi.unit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpResponseException;

import uk.gov.companieshouse.api.ApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.company.CompanyResourceHandler;
import uk.gov.companieshouse.api.handler.company.request.CompanyGet;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.registeredemailaddressapi.client.ApiClientService;
import uk.gov.companieshouse.registeredemailaddressapi.exception.CompanyNotFoundException;
import uk.gov.companieshouse.registeredemailaddressapi.exception.ServiceException;
import uk.gov.companieshouse.registeredemailaddressapi.service.CompanyProfileService;

@ExtendWith(MockitoExtension.class)
class CompanyProfileServiceTest {

    private static final String COMPANY_NUMBER = "12345678";
    @Mock
    private ApiClientService apiClientService;

    @Mock
    private ApiClient apiClient;

    @Mock
    private CompanyResourceHandler companyResourceHandler;

    @Mock
    private CompanyGet companyGet;

    @Mock
    private ApiResponse<CompanyProfileApi> apiResponse;

    @InjectMocks
    private CompanyProfileService companyProfileService;

    @Test
    void getCompanyProfile() throws ServiceException, ApiErrorResponseException, URIValidationException, CompanyNotFoundException {
        CompanyProfileApi companyProfile = new CompanyProfileApi();
        companyProfile.setCompanyName("COMPANY NAME");

        when(apiClientService.getApiKeyAuthenticatedClient()).thenReturn(apiClient);
        when(apiClient.company()).thenReturn(companyResourceHandler);
        when(companyResourceHandler.get("/company/" + COMPANY_NUMBER)).thenReturn(companyGet);
        when(companyGet.execute()).thenReturn(apiResponse);
        when(apiResponse.getData()).thenReturn(companyProfile);

        var response = companyProfileService.getCompanyProfile(COMPANY_NUMBER);

        assertEquals(companyProfile, response);
    }

    @Test
    void getCompanyProfileURIValidationException() throws ApiErrorResponseException, URIValidationException {
        when(apiClientService.getApiKeyAuthenticatedClient()).thenReturn(apiClient);
        when(apiClient.company()).thenReturn(companyResourceHandler);
        when(companyResourceHandler.get("/company/" + COMPANY_NUMBER)).thenReturn(companyGet);
        when(companyGet.execute()).thenThrow(new URIValidationException("ERROR"));

        ServiceException se = assertThrows(ServiceException.class, () -> companyProfileService.getCompanyProfile(COMPANY_NUMBER));
        assertTrue(se.getMessage().contains(COMPANY_NUMBER));
    }

    @Test
    void getCompanyProfileApiErrorResponse() throws ApiErrorResponseException, URIValidationException {
        when(apiClientService.getApiKeyAuthenticatedClient()).thenReturn(apiClient);
        when(apiClient.company()).thenReturn(companyResourceHandler);
        when(companyResourceHandler.get("/company/" + COMPANY_NUMBER)).thenReturn(companyGet);
        when(companyGet.execute()).thenThrow(ApiErrorResponseException.fromIOException(new IOException("ERROR")));

        ServiceException se = assertThrows(ServiceException.class, () -> companyProfileService.getCompanyProfile(COMPANY_NUMBER));
        assertTrue(se.getMessage().contains(COMPANY_NUMBER));
        assertTrue(se.getMessage().contains("500"));
    }

    @Test
    void getCompanyProfileApiCompanyNotFoundResponse() throws ApiErrorResponseException, URIValidationException {
        when(apiClientService.getApiKeyAuthenticatedClient()).thenReturn(apiClient);
        when(apiClient.company()).thenReturn(companyResourceHandler);
        when(companyResourceHandler.get("/company/" + COMPANY_NUMBER)).thenReturn(companyGet);

        HttpResponseException httpResponseException =
                new HttpResponseException.Builder(404, "test", new HttpHeaders()).build();
        when(companyGet.execute()).thenThrow(ApiErrorResponseException.fromHttpResponseException(httpResponseException));

        assertThrows(CompanyNotFoundException.class, () -> companyProfileService.getCompanyProfile(COMPANY_NUMBER));
    }
}
