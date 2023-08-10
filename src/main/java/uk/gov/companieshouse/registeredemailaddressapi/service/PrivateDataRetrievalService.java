package uk.gov.companieshouse.registeredemailaddressapi.service;

import java.util.HashMap;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.company.RegisteredEmailAddressJson;
import uk.gov.companieshouse.registeredemailaddressapi.client.ApiClientService;
import uk.gov.companieshouse.registeredemailaddressapi.exception.ServiceException;
import uk.gov.companieshouse.registeredemailaddressapi.utils.ApiLogger;

@Service
public class PrivateDataRetrievalService {

    private static final String COMPANY_NUMBER = "company_number";
    private static final String REGISTERED_EMAIL_ADDRESS_URI_SUFFIX = "/company/%s/registered-email-address";

    @Autowired
    private ApiClientService apiClientService;

    @Value("${ORACLE_QUERY_API_URL}")
    private String oracleQueryApiUrl;

    public RegisteredEmailAddressJson getRegisteredEmailAddress(String companyNumber)
            throws ServiceException {

        var logMap = new HashMap<String, Object>();
        try {
            logMap.put(COMPANY_NUMBER, companyNumber);
            ApiLogger.info("Retrieving Registered Email Address for Company Number ", logMap);
            ApiLogger.debug("oracleQueryApiUrl : " +  oracleQueryApiUrl, logMap);

            var internalApiClient = apiClientService.getInternalApiClient();
            internalApiClient.setBasePath(oracleQueryApiUrl);
            var registeredEmailAddressJson = internalApiClient
                    .privateCompanyResourceHandler()
                    .getCompanyRegisteredEmailAddress(String.format(REGISTERED_EMAIL_ADDRESS_URI_SUFFIX, companyNumber))
                    .execute()
                    .getData();

            ApiLogger.info("Successfully retrieved Registered Email Address from database", logMap);
            return registeredEmailAddressJson;

        } catch (ApiErrorResponseException e) {
            ApiLogger.error("Error Retrieving Registered Email Address for Company ", e, logMap);

            if (e.getStatusCode() == HttpServletResponse.SC_NOT_FOUND) {
                ApiLogger.info("No Registered EmailAddress found for Company "+companyNumber, logMap);
                return null;
            }
            throw new ServiceException(e.getStatusMessage(), e);
        } catch (URIValidationException e) {
            var message = "Error Retrieving Registered Email Address for Company " + companyNumber;
            ApiLogger.errorContext(message, e);
            throw new ServiceException(e.getMessage(), e);
        }
    }
}