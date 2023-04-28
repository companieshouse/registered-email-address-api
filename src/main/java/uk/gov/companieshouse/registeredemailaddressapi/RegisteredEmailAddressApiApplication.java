package uk.gov.companieshouse.registeredemailaddressapi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication()
public class RegisteredEmailAddressApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(RegisteredEmailAddressApiApplication.class, args);
    }
}
