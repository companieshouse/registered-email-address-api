package uk.gov.companieshouse.registeredemailaddressapi.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.registeredemailaddressapi.client.ApiClientService;
import uk.gov.companieshouse.registeredemailaddressapi.exception.ServiceException;
import uk.gov.companieshouse.registeredemailaddressapi.utils.ApiLogger;

import java.io.IOException;

import static uk.gov.companieshouse.registeredemailaddressapi.utils.Constants.TRANSACTIONS_PRIVATE_API_PREFIX;
import static uk.gov.companieshouse.registeredemailaddressapi.utils.Constants.TRANSACTIONS_PUBLIC_API_PREFIX;

@Service
public class TransactionService {

    private final ApiClientService apiClientService;

    public TransactionService(ApiClientService apiClientService) {
        this.apiClientService = apiClientService;
    }

    public Transaction getTransaction(String transactionId, String passthroughHeader, String loggingContext) throws ServiceException {
        try {
            var uri = TRANSACTIONS_PUBLIC_API_PREFIX + transactionId;
            return apiClientService.getOauthAuthenticatedClient(passthroughHeader).transactions().get(uri).execute().getData();
        } catch (URIValidationException | IOException e) {
            var message = "Error Retrieving Transaction " + transactionId;
            ApiLogger.errorContext(loggingContext, message, e);
            throw new ServiceException(message, e);
        }
    }

    public void updateTransaction(Transaction transaction, String loggingContext) throws ServiceException {
        try {
            var uri = TRANSACTIONS_PRIVATE_API_PREFIX + transaction.getId();

            // The internal API key client is used here as the transaction service will call back into the OE API to get
            // the costs (if a costs end-point has already been set on the transaction) and those calls cannot be made
            // with a user token
            var response = apiClientService.getInternalApiClient()
                    .privateTransaction().patch(uri, transaction).execute();

            if (response.getStatusCode() != 204) {
                throw new IOException("Invalid Status Code received from Transactions-api: " + response.getStatusCode());
            }
        } catch (IOException | URIValidationException e) {
            var message = "Error Updating Transaction " + transaction.getId();
            ApiLogger.errorContext(loggingContext, message, e);
            throw new ServiceException(message, e);
        }
    }
}
