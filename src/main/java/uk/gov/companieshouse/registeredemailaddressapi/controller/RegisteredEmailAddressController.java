package uk.gov.companieshouse.registeredemailaddressapi.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusResponse;
import uk.gov.companieshouse.registeredemailaddressapi.exception.SubmissionNotFoundException;
import uk.gov.companieshouse.registeredemailaddressapi.model.dto.RegisteredEmailAddressDTO;
import uk.gov.companieshouse.registeredemailaddressapi.service.RegisteredEmailAddressService;
import uk.gov.companieshouse.registeredemailaddressapi.utils.ApiLogger;

import javax.validation.Valid;
import java.net.URI;
import java.util.HashMap;

import static uk.gov.companieshouse.registeredemailaddressapi.utils.Constants.ERIC_IDENTITY;
import static uk.gov.companieshouse.registeredemailaddressapi.utils.Constants.ERIC_REQUEST_ID_KEY;
import static uk.gov.companieshouse.registeredemailaddressapi.utils.Constants.TRANSACTION_ID_KEY;
import static uk.gov.companieshouse.registeredemailaddressapi.utils.Constants.TRANSACTION_KEY;

@RestController
@RequestMapping("/transactions/{" + TRANSACTION_ID_KEY + "}/registered-email-address")
public class RegisteredEmailAddressController {

    // error constant(s)
    private static final String SUBMISSION_ERROR = "Error Creating registered email address Submission";

    // controller logging constant(s)
    private static final String REA_REQUEST = "- Create registered email address request - ";

    private final RegisteredEmailAddressService registeredEmailAddressService;

    public RegisteredEmailAddressController(RegisteredEmailAddressService registeredEmailAddressService) {
        this.registeredEmailAddressService = registeredEmailAddressService;
    }

    @PostMapping()
    public ResponseEntity<Object> createRegisteredEmailAddress(
            @RequestAttribute(TRANSACTION_KEY) Transaction transaction,
            @Valid @RequestBody RegisteredEmailAddressDTO registeredEmailAddressDto,
            @RequestHeader(value = ERIC_REQUEST_ID_KEY) String requestId,
            @RequestHeader(value = ERIC_IDENTITY) String userId) {

        HashMap<String, Object> logMap = new HashMap<String, Object>();
        logMap.put(TRANSACTION_ID_KEY, transaction.getId());

        try {
            ApiLogger.infoContext(requestId, REA_REQUEST, logMap);

            RegisteredEmailAddressDTO registeredEmailAddress = this.registeredEmailAddressService
                    .createRegisteredEmailAddress(
                            transaction,
                            registeredEmailAddressDto,
                            requestId,
                            userId);

            return ResponseEntity.created(URI.create(registeredEmailAddress.getId())).body(registeredEmailAddress);

        } catch (Exception e) {
            ApiLogger.errorContext(requestId, SUBMISSION_ERROR, e, logMap);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @GetMapping("/validation-status")
    public ResponseEntity<ValidationStatusResponse> getValidationStatus(
            @PathVariable(TRANSACTION_ID_KEY) String transactionId,
            @RequestHeader(value = ERIC_REQUEST_ID_KEY) String requestId) throws SubmissionNotFoundException {

        ApiLogger.debugContext(requestId, "Called getValidationStatus(...)");

        var logMap = new HashMap<String, Object>();
        logMap.put(TRANSACTION_ID_KEY, transactionId);

        ApiLogger.infoContext(requestId, "Calling service to get the registered email address submission", logMap);

        ValidationStatusResponse response = registeredEmailAddressService
                .getValidationStatus(transactionId, requestId);

        return ResponseEntity.ok().body(response);

    }

    @GetMapping
    public ResponseEntity<String> getRegisteredEmailAddress(
            @PathVariable(TRANSACTION_ID_KEY) String transactionId,
            @RequestHeader(value = ERIC_REQUEST_ID_KEY) String requestId) throws SubmissionNotFoundException {

        ApiLogger.debugContext(requestId, "Called getRegisteredEmailAddress(...)");

        var logMap = new HashMap<String, Object>();
        logMap.put(TRANSACTION_ID_KEY, transactionId);

        ApiLogger.infoContext(requestId, "Calling service to get the registered email address", logMap);

        String registeredEmailAddress = registeredEmailAddressService.getRegisteredEmailAddress(transactionId, requestId);

        return ResponseEntity.ok().body(registeredEmailAddress);
    }
}
