package uk.gov.companieshouse.registeredemailaddressapi.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/registered-email-address")
public class HealthCheckController {

    @GetMapping("/healthcheck")
    public ResponseEntity<String> getHealthCheck() {
        return ResponseEntity.ok().body("Registered Email Address Service is Healthy");

    }

}
