package uk.gov.companieshouse.registeredemailaddressapi.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.registeredemailaddressapi.model.dto.RegisteredEmailAddressDTO;
import uk.gov.companieshouse.registeredemailaddressapi.service.RegisteredEmailAddressService;
import uk.gov.companieshouse.registeredemailaddressapi.utils.ApiLogger;

import javax.validation.Valid;
import java.net.URI;
import java.util.HashMap;

import static uk.gov.companieshouse.registeredemailaddressapi.utils.Constants.*;

@RestController
@RequestMapping()
public class RegisteredEmailAddressController {

    // error constant(s)
    private static final String SUBMISSION_ERROR = "Error Creating registered email address Submission";

    // controller logging constant(s)
    private static final String REA_REQUEST = "- Create registered email address request - ";

    private final RegisteredEmailAddressService registeredEmailAddressService;

    public RegisteredEmailAddressController(RegisteredEmailAddressService registeredEmailAddressService) {
        this.registeredEmailAddressService = registeredEmailAddressService;
    }

    @PostMapping("/transactions/{" + TRANSACTION_ID_KEY + "}/registered-email-address")
    public ResponseEntity<Object> createRegisteredEmailAddress(
            @RequestAttribute("transaction") Transaction transaction,
            @Valid @RequestBody RegisteredEmailAddressDTO registeredEmailAddressDto,
            @RequestHeader(value = ERIC_REQUEST_ID_KEY) String requestId,
            @RequestHeader(value = ERIC_IDENTITY) String userId) {

        HashMap<String, Object> logMap = new HashMap<String, Object>();
        logMap.put(TRANSACTION_ID_KEY, transaction.getId());

        try {

            //TODO - Add validation here

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

}
