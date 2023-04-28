package uk.gov.companieshouse.registeredemailaddressapi.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.companieshouse.registeredemailaddressapi.model.dto.RegisteredEmailAddressDTO;
import uk.gov.companieshouse.registeredemailaddressapi.service.RegisteredEmailAddressService;
import uk.gov.companieshouse.registeredemailaddressapi.utils.ApiLogger;

import java.net.URI;
import java.util.HashMap;

import static uk.gov.companieshouse.registeredemailaddressapi.utils.Constants.*;

@RestController
@RequestMapping("/registered-email-address")
public class RegisteredEmailAddressController {

    private final RegisteredEmailAddressService registeredEmailAddressService;
    @Autowired
    public RegisteredEmailAddressController(RegisteredEmailAddressService registeredEmailAddressService) {
        this.registeredEmailAddressService = registeredEmailAddressService;
    }
    @GetMapping("/healthcheck")
    public ResponseEntity<String> getHealthCheck() {
        return ResponseEntity.ok().body("Registered Email Address Service is Healthy");

    }
    @PostMapping("/transactions/{transaction-id}/registered-email-address")
    public ResponseEntity<RegisteredEmailAddressDTO> createNewSubmission(
            @PathVariable("transaction-id") String transactionId,
            @RequestBody RegisteredEmailAddressDTO registeredEmailAddressDto,
            @RequestHeader(value = ERIC_REQUEST_ID_KEY) String requestId,
            @RequestHeader(value = ERIC_IDENTITY) String userId) {

        HashMap<String, Object> logMap = new HashMap<String, Object>();
        logMap.put(TRANSACTION_ID_KEY, transactionId);


        try {
            //TODO - Add validation here

            ApiLogger.infoContext(requestId, "- Create registered email address request - ", logMap);

            RegisteredEmailAddressDTO response = this.registeredEmailAddressService.createRegisteredEmailAddress(
                    transactionId,
                    registeredEmailAddressDto,
                    requestId,
                    userId
            );

            return ResponseEntity.created(URI.create(response.getId())).body(response);

        }
        catch (Exception e) {
            ApiLogger.errorContext(requestId, "Error Creating Overseas Entity Submission", e, logMap);
            return new ResponseEntity<RegisteredEmailAddressDTO>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }


}
