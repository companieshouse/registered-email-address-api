package uk.gov.companieshouse.registeredemailaddressapi.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpResponseException;
import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.company.request.PrivateCompanyEmailGet;
import uk.gov.companieshouse.api.handler.company.PrivateCompanyResourceHandler;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.company.RegisteredEmailAddressJson;
import uk.gov.companieshouse.registeredemailaddressapi.client.ApiClientService;
import uk.gov.companieshouse.registeredemailaddressapi.exception.ServiceException;
import uk.gov.companieshouse.registeredemailaddressapi.service.OracleQueryApiDataRetrievalServiceImpl;

@ExtendWith(MockitoExtension.class)
class OracleQueryApiDataRetrievalServiceImplTest {

    private static final String COMPANY_NUMBER = "12345678";
    private static final String COMPANY_EMAIL = "tester@testing.com";

    @InjectMocks
    private OracleQueryApiDataRetrievalServiceImpl oracleQueryApiDataRetrievalServiceImpl;

    @Mock
    private ApiClientService apiClientService;

    @Mock
    private InternalApiClient apiClient;

    @Mock
    private ApiResponse<RegisteredEmailAddressJson> apiPrivateCompanyEmailGetResponse;

    @Mock
    private PrivateCompanyResourceHandler privateCompanyResourceHandler;

    @Mock
    private PrivateCompanyEmailGet privateCompanyEmailGet;


    private static final ApiErrorResponseException NOT_FOUND_EXCEPTION = ApiErrorResponseException.fromHttpResponseException(
            new HttpResponseException.Builder(404, "ERROR", new HttpHeaders()).build());


    @Nested
    class GetRegisteredEmailAddressTests {

        @BeforeEach
        void init()  {
            when(apiClientService.getInternalApiClient()).thenReturn(apiClient);
            // Private Get Company Email Data Mocks
            when(apiClient.privateCompanyResourceHandler()).thenReturn(
                    privateCompanyResourceHandler);
            when(privateCompanyResourceHandler.getCompanyRegisteredEmailAddress(
                    Mockito.anyString())).thenReturn(privateCompanyEmailGet);
        }


        @Test
        void testGetRegisteredEmailAddressIsSuccessful()
                throws ServiceException, ApiErrorResponseException, URIValidationException {
            RegisteredEmailAddressJson registeredEmailAddressJson = buildRegisteredEmailAddressJson(COMPANY_EMAIL);
            when(privateCompanyEmailGet.execute()).thenReturn(apiPrivateCompanyEmailGetResponse);
            when(apiPrivateCompanyEmailGetResponse.getData()).thenReturn(registeredEmailAddressJson);

            RegisteredEmailAddressJson response = oracleQueryApiDataRetrievalServiceImpl.getRegisteredEmailAddress(COMPANY_NUMBER);
            assertEquals(COMPANY_EMAIL, response.getRegisteredEmailAddress());
        }

        @Test
        void testNullResponseWhenGetRegisteredEmailAddressReturnsNullEmailAddress()
                throws ServiceException, ApiErrorResponseException, URIValidationException {
            RegisteredEmailAddressJson registeredEmailAddressJson = buildRegisteredEmailAddressJson(null);
            when(privateCompanyEmailGet.execute()).thenReturn(apiPrivateCompanyEmailGetResponse);
            when(apiPrivateCompanyEmailGetResponse.getData()).thenReturn(registeredEmailAddressJson);

            RegisteredEmailAddressJson response = oracleQueryApiDataRetrievalServiceImpl.getRegisteredEmailAddress(COMPANY_NUMBER);
            assertNull(response.getRegisteredEmailAddress());
        }

        @Test
        void testNullResponseWhenGetRegisteredEmailAddressReturnsNullResponse()
                throws ServiceException, ApiErrorResponseException, URIValidationException {
            when(privateCompanyEmailGet.execute()).thenReturn(apiPrivateCompanyEmailGetResponse);
            when(apiPrivateCompanyEmailGetResponse.getData()).thenReturn(null);

            RegisteredEmailAddressJson response = oracleQueryApiDataRetrievalServiceImpl.getRegisteredEmailAddress(COMPANY_NUMBER);
            assertNull(response);
        }

        @Test
        void testNullResponseWhenCompanyEmailGetThrowsNotFoundApiResponseException()
                throws IOException, URIValidationException, ServiceException {

            when(privateCompanyEmailGet.execute()).thenThrow(NOT_FOUND_EXCEPTION);

            RegisteredEmailAddressJson response = oracleQueryApiDataRetrievalServiceImpl.getRegisteredEmailAddress(COMPANY_NUMBER);
            assertNull(response);
        }

        @Test
        void testServiceExceptionThrownWhenCompanyEmailGetThrowsURIValidationException()
                throws IOException, URIValidationException {
            when(privateCompanyEmailGet.execute()).thenThrow(new URIValidationException("ERROR"));

            assertThrows(
                    ServiceException.class,
                    () -> oracleQueryApiDataRetrievalServiceImpl.getRegisteredEmailAddress((COMPANY_NUMBER)));
        }

        @Test
        void testServiceExceptionThrownWhenGetBeneficialOwnerPrivateDataThrowsIOException()
                throws IOException, URIValidationException {

            when(privateCompanyEmailGet.execute())
                    .thenThrow(ApiErrorResponseException.fromIOException(new IOException("ERROR")));

            assertThrows(
                    ServiceException.class,
                    () -> oracleQueryApiDataRetrievalServiceImpl.getRegisteredEmailAddress(COMPANY_NUMBER));
        }

    }

    private RegisteredEmailAddressJson buildRegisteredEmailAddressJson(String companyEmail) {
        RegisteredEmailAddressJson response = new RegisteredEmailAddressJson();
        response.setRegisteredEmailAddress(companyEmail);
        return response;
    }

}