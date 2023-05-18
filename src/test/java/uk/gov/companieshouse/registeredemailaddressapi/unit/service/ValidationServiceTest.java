package uk.gov.companieshouse.registeredemailaddressapi.unit.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusResponse;
import uk.gov.companieshouse.registeredemailaddressapi.client.ApiClientService;
import uk.gov.companieshouse.registeredemailaddressapi.model.dao.RegisteredEmailAddressDAO;
import uk.gov.companieshouse.registeredemailaddressapi.service.ValidationService;
import uk.gov.companieshouse.registeredemailaddressapi.utils.ValidationUtils;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.companieshouse.registeredemailaddressapi.utils.ValidationUtils.INVALID_EMAIL_ERROR_MESSAGE;
import static uk.gov.companieshouse.registeredemailaddressapi.utils.ValidationUtils.NOT_NULL_ERROR_MESSAGE;

@ExtendWith(MockitoExtension.class)
class ValidationServiceTest {

    private static final String TRANSACTION_ID = UUID.randomUUID().toString();
    private static final String REQUEST_ID = UUID.randomUUID().toString();

    @Mock
    private ApiClientService apiClientService;

    @Mock
    private ValidationUtils validationUtils;


    @InjectMocks
    private ValidationService validationService;

    @Test
    void testvalidateRegisteredEmailAddressSuccessful() {
        RegisteredEmailAddressDAO registeredEmailAddress =  new RegisteredEmailAddressDAO();
        registeredEmailAddress.setRegisteredEmailAddress("Test@Test.com");

        ValidationStatusResponse response = validationService.validateRegisteredEmailAddress(registeredEmailAddress, REQUEST_ID);

        assertEquals(true, response.isValid());

    }

    @Test
    void testvalidateOfEmptyRegisteredEmailAddress() {
        RegisteredEmailAddressDAO registeredEmailAddress =  new RegisteredEmailAddressDAO();
        registeredEmailAddress.setRegisteredEmailAddress(null);

        ValidationStatusResponse response = validationService.validateRegisteredEmailAddress(registeredEmailAddress, REQUEST_ID);

        assertEquals(false, response.isValid());
        assertTrue(Arrays.stream(response.getValidationStatusError()).findFirst().get().getError()
                .contains( String.format(NOT_NULL_ERROR_MESSAGE, "registered_email_address")));

    }

    @Test
    void testvalidateOfInvalidRegisteredEmailAddress() {
        RegisteredEmailAddressDAO registeredEmailAddress =  new RegisteredEmailAddressDAO();
        registeredEmailAddress.setRegisteredEmailAddress("234$@3df");

        ValidationStatusResponse response = validationService.validateRegisteredEmailAddress(registeredEmailAddress, REQUEST_ID);

        assertEquals(false, response.isValid());
        assertTrue(Arrays.stream(response.getValidationStatusError()).findFirst().get().getError()
                .contains( String.format(INVALID_EMAIL_ERROR_MESSAGE, "registered_email_address")));

    }

}