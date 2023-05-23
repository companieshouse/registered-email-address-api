package uk.gov.companieshouse.registeredemailaddressapi.unit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Value;

import static org.mockito.Mockito.*;

import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.filinggenerator.FilingApi;
import uk.gov.companieshouse.registeredemailaddressapi.model.dto.RegisteredEmailAddressDTO;
import uk.gov.companieshouse.registeredemailaddressapi.service.RegisteredEmailAddressService;
import uk.gov.companieshouse.registeredemailaddressapi.service.RegisteredEmailAddressFilingService;
import uk.gov.companieshouse.registeredemailaddressapi.exception.SubmissionNotFoundException;

import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.registeredemailaddressapi.repository.RegisteredEmailAddressRepository;
import uk.gov.companieshouse.registeredemailaddressapi.service.TransactionService;
import uk.gov.companieshouse.registeredemailaddressapi.mapper.RegisteredEmailAddressMapper;
import uk.gov.companieshouse.registeredemailaddressapi.model.dao.RegisteredEmailAddressDAO;

import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;
import java.util.Optional;
import java.util.function.Supplier;

import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import static uk.gov.companieshouse.registeredemailaddressapi.utils.Constants.COMPANY_NUMBER;
import static uk.gov.companieshouse.registeredemailaddressapi.utils.Constants.FILING_KIND;

@ExtendWith(MockitoExtension.class)
class RegisteredEmailAddressFilingServiceTest {
    
    private static final String TEST_EMAIL = "test@Test.com";
    private static final String TEST_COMPANY_NUMBER = "000987699";
    private static final String REA_FILING_DESCRIPTION_IDENTIFIER = "Registered Email Address Filing Description Id";
    private static final String REA_FILING_DESCRIPTION = "Registered Email Address Filing Description with registration date {date}";
    private static final String REA_UPDATE_FILING_DESCRIPTION = "Registered Email Address Filing update statement made {date}";
    private static final String SERVICE_EXCEPTION = "Empty data set returned when generating filing for %s";
    private static final LocalDate FILING_DUMMY_DATE = LocalDate.of(2023, 3, 26);

    private static final String TRANSACTION_ID = UUID.randomUUID().toString();
    private static final String SUBMISSION_ID = UUID.randomUUID().toString();

    @Value("${REGISTERED_EMAIL_ADDRESS_FILING_DESCRIPTION_IDENTIFIER}")
    private String filingDescriptionIdentifier;

    @Value("${REGISTERED_EMAIL_ADDRESS_FILING_DESCRIPTION}")
    private String filingDescription;

    @Value("${REGISTERED_EMAIL_ADDRESS_UPDATE_FILING_DESCRIPTION}")
    private String updateFilingDescription;

    @Mock
    private TransactionService transactionService;

    @Mock
    private RegisteredEmailAddressMapper registeredEmailAddressMapper;

    @Mock
    private RegisteredEmailAddressRepository registeredEmailAddressRepository;

    @Mock
    private RegisteredEmailAddressService registeredEmailAddressService;

    @Mock
    private Supplier<LocalDate> localDateSupplier;

    @InjectMocks
    private RegisteredEmailAddressFilingService registeredEmailAddressFilingService;

    private Transaction transaction;

    @BeforeEach
    void init() {
        transaction = buildTransaction();
        ReflectionTestUtils.setField(registeredEmailAddressFilingService, "filingDescriptionIdentifier", REA_FILING_DESCRIPTION_IDENTIFIER);
        ReflectionTestUtils.setField(registeredEmailAddressFilingService, "updateFilingDescription", REA_UPDATE_FILING_DESCRIPTION);
        ReflectionTestUtils.setField(registeredEmailAddressFilingService, "filingDescription", REA_FILING_DESCRIPTION);
    }

    @Test
    void testRegisteredEmailAddressFilingFilingReturnedSuccessfully() throws SubmissionNotFoundException, IOException, URIValidationException {
        // mocking
        when(localDateSupplier.get()).thenReturn(FILING_DUMMY_DATE);
        when(registeredEmailAddressService.getRegisteredEmailAddressSubmission(SUBMISSION_ID)).thenReturn(Optional.of(buildRegisteredEmailAddressDTO()));
        when(registeredEmailAddressRepository.findByTransactionId(TRANSACTION_ID)).thenReturn(buildRegisteredEmailAddressDAO());

        FilingApi registeredEmailAddressFiling = registeredEmailAddressFilingService.generateRegisteredEmailAddressFilings(transaction);

        verify(localDateSupplier, times(1)).get();
        assertEquals(FILING_KIND, registeredEmailAddressFiling.getKind());
        assertEquals(REA_FILING_DESCRIPTION_IDENTIFIER, registeredEmailAddressFiling.getDescriptionIdentifier());
        assertEquals("Registered Email Address Filing update statement made 26 March 2023", registeredEmailAddressFiling.getDescription());   
        assertEquals(TEST_EMAIL, registeredEmailAddressFiling.getData().get(FILING_KIND));
        assertEquals(TEST_COMPANY_NUMBER, registeredEmailAddressFiling.getData().get(COMPANY_NUMBER));
    }

    @Test
    void testRegisteredEmailAddressFilingErrorHandledSuccessfully() throws SubmissionNotFoundException {
        // mocking
        when(registeredEmailAddressService.getRegisteredEmailAddressSubmission(SUBMISSION_ID)).thenReturn(Optional.empty());
        when(registeredEmailAddressRepository.findByTransactionId(TRANSACTION_ID)).thenReturn(buildRegisteredEmailAddressDAO());

        var submissionNotFoundException = assertThrows(
            SubmissionNotFoundException.class,
            () -> registeredEmailAddressFilingService.generateRegisteredEmailAddressFilings(transaction)
        );

        assertTrue(submissionNotFoundException.getMessage()
                .contains(String.format(SERVICE_EXCEPTION, SUBMISSION_ID)));
    }

    private Transaction buildTransaction() {
        Transaction transaction = new Transaction();
        transaction.setId(TRANSACTION_ID);
        transaction.setCompanyNumber(TEST_COMPANY_NUMBER);
        return transaction;
    }

    private RegisteredEmailAddressDTO buildRegisteredEmailAddressDTO() {
        RegisteredEmailAddressDTO registeredEmailAddressDTO = new RegisteredEmailAddressDTO();
        registeredEmailAddressDTO.setRegisteredEmailAddress(TEST_EMAIL);
        registeredEmailAddressDTO.setId(SUBMISSION_ID);
        return registeredEmailAddressDTO;
    }

    private RegisteredEmailAddressDAO buildRegisteredEmailAddressDAO() {
        RegisteredEmailAddressDAO registeredEmailAddressDAO = new RegisteredEmailAddressDAO();
        registeredEmailAddressDAO.setRegisteredEmailAddress(TEST_EMAIL);
        registeredEmailAddressDAO.setId(SUBMISSION_ID);
        return registeredEmailAddressDAO;
    }
}
