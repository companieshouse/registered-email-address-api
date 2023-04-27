package uk.gov.companieshouse.registeredemailaddressapi.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RegisteredEmailAddressController {
    // controller class routing constants
    private static final String REGISTERED_EMAIL_HEALTHCHECK_ROUTE = "/registered-email-address/healthcheck";

    // controller class response constants
    private static final String HEALTHCHECK_RESPONSE = "/registered-email-address/healthcheck";

    @GetMapping(REGISTERED_EMAIL_HEALTHCHECK_ROUTE)
    public ResponseEntity<String> getHealthCheck() {
        return ResponseEntity.ok().body(HEALTHCHECK_RESPONSE);

    }

}
