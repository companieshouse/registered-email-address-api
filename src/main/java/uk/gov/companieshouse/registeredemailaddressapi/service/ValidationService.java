package uk.gov.companieshouse.registeredemailaddressapi.service;

import com.google.gson.GsonBuilder;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusResponse;
import uk.gov.companieshouse.registeredemailaddressapi.model.dao.RegisteredEmailAddressDAO;
import uk.gov.companieshouse.registeredemailaddressapi.utils.ApiLogger;
import uk.gov.companieshouse.service.rest.err.Errors;

import static uk.gov.companieshouse.registeredemailaddressapi.utils.ValidationUtils.isNotNull;
import static uk.gov.companieshouse.registeredemailaddressapi.utils.ValidationUtils.isValidEmailAddress;

@Service
public class ValidationService {

    public ValidationStatusResponse validateRegisteredEmailAddress(RegisteredEmailAddressDAO registeredEmailAddress,
                                                                   String requestId) {
        var errors = new Errors();
        if (isNotNull(registeredEmailAddress.getRegisteredEmailAddress(),
                "registered_email_address",
                errors,
                requestId)) {

            isValidEmailAddress(registeredEmailAddress.getRegisteredEmailAddress(),
                    "registered_email_address",
                    errors,
                    requestId);

        }
        return formatValidationStatusResponse(errors, registeredEmailAddress, requestId);
    }

    private ValidationStatusResponse formatValidationStatusResponse(Errors validationErrors,
                                                                    RegisteredEmailAddressDAO registeredEmailAddress,
                                                                    String requestId) {
        var validationStatus = new ValidationStatusResponse();

        if (!validationErrors.hasErrors()) {
            ApiLogger.infoContext(requestId, String.format("Validation Successful for TransactionId %s",
                    registeredEmailAddress.getTransactionId()));

            validationStatus.setValid(true);
        } else {
            ApiLogger.infoContext(requestId, String.format("Validation Unsuccessful for TransactionId %s, return errors",
                    registeredEmailAddress.getTransactionId()));
            String errorsAsJsonString = convertErrorsToJsonString(validationErrors);
            validationStatus.setValid(false);
            var errors = new ValidationStatusError[1];
            var error = new ValidationStatusError();
            error.setError(errorsAsJsonString);
            errors[0] = error;

            validationStatus.setValidationStatusError(errors);
        }
        return validationStatus;
    }

    private static String convertErrorsToJsonString(Errors validationErrors) {
        var gson = new GsonBuilder().create();
        return gson.toJson(validationErrors);
    }

}
