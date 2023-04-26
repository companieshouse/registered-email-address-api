package uk.gov.companieshouse.registeredemailaddressapi.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RegisteredEmailAddressController {

    @GetMapping("/registered-email-address/healthcheck")
    public ResponseEntity<String> getHealthCheck() {
        return ResponseEntity.ok().body("Registered Email Address Service is Healthy");

    }

}
