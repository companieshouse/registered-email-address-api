package uk.gov.companieshouse.registeredemailaddressapi.unit.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.company.RegisteredEmailAddressJson;
import uk.gov.companieshouse.api.model.transaction.Resource;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusResponse;
import uk.gov.companieshouse.registeredemailaddressapi.exception.*;
import uk.gov.companieshouse.registeredemailaddressapi.mapper.RegisteredEmailAddressMapper;
import uk.gov.companieshouse.registeredemailaddressapi.model.dao.RegisteredEmailAddressDAO;
import uk.gov.companieshouse.registeredemailaddressapi.model.dao.RegisteredEmailAddressData;
import uk.gov.companieshouse.registeredemailaddressapi.model.dto.RegisteredEmailAddressDTO;
import uk.gov.companieshouse.registeredemailaddressapi.model.dto.RegisteredEmailAddressResponseDTO;
import uk.gov.companieshouse.registeredemailaddressapi.model.dto.RegisteredEmailAddressResponseData;
import uk.gov.companieshouse.registeredemailaddressapi.repository.RegisteredEmailAddressRepository;
import uk.gov.companieshouse.registeredemailaddressapi.service.PrivateDataRetrievalService;
import uk.gov.companieshouse.registeredemailaddressapi.service.RegisteredEmailAddressService;
import uk.gov.companieshouse.registeredemailaddressapi.service.TransactionService;
import uk.gov.companieshouse.registeredemailaddressapi.service.ValidationService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.companieshouse.api.model.transaction.TransactionStatus.CLOSED;
import static uk.gov.companieshouse.api.model.transaction.TransactionStatus.OPEN;
import static uk.gov.companieshouse.registeredemailaddressapi.utils.Constants.FILING_KIND;

@ExtendWith(MockitoExtension.class)
class RegisteredEmailAddressServiceTest {
    private static final String COMPANY_NUMBER = "12345678";
    private static final String COMPANY_EMAIL = "tester@testing.com";

    private static final String REQUEST_ID = UUID.randomUUID().toString();
    private static final String USER_ID = UUID.randomUUID().toString();
    private static final String SUBMISSION_ID = UUID.randomUUID().toString();
    private static final String TRANSACTION_ID = UUID.randomUUID().toString();

    @Mock
    private TransactionService transactionService;

    @Mock
    private RegisteredEmailAddressMapper registeredEmailAddressMapper;

    @Mock
    private RegisteredEmailAddressRepository registeredEmailAddressRepository;

    @Mock
    private ValidationService validationService;

    @Mock
    private PrivateDataRetrievalService privateDataRetrievalService;

    @InjectMocks
    private RegisteredEmailAddressService registeredEmailAddressService;

    @Captor
    private ArgumentCaptor<Transaction> transactionApiCaptor;

    @Test
    void testCreateRegisteredEmailAddressIsSuccessful() throws ServiceException, NoExistingEmailAddressException, SubmissionAlreadyExistsException {
        Transaction transaction = buildTransaction();
        RegisteredEmailAddressDTO registeredEmailAddressDTO = buildRegisteredEmailAddressDTO();
        RegisteredEmailAddressResponseDTO registeredEmailAddressResponseDTO = buildRegisteredEmailAddressResponsesDTO();
        RegisteredEmailAddressDAO registeredEmailAddressDAO = buildRegisteredEmailAddressDAO();

        when(registeredEmailAddressMapper.dtoToDao(any())).thenReturn(registeredEmailAddressDAO);
        when(registeredEmailAddressMapper.daoToDto(any())).thenReturn(registeredEmailAddressResponseDTO);
        when(registeredEmailAddressRepository.insert(registeredEmailAddressDAO)).thenReturn(registeredEmailAddressDAO);

        RegisteredEmailAddressJson registeredEmailAddressJson = buildRegisteredEmailAddressJson(COMPANY_EMAIL);
        when(privateDataRetrievalService.getRegisteredEmailAddress(COMPANY_NUMBER)).thenReturn(registeredEmailAddressJson);

        RegisteredEmailAddressResponseDTO response = registeredEmailAddressService.createRegisteredEmailAddress(transaction,
                registeredEmailAddressDTO,
                REQUEST_ID,
                USER_ID);

        assertEquals(SUBMISSION_ID, response.getId());

        verify(registeredEmailAddressMapper, times(1)).dtoToDao(any());
        verify(transactionService, times(1)).updateTransaction(transactionApiCaptor.capture(), any());

        String submissionUri = String.format("/transactions/%s/registered-email-address", transaction.getId());

        String expectedReference = String.format("UpdateRegisteredEmailAddressReference_%s", response.getId());
        Transaction transactionSent = transactionApiCaptor.getValue();
        assertEquals(expectedReference, transactionSent.getReference());
        assertEquals(submissionUri, transactionSent.getResources().get(submissionUri).getLinks().get("resource"));
        assertEquals(submissionUri + "/validation-status", transactionSent.getResources()
                .get(submissionUri).getLinks().get("validation_status"));

        verify(registeredEmailAddressMapper, times(1)).daoToDto(any());
    }

