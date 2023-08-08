package uk.gov.companieshouse.registeredemailaddressapi.service;

import java.util.HashMap;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.company.RegisteredEmailAddressJson;
import uk.gov.companieshouse.registeredemailaddressapi.client.ApiClientService;
import uk.gov.companieshouse.registeredemailaddressapi.exception.ServiceException;
import uk.gov.companieshouse.registeredemailaddressapi.utils.ApiLogger;

@Component
public class PrivateDataRetrievalService {

    private static final String COMPANY_NUMBER = "company_number";
    private final ApiClientService apiClientService;

    public PrivateDataRetrievalService(ApiClientService apiClientService) {
        this.apiClientService = apiClientService;
    }

    public RegisteredEmailAddressJson getRegisteredEmailAddress(String companyNumber)
            throws ServiceException {

        var logMap = new HashMap<String, Object>();
        logMap.put(COMPANY_NUMBER, companyNumber);
        ApiLogger.info("Retrieving Registered Email Address for Company Number ", logMap);

        try {
            RegisteredEmailAddressJson registeredEmailAddressJson = apiClientService.getInternalApiClient()
                    .privateCompanyResourceHandler()
                    .getCompanyRegisteredEmailAddress("/company/" + companyNumber + "/registered-email-address")
                    .execute()
                    .getData();

            return registeredEmailAddressJson;
        } catch (ApiErrorResponseException e) {
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