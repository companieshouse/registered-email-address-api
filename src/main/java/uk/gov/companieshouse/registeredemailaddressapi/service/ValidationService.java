package uk.gov.companieshouse.registeredemailaddressapi.service;

import com.google.gson.GsonBuilder;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusResponse;
import uk.gov.companieshouse.registeredemailaddressapi.model.dao.RegisteredEmailAddressDAO;
import uk.gov.companieshouse.registeredemailaddressapi.utils.ApiLogger;
import uk.gov.companieshouse.service.rest.err.Errors;

import java.util.ArrayList;

import static uk.gov.companieshouse.registeredemailaddressapi.utils.ValidationUtils.*;

@Service
public class ValidationService {

    public ValidationStatusResponse validateRegisteredEmailAddress(RegisteredEmailAddressDAO registeredEmailAddress,
                                                                   String requestId) {
        var errors = new ArrayList<ValidationStatusError>();
        if (isNotNull(registeredEmailAddress.getData(),
                "registered_email_address",
                errors,
                requestId)) {

            ApiLogger.debugContext(requestId, String.format("Registered Email Address found for Transaction %s.",
                    registeredEmailAddress.getTransactionId() ));

            isValidEmailAddress(registeredEmailAddress.getData().getRegisteredEmailAddress(),
                    "registered_email_address",
                    errors,
                    requestId);

            isEmailAddressStatementAccepted(registeredEmailAddress.getData().isAcceptAppropriateEmailAddressStatement(),
                    "accept_appropriate_email_address_statement",
                    errors,
                    requestId);


        }
        return formatValidationStatusResponse(errors, registeredEmailAddress, requestId);
    }

    private ValidationStatusResponse formatValidationStatusResponse(ArrayList<ValidationStatusError> validationErrors,
                                                                    RegisteredEmailAddressDAO registeredEmailAddress,
                                                                    String requestId) {
        var validationStatus = new ValidationStatusResponse();

        if (validationErrors.isEmpty()) {
            ApiLogger.infoContext(requestId, String.format("Validation Successful for TransactionId %s",
                    registeredEmailAddress.getTransactionId()));
            validationStatus.setValid(true);
        } else {

            ApiLogger.infoContext(requestId, String.format("Validation Unsuccessful for TransactionId %s, return errors",
                    registeredEmailAddress.getTransactionId()));
            validationStatus.setValid(false);
            ValidationStatusError[] validationStatusErrors = validationErrors.toArray(new ValidationStatusError[0]);
            validationStatus.setValidationStatusError(validationStatusErrors);
        }
        return validationStatus;
    }
}
