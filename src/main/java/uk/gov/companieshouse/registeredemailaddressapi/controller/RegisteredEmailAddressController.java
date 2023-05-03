package uk.gov.companieshouse.registeredemailaddressapi.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.registeredemailaddressapi.exception.ServiceException;
import uk.gov.companieshouse.registeredemailaddressapi.model.dto.RegisteredEmailAddressDTO;
import uk.gov.companieshouse.registeredemailaddressapi.model.dto.RegisteredEmailAddress;
import uk.gov.companieshouse.registeredemailaddressapi.service.RegisteredEmailAddressService;
import uk.gov.companieshouse.registeredemailaddressapi.utils.ApiLogger;

import javax.validation.Valid;
import java.net.URI;
import java.util.HashMap;

import static uk.gov.companieshouse.registeredemailaddressapi.utils.Constants.*;

@RestController
@RequestMapping()
public class RegisteredEmailAddressController {

    private final RegisteredEmailAddressService registeredEmailAddressService;

    @Autowired
    public RegisteredEmailAddressController(RegisteredEmailAddressService registeredEmailAddressService) {
        this.registeredEmailAddressService = registeredEmailAddressService;
    }

    @GetMapping("/registered-email-address/healthcheck")
    public ResponseEntity<String> getHealthCheck() {
        return ResponseEntity.ok().body("Registered Email Address Service is Healthy");

    }

    @PostMapping("/transactions/{" + TRANSACTION_ID_KEY + "}/registered-email-address")
    public ResponseEntity<Object> createRegisteredEmailAddress(
            @RequestAttribute("transaction") Transaction transaction,
            @Valid @RequestBody RegisteredEmailAddress registeredEmailAddressDto,
            @RequestHeader(value = ERIC_REQUEST_ID_KEY) String requestId,
            @RequestHeader(value = ERIC_IDENTITY) String userId) {

        try {

            HashMap<String, Object> logMap = new HashMap<String, Object>();
            logMap.put(TRANSACTION_ID_KEY, transaction.getId());

            //TODO - Add validation here

            ApiLogger.infoContext(requestId, "- Create registered email address request - ", logMap);


            RegisteredEmailAddressDTO response = this.registeredEmailAddressService.createRegisteredEmailAddress(
                    transaction,
                    registeredEmailAddressDto,
                    requestId,
                    userId
            );

            return ResponseEntity.created(URI.create(response.getId())).body(response);

        } catch (Exception e) {
            return (ResponseEntity<Object>) ResponseEntity.internalServerError();
        }

    }


}
