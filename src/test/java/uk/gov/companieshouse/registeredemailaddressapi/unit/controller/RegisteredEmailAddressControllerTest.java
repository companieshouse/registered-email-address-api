package uk.gov.companieshouse.registeredemailaddressapi.unit.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.registeredemailaddressapi.controller.RegisteredEmailAddressController;
import uk.gov.companieshouse.registeredemailaddressapi.model.dto.RegisteredEmailAddressDTO;
import uk.gov.companieshouse.registeredemailaddressapi.service.RegisteredEmailAddressService;
import uk.gov.companieshouse.registeredemailaddressapi.exception.ServiceException;

import java.net.URI;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RegisteredEmailAddressControllerTest {

    private RegisteredEmailAddressDTO registeredEmailAddressDTO;
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

    @BeforeEach
    void init() {
        registeredEmailAddressDTO = new RegisteredEmailAddressDTO();
        registeredEmailAddressDTO.setRegisteredEmailAddress(EMAIL_ADDRESS);
    }

    @Test
    void testCreateRegisteredEmailAddressSuccessTest() throws ServiceException {

        registeredEmailAddressDTO.setId(UUID.randomUUID().toString());

        when(this.registeredEmailAddressService.createRegisteredEmailAddress(
            transaction,
            registeredEmailAddressDTO,
            REQUEST_ID,
            USER_ID)
        ).thenReturn(registeredEmailAddressDTO);

        var createRegisteredEmailAddressResponse = registeredEmailAddressController.createRegisteredEmailAddress(
            transaction,
            registeredEmailAddressDTO,
            REQUEST_ID,
            USER_ID
        );

        assertEquals(HttpStatus.CREATED.value(), createRegisteredEmailAddressResponse.getStatusCodeValue());
        assertEquals(registeredEmailAddressDTO, createRegisteredEmailAddressResponse.getBody());

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