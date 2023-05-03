package uk.gov.companieshouse.registeredemailaddressapi.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.registeredemailaddressapi.model.dto.RegisteredEmailAddressDTO;
import uk.gov.companieshouse.registeredemailaddressapi.service.RegisteredEmailAddressService;
import uk.gov.companieshouse.registeredemailaddressapi.exception.ServiceException;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RegisteredEmailAddressControllerTest {

    private static final ResponseEntity<Object> CREATED_SUCCESS_RESPONSE = ResponseEntity.created(URI.create("URI")).body("Created");
    private static final ResponseEntity<Object> ERROR_RESPONSE = ResponseEntity.internalServerError().build();
    private static final String REQUEST_ID = "bswi3nj8jjn";
    private static final String USER_ID = "7272838";
    private static final String EMAIL_ADDRESS = "Test@Test.com";
    private static final String UNEXPECTED_ERROR = "UNEXPCTED ERROR - EXITING...";
    
    @Mock
    private RegisteredEmailAddressService registeredEmailAddressService;

    @Mock
    private Transaction transaction;

    @InjectMocks
    private RegisteredEmailAddressController registeredEmailAddressController;

    private RegisteredEmailAddressDTO registeredEmailAddressDTO;

    @BeforeEach
    void init() {
        registeredEmailAddressDTO = new RegisteredEmailAddressDTO();
        registeredEmailAddressDTO.setRegisteredEmailAddress(EMAIL_ADDRESS);
    }

    @Test
    void testCreateRegisteredEmailAddressSuccessTest() throws ServiceException {
        when(this.registeredEmailAddressService.createRegisteredEmailAddress(
            transaction,
            registeredEmailAddressDTO,
            REQUEST_ID,
            USER_ID)
        ).thenReturn(CREATED_SUCCESS_RESPONSE);

        var createRegisteredEmailAddressResponse = registeredEmailAddressController.createRegisteredEmailAddress(
            transaction,
            registeredEmailAddressDTO,
            REQUEST_ID,
            USER_ID
        );

        assertEquals(HttpStatus.CREATED.value(), createRegisteredEmailAddressResponse.getStatusCodeValue());
        assertEquals(CREATED_SUCCESS_RESPONSE, createRegisteredEmailAddressResponse);

        verify(registeredEmailAddressService).createRegisteredEmailAddress(
                transaction,
                registeredEmailAddressDTO,
                REQUEST_ID,
                USER_ID);
    }

    @Test
    void testCreateRegisteredEmailAddressErroTest() throws ServiceException {
        when(this.registeredEmailAddressService.createRegisteredEmailAddress(
            transaction,
            registeredEmailAddressDTO,
            REQUEST_ID,
            USER_ID)
        ).thenThrow(new RuntimeException(UNEXPECTED_ERROR));

        var createRegisteredEmailAddressResponse = registeredEmailAddressController.createRegisteredEmailAddress(
            transaction,
            registeredEmailAddressDTO,
            REQUEST_ID,
            USER_ID
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), createRegisteredEmailAddressResponse.getStatusCodeValue());
        assertEquals(ERROR_RESPONSE, createRegisteredEmailAddressResponse);

        verify(registeredEmailAddressService).createRegisteredEmailAddress(
                transaction,
                registeredEmailAddressDTO,
                REQUEST_ID,
                USER_ID);
    }

}