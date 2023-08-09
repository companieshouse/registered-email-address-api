package uk.gov.companieshouse.registeredemailaddressapi.controller;

import com.google.api.client.util.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusResponse;
import uk.gov.companieshouse.registeredemailaddressapi.exception.ServiceException;
import uk.gov.companieshouse.registeredemailaddressapi.exception.SubmissionNotFoundException;
import uk.gov.companieshouse.registeredemailaddressapi.model.dto.RegisteredEmailAddressDTO;
import uk.gov.companieshouse.registeredemailaddressapi.model.dto.RegisteredEmailAddressResponseDTO;
import uk.gov.companieshouse.registeredemailaddressapi.service.RegisteredEmailAddressService;
import uk.gov.companieshouse.registeredemailaddressapi.utils.ApiLogger;

import javax.validation.Valid;
import java.net.URI;
import java.util.HashMap;

import static uk.gov.companieshouse.registeredemailaddressapi.utils.Constants.*;

@RestController
@RequestMapping("/transactions/{" + TRANSACTION_ID_KEY + "}/registered-email-address")
public class RegisteredEmailAddressController {

    private static final String REA_REQUEST = "- Create registered email address request - ";

    private final RegisteredEmailAddressService registeredEmailAddressService;

    @Autowired
    public RegisteredEmailAddressController(RegisteredEmailAddressService registeredEmailAddressService) {
        this.registeredEmailAddressService = registeredEmailAddressService;
    }

    @PostMapping()
    public ResponseEntity<RegisteredEmailAddressResponseDTO> createRegisteredEmailAddress(
            @RequestAttribute(TRANSACTION_KEY) Transaction transaction,
            @Valid @RequestBody RegisteredEmailAddressDTO registeredEmailAddressDto,
            @RequestHeader(value = ERIC_REQUEST_ID_KEY) String requestId,
            @RequestHeader(value = ERIC_IDENTITY) String userId) throws ServiceException {

        HashMap<String, Object> logMap = new HashMap<>();
        logMap.put(TRANSACTION_ID_KEY, transaction.getId());

            ApiLogger.infoContext(requestId, REA_REQUEST, logMap);

            RegisteredEmailAddressResponseDTO registeredEmailAddress = this.registeredEmailAddressService
                    .createRegisteredEmailAddress(
                            transaction,
                            registeredEmailAddressDto,
                            requestId,
                            userId);

            return ResponseEntity.created(URI.create(registeredEmailAddress.getId())).body(registeredEmailAddress);

    }

    @PutMapping()
    public ResponseEntity<RegisteredEmailAddressResponseDTO> updateRegisteredEmailAddress(
            @RequestAttribute(TRANSACTION_KEY) Transaction transaction,
            @Valid @RequestBody RegisteredEmailAddressDTO registeredEmailAddressDto,
            @RequestHeader(value = ERIC_REQUEST_ID_KEY) String requestId,
            @RequestHeader(value = ERIC_IDENTITY) String userId) throws ServiceException, SubmissionNotFoundException {

        HashMap<String, Object> logMap = Maps.newHashMap();
        logMap.put(TRANSACTION_ID_KEY, transaction.getId());

            ApiLogger.infoContext(requestId, REA_REQUEST, logMap);

        RegisteredEmailAddressResponseDTO registeredEmailAddress = this.registeredEmailAddressService
                    .updateRegisteredEmailAddress(
                            transaction,
                            registeredEmailAddressDto,
                            requestId,
                            userId);

            return ResponseEntity.ok().body(registeredEmailAddress);

    }

    @GetMapping
    public ResponseEntity<RegisteredEmailAddressResponseDTO> getRegisteredEmailAddressFilingSubmission(
            @PathVariable(TRANSACTION_ID_KEY) String transactionId,
            @RequestHeader(value = ERIC_REQUEST_ID_KEY) String requestId) throws SubmissionNotFoundException {

        ApiLogger.debugContext(requestId, "Called getRegisteredEmailAddress(...)");

        var logMap = new HashMap<String, Object>();
        logMap.put(TRANSACTION_ID_KEY, transactionId);

        ApiLogger.infoContext(requestId, "Calling service to get the registered email address", logMap);

        RegisteredEmailAddressResponseDTO registeredEmailAddress =
                registeredEmailAddressService.getRegisteredEmailAddress(transactionId, requestId);

        return ResponseEntity.ok().body(registeredEmailAddress);
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
}
