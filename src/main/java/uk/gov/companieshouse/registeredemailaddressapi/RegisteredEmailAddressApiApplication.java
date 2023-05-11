package uk.gov.companieshouse.registeredemailaddressapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication()
public class RegisteredEmailAddressApiApplication {

    public static final String REA_APP_NAMESPACE = "registered-email-address-api";
    
    public static void main(String[] args) {
        SpringApplication.run(RegisteredEmailAddressApiApplication.class, args);
    }
}