    @Test
    void testCreateRegisteredEmailAddressFailsCompanyHasNoEmailAddress() throws ServiceException {

        Transaction transaction = buildTransaction();

        RegisteredEmailAddressDTO registeredEmailAddressDTO = buildRegisteredEmailAddressDTO();

        RegisteredEmailAddressJson registeredEmailAddressJson = buildRegisteredEmailAddressJson(null);
        when(privateDataRetrievalService.getRegisteredEmailAddress(COMPANY_NUMBER)).thenReturn(registeredEmailAddressJson);

        try {
            registeredEmailAddressService.createRegisteredEmailAddress(transaction,
                    registeredEmailAddressDTO,
                    REQUEST_ID,
                    USER_ID);
            fail();
        } catch (Exception e) {
            assertEquals(NoExistingEmailAddressException.class, e.getClass());
            assertEquals(String.format("Transaction id: %s; company number: %s has no existing Registered Email Address",
                            TRANSACTION_ID, COMPANY_NUMBER), e.getMessage());
        }

    }

    @Test
    void testCreateRegisteredEmailAddressFailsTransactionAlreadyHasEmailAddress() throws ServiceException {

        Transaction transaction = buildTransaction();
        Resource resource = new Resource();
        resource.setKind(FILING_KIND);
        Map<String, Resource> resourceMap = new HashMap<>();
        resourceMap.put("test", resource);
        transaction.setResources(resourceMap);

        RegisteredEmailAddressDTO registeredEmailAddressDTO = buildRegisteredEmailAddressDTO();

        RegisteredEmailAddressJson registeredEmailAddressJson = buildRegisteredEmailAddressJson(COMPANY_EMAIL);
        when(privateDataRetrievalService.getRegisteredEmailAddress(COMPANY_NUMBER)).thenReturn(registeredEmailAddressJson);

        try {
            registeredEmailAddressService.createRegisteredEmailAddress(transaction,
                    registeredEmailAddressDTO,
                    REQUEST_ID,
                    USER_ID);
            fail();
        } catch (Exception e) {
            assertEquals(SubmissionAlreadyExistsException.class, e.getClass());
            assertEquals(e.getMessage(),
                    String.format("Transaction id: %s has an existing Registered Email Address submission",
                            TRANSACTION_ID));
        }

    }

    @Test
    void testUpdateRegisteredEmailAddressIsSuccessful() throws ServiceException, NotFoundException, NoExistingEmailAddressException, SubmissionAlreadyExistsException, TransactionNotOpenException {
        Transaction transaction = buildTransaction();
        transaction.setStatus(OPEN);

        String newEmail = "Update@Test.com";

        RegisteredEmailAddressDAO registeredEmailAddressDAO = buildRegisteredEmailAddressDAO();
        RegisteredEmailAddressDTO registeredEmailAddressDTO = buildRegisteredEmailAddressDTO();

        RegisteredEmailAddressResponseDTO registeredEmailAddressResponseDTO = buildRegisteredEmailAddressResponsesDTO();
        registeredEmailAddressResponseDTO.getData().setRegisteredEmailAddress(newEmail);

        when(registeredEmailAddressRepository.findByTransactionId(transaction.getId())).thenReturn(registeredEmailAddressDAO);
        registeredEmailAddressDAO.setUpdatedAt(LocalDateTime.now());
        registeredEmailAddressDAO.getData()
                .setRegisteredEmailAddress(newEmail);
        registeredEmailAddressDAO.getData().setAcceptAppropriateEmailAddressStatement(false);
        when(registeredEmailAddressRepository.save(registeredEmailAddressDAO)).thenReturn(registeredEmailAddressDAO);
        when(registeredEmailAddressMapper.daoToDto(any())).thenReturn(registeredEmailAddressResponseDTO);

        RegisteredEmailAddressJson registeredEmailAddressJson = buildRegisteredEmailAddressJson(COMPANY_EMAIL);
        when(privateDataRetrievalService.getRegisteredEmailAddress(COMPANY_NUMBER)).thenReturn(registeredEmailAddressJson);

        RegisteredEmailAddressResponseDTO response = registeredEmailAddressService.updateRegisteredEmailAddress(transaction,
                registeredEmailAddressDTO,
                REQUEST_ID,
                USER_ID);

        assertEquals(SUBMISSION_ID, response.getId());
        assertEquals(newEmail, response.getData().getRegisteredEmailAddress());

        verify(registeredEmailAddressMapper, times(1)).daoToDto(any());
    }

