package uk.gov.companieshouse.registeredemailaddressapi.unit.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusResponse;
import uk.gov.companieshouse.registeredemailaddressapi.model.dao.RegisteredEmailAddressDAO;
import uk.gov.companieshouse.registeredemailaddressapi.model.dao.RegisteredEmailAddressData;
import uk.gov.companieshouse.registeredemailaddressapi.service.ValidationService;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.companieshouse.registeredemailaddressapi.utils.ValidationUtils.*;

@ExtendWith(MockitoExtension.class)
class ValidationServiceTest {

    private static final String REQUEST_ID = UUID.randomUUID().toString();

    @InjectMocks
    private ValidationService validationService;

    @Test
    void testValidateRegisteredEmailAddressSuccessful() {
        RegisteredEmailAddressData registeredEmailAddressData =  new RegisteredEmailAddressData();
        registeredEmailAddressData.setRegisteredEmailAddress("Test@Test.com");
        registeredEmailAddressData.setAcceptAppropriateEmailAddressStatement(true);
        RegisteredEmailAddressDAO registeredEmailAddress =  new RegisteredEmailAddressDAO();
        registeredEmailAddress.setData(registeredEmailAddressData);

        ValidationStatusResponse response = validationService.validateRegisteredEmailAddress(registeredEmailAddress, REQUEST_ID);

        assertEquals(true, response.isValid());

    }

    @Test
    void testValidateOfEmptyRegisteredEmailAddress() {
        RegisteredEmailAddressDAO registeredEmailAddress =  new RegisteredEmailAddressDAO();
        registeredEmailAddress.setData(null);

        ValidationStatusResponse response = validationService.validateRegisteredEmailAddress(registeredEmailAddress, REQUEST_ID);

        assertEquals(false, response.isValid());
        assertTrue(Arrays.stream(response.getValidationStatusError()).findFirst().get().getError()
                .contains( String.format(NOT_NULL_ERROR_MESSAGE, "registered_email_address")));

    }

    @Test
    void testvalidateOfInvalidRegisteredEmailAddress() {
        RegisteredEmailAddressData registeredEmailAddressData =  new RegisteredEmailAddressData();
        registeredEmailAddressData.setRegisteredEmailAddress("234$@3df");
        RegisteredEmailAddressDAO registeredEmailAddress =  new RegisteredEmailAddressDAO();
        registeredEmailAddress.setData(registeredEmailAddressData);

        ValidationStatusResponse response = validationService.validateRegisteredEmailAddress(registeredEmailAddress, REQUEST_ID);

        assertEquals(false, response.isValid());
        assertTrue(Arrays.stream(response.getValidationStatusError()).findFirst().get().getError()
                .contains( String.format(INVALID_EMAIL_ERROR_MESSAGE, "registered_email_address")));

    }

    @Test
    void testValidateOfFalseAppropriateEmailAddressStatement() {
        RegisteredEmailAddressData registeredEmailAddressData =  new RegisteredEmailAddressData();
        registeredEmailAddressData.setRegisteredEmailAddress("Test@Test.com");
        registeredEmailAddressData.setAcceptAppropriateEmailAddressStatement(false);
        RegisteredEmailAddressDAO registeredEmailAddress =  new RegisteredEmailAddressDAO();
        registeredEmailAddress.setData(registeredEmailAddressData);

        ValidationStatusResponse response = validationService.validateRegisteredEmailAddress(registeredEmailAddress, REQUEST_ID);

        assertEquals(false, response.isValid());

        assertTrue(Arrays.stream(response.getValidationStatusError()).findFirst().get().getError()
                .contains( String.format(ACCEPTED_EMAIL_ADDRESS_STATEMENT_ERROR_MESSAGE, "accept_appropriate_email_address_statement")));

    }

}