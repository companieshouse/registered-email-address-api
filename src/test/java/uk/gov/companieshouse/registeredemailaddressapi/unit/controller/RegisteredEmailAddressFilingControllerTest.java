package uk.gov.companieshouse.registeredemailaddressapi.unit.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.filinggenerator.FilingApi;
import uk.gov.companieshouse.registeredemailaddressapi.service.RegisteredEmailAddressService;
import uk.gov.companieshouse.registeredemailaddressapi.controller.RegisteredEmailAddressFilingController;
import uk.gov.companieshouse.registeredemailaddressapi.service.RegisteredEmailAddressFilingService;
import uk.gov.companieshouse.registeredemailaddressapi.exception.NotFoundException;

import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
class RegisteredEmailAddressFilingControllerTest {
    
    private static final String FILING_DESCRIPTION = "registered email address test filing";
    private static final String TEST_COMPANY_NUMBER = "000987699";
    private static final String TRANSACTION_ID = UUID.randomUUID().toString();
    private static final String SUBMISSION_ID = UUID.randomUUID().toString();

    @Mock
    private RegisteredEmailAddressService registeredEmailAddressService;

    @Mock
    private RegisteredEmailAddressFilingService registeredEmailAddressFilingService;

    @InjectMocks
    private RegisteredEmailAddressFilingController registeredEmailAddressFilingController;

    private Transaction transaction;

    @BeforeEach
    void init() {
        transaction = buildTransaction();
    }

    @Test
    void testCreateRegisteredEmailAddressSuccessTest() throws NotFoundException {
       // mocking
       FilingApi filing = buildFiling();
       when(registeredEmailAddressFilingService.generateRegisteredEmailAddressFilings(transaction)).thenReturn(buildFiling());

        var createRegisteredEmailAddressFilingResponse = registeredEmailAddressFilingController.getRegisteredEmailAddressFilings(
            transaction,
            SUBMISSION_ID
        );

        assertNotNull(createRegisteredEmailAddressFilingResponse.getBody());
        assertEquals(1, createRegisteredEmailAddressFilingResponse.getBody().length);
        assertEquals(HttpStatus.OK.value(), createRegisteredEmailAddressFilingResponse.getStatusCode().value());
        assertEquals(filing.getDescription(), createRegisteredEmailAddressFilingResponse.getBody()[0].getDescription());
    }

    @Test
    void testGCreateRegisteredEmailAddressSubmissionNotFound() throws NotFoundException {
        when(registeredEmailAddressFilingService.generateRegisteredEmailAddressFilings(transaction)).thenThrow(NotFoundException.class);

        var createRegisteredEmailAddressFilingResponse = registeredEmailAddressFilingController.getRegisteredEmailAddressFilings(
            transaction,
            SUBMISSION_ID
        );

        assertNull(createRegisteredEmailAddressFilingResponse.getBody());
        assertEquals(HttpStatus.NOT_FOUND, createRegisteredEmailAddressFilingResponse.getStatusCode());
    }

    private Transaction buildTransaction() {
        Transaction transaction = new Transaction();
        transaction.setId(TRANSACTION_ID);
        transaction.setCompanyNumber(TEST_COMPANY_NUMBER);
        return transaction;
    }

    private FilingApi buildFiling() {
        FilingApi filing = new FilingApi();
        filing.setDescription(FILING_DESCRIPTION);
        return filing;
    }
}