    @Test
    void testUpdateRegisteredEmailAddressFailsCompanyHasNoEmailAddress() throws ServiceException {
        Transaction transaction = buildTransaction();
        transaction.setStatus(OPEN);

        String newEmail = "Update@Test.com";

        RegisteredEmailAddressDAO registeredEmailAddressDAO = buildRegisteredEmailAddressDAO();

        RegisteredEmailAddressResponseDTO registeredEmailAddressResponseDTO = buildRegisteredEmailAddressResponsesDTO();
        registeredEmailAddressResponseDTO.getData().setRegisteredEmailAddress(newEmail);

        when(registeredEmailAddressRepository.findByTransactionId(transaction.getId())).thenReturn(registeredEmailAddressDAO);
        registeredEmailAddressDAO.setUpdatedAt(LocalDateTime.now());
        registeredEmailAddressDAO.getData()
                .setRegisteredEmailAddress(newEmail);
        registeredEmailAddressDAO.getData().setAcceptAppropriateEmailAddressStatement(false);

        RegisteredEmailAddressJson registeredEmailAddressJson = buildRegisteredEmailAddressJson("");
        when(privateDataRetrievalService.getRegisteredEmailAddress(COMPANY_NUMBER)).thenReturn(registeredEmailAddressJson);

        try {
            registeredEmailAddressService.updateRegisteredEmailAddress(transaction,
                    buildRegisteredEmailAddressDTO(),
                    REQUEST_ID,
                    USER_ID);
            fail();
        } catch (Exception e) {
            assertEquals(NoExistingEmailAddressException.class, e.getClass());
            assertEquals(format("Transaction id: %s; company number: %s has no existing Registered Email Address",
                    TRANSACTION_ID, COMPANY_NUMBER), e.getMessage());

        }
    }

    @Test
    void testUpdateRegisteredEmailAddressFailsTransactionClosed() {
        Transaction transaction = buildTransaction();
        transaction.setStatus(CLOSED);

        try {
            registeredEmailAddressService.updateRegisteredEmailAddress(transaction,
                    buildRegisteredEmailAddressDTO(),
                    REQUEST_ID,
                    USER_ID);
            fail();
        } catch (Exception e) {
            assertEquals(TransactionNotOpenException.class, e.getClass());
            assertEquals(e.getMessage(),
                    format("Transaction %s can only be edited when status is %s ",
                            transaction.getId(), OPEN));
        }
    }

    @Test
    void testUpdateRegisteredEmailAddressFailsInvalidTransaction() {
        Transaction transaction = new Transaction();
        try {
            registeredEmailAddressService.updateRegisteredEmailAddress(transaction,
                    buildRegisteredEmailAddressDTO(),
                    REQUEST_ID,
                    USER_ID);
            fail();
        } catch (Exception e) {
            assertEquals(ServiceException.class, e.getClass());
            assertEquals(e.getMessage(), format("Transaction %s invalid", "null"));
        }
    }

    @Test
    void getValidationStatusIsSuccessful() throws NotFoundException {
        RegisteredEmailAddressDAO registeredEmailAddressDAO = buildRegisteredEmailAddressDAO();
        ValidationStatusResponse validationStatusResponse = new ValidationStatusResponse();
        validationStatusResponse.setValid(true);

        when(registeredEmailAddressRepository.findByTransactionId(TRANSACTION_ID))
                .thenReturn(registeredEmailAddressDAO);
        when(validationService.validateRegisteredEmailAddress(registeredEmailAddressDAO, REQUEST_ID))
                .thenReturn(validationStatusResponse);

        ValidationStatusResponse response = registeredEmailAddressService
                .getValidationStatus(TRANSACTION_ID, REQUEST_ID);

        assertTrue(response.isValid());
        assertNull(response.getValidationStatusError());

        verify(registeredEmailAddressRepository, times(1))
                .findByTransactionId(TRANSACTION_ID);
        verify(validationService, times(1))
                .validateRegisteredEmailAddress(registeredEmailAddressDAO, REQUEST_ID);
    }

