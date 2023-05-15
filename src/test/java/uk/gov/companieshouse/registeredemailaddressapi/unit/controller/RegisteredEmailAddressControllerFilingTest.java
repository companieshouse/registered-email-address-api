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
import uk.gov.companieshouse.registeredemailaddressapi.exception.SubmissionNotFoundException;

import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
public class RegisteredEmailAddressControllerFilingTest {
    
    private static final String FILING_DESCRIPTION = "registered email address test filing";
    private static final String TEST_COMPANY_NUMBER = "000987699";
    private static final String ERIC_REQUEST_ID = "1234567890";
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
    void testCreateRegisteredEmailAddressSuccessTest() throws SubmissionNotFoundException {
       // mocking
       FilingApi filing = buildFiling();
       when(registeredEmailAddressFilingService.generateRegisteredEmailAddressFilings(transaction, ERIC_REQUEST_ID)).thenReturn(buildFiling());

        var createRegisteredEmailAddressFilingResponse = registeredEmailAddressFilingController.getRegisteredEmailAddressFilings(
            transaction,
            ERIC_REQUEST_ID,
            SUBMISSION_ID
        );

        assertNotNull(createRegisteredEmailAddressFilingResponse.getBody());
        assertEquals(1, createRegisteredEmailAddressFilingResponse.getBody().length);
        assertEquals(HttpStatus.OK.value(), createRegisteredEmailAddressFilingResponse.getStatusCodeValue());
        assertEquals(filing.getDescription(), createRegisteredEmailAddressFilingResponse.getBody()[0].getDescription());
    }

    @Test
    void testGCreateRegisteredEmailAddressSubmissionNotFound() throws SubmissionNotFoundException {
        when(registeredEmailAddressFilingService.generateRegisteredEmailAddressFilings(transaction, ERIC_REQUEST_ID)).thenThrow(SubmissionNotFoundException.class);

        var createRegisteredEmailAddressFilingResponse = registeredEmailAddressFilingController.getRegisteredEmailAddressFilings(
            transaction,
            ERIC_REQUEST_ID,
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