    @Test
    void getValidationStatusIsUnSuccessful() {
        when(registeredEmailAddressRepository.findByTransactionId(TRANSACTION_ID))
                .thenThrow(new NullPointerException());

        try {
            registeredEmailAddressService
                    .getValidationStatus(TRANSACTION_ID, REQUEST_ID);
            fail();
        } catch (Exception e) {
            assertEquals(NotFoundException.class, e.getClass());
            assertEquals(String.format("Registered Email Address for TransactionId : %s Not Found", TRANSACTION_ID),
                    e.getMessage());

        }

        verify(registeredEmailAddressRepository, times(1)).findByTransactionId(TRANSACTION_ID);
        verify(validationService, times(0)).validateRegisteredEmailAddress(any(), any());

    }

    @Test
    void getRegisteredEmailAddressIsSuccessful() throws NotFoundException {
        RegisteredEmailAddressDAO registeredEmailAddressDAO = buildRegisteredEmailAddressDAO();
        RegisteredEmailAddressResponseDTO registeredEmailAddressResponseDTO = buildRegisteredEmailAddressResponsesDTO();
        ValidationStatusResponse validationStatusResponse = new ValidationStatusResponse();
        validationStatusResponse.setValid(true);

        when(registeredEmailAddressRepository.findByTransactionId(TRANSACTION_ID))
                .thenReturn(registeredEmailAddressDAO);
        when(registeredEmailAddressMapper.daoToDto(any())).thenReturn(registeredEmailAddressResponseDTO);


        RegisteredEmailAddressResponseDTO response = registeredEmailAddressService
                .getRegisteredEmailAddress(TRANSACTION_ID, REQUEST_ID);

        assertEquals("test@Test.com", response.getData().getRegisteredEmailAddress());

        verify(registeredEmailAddressRepository).findByTransactionId(TRANSACTION_ID);
    }

    @Test
    void getRegisteredEmailAddressIsUnSuccessfulREANotCreated() {
        when(registeredEmailAddressRepository.findByTransactionId(TRANSACTION_ID))
                .thenReturn(null);

        try {
            registeredEmailAddressService.getRegisteredEmailAddress(TRANSACTION_ID, REQUEST_ID);
            fail();
        } catch (Exception e) {
            assertEquals(NotFoundException.class, e.getClass());
            assertEquals(format("Registered Email Address for TransactionId : %s Not Found", TRANSACTION_ID), e.getMessage());
        }

        verify(registeredEmailAddressRepository, times(1)).findByTransactionId(TRANSACTION_ID);
    }

    private Transaction buildTransaction() {
        Transaction transaction = new Transaction();
        transaction.setId(TRANSACTION_ID);
        transaction.setCompanyNumber(COMPANY_NUMBER);
        return transaction;
    }

    private RegisteredEmailAddressDTO buildRegisteredEmailAddressDTO() {
        RegisteredEmailAddressDTO registeredEmailAddressDTO = new RegisteredEmailAddressDTO();
        registeredEmailAddressDTO.setRegisteredEmailAddress("test@Test.com");
        registeredEmailAddressDTO.setAcceptAppropriateEmailAddressStatement(true);
        return registeredEmailAddressDTO;
    }

    private RegisteredEmailAddressDAO buildRegisteredEmailAddressDAO() {
        RegisteredEmailAddressData registeredEmailAddressData = new RegisteredEmailAddressData();
        registeredEmailAddressData.setRegisteredEmailAddress("test@Test.com");
        registeredEmailAddressData.setAcceptAppropriateEmailAddressStatement(false);
        RegisteredEmailAddressDAO registeredEmailAddressDAO = new RegisteredEmailAddressDAO();
        registeredEmailAddressDAO.setData(registeredEmailAddressData);
        registeredEmailAddressDAO.setId(SUBMISSION_ID);
        registeredEmailAddressDAO.setTransactionId(TRANSACTION_ID);
        return registeredEmailAddressDAO;
    }

    private RegisteredEmailAddressResponseDTO buildRegisteredEmailAddressResponsesDTO() {
        RegisteredEmailAddressResponseData registeredEmailAddressData = new RegisteredEmailAddressResponseData();
        registeredEmailAddressData.setRegisteredEmailAddress("test@Test.com");
        RegisteredEmailAddressResponseDTO registeredEmailAddressResponseDTO = new RegisteredEmailAddressResponseDTO();
        registeredEmailAddressResponseDTO.setData(registeredEmailAddressData);
        registeredEmailAddressResponseDTO.setId(SUBMISSION_ID);
        return registeredEmailAddressResponseDTO;
    }

    private RegisteredEmailAddressJson buildRegisteredEmailAddressJson(String companyEmail) {
        RegisteredEmailAddressJson response = new RegisteredEmailAddressJson();
        response.setRegisteredEmailAddress(companyEmail);
        return response;
    }

}
