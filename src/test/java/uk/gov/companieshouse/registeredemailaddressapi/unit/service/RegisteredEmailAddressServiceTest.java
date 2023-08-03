package uk.gov.companieshouse.registeredemailaddressapi.unit.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.transaction.Resource;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusResponse;
import uk.gov.companieshouse.registeredemailaddressapi.exception.ServiceException;
import uk.gov.companieshouse.registeredemailaddressapi.exception.SubmissionNotFoundException;
import uk.gov.companieshouse.registeredemailaddressapi.mapper.RegisteredEmailAddressMapper;
import uk.gov.companieshouse.registeredemailaddressapi.model.dao.RegisteredEmailAddressDAO;
import uk.gov.companieshouse.registeredemailaddressapi.model.dao.RegisteredEmailAddressData;
import uk.gov.companieshouse.registeredemailaddressapi.model.dto.RegisteredEmailAddressDTO;
import uk.gov.companieshouse.registeredemailaddressapi.model.dto.RegisteredEmailAddressResponseDTO;
import uk.gov.companieshouse.registeredemailaddressapi.model.dto.RegisteredEmailAddressResponseData;
import uk.gov.companieshouse.registeredemailaddressapi.repository.RegisteredEmailAddressRepository;
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

    @InjectMocks
    private RegisteredEmailAddressService registeredEmailAddressService;

    @Captor
    private ArgumentCaptor<Transaction> transactionApiCaptor;

    @Test
    void testCreateRegisteredEmailAddressIsSuccessful() throws ServiceException {
        Transaction transaction = buildTransaction();
        RegisteredEmailAddressDTO registeredEmailAddressDTO = buildRegisteredEmailAddressDTO();
        RegisteredEmailAddressResponseDTO registeredEmailAddressResponseDTO = buildRegisteredEmailAddressResponsesDTO();
        RegisteredEmailAddressDAO registeredEmailAddressDAO = buildRegisteredEmailAddressDAO();

        when(registeredEmailAddressMapper.dtoToDao(any())).thenReturn(registeredEmailAddressDAO);
        when(registeredEmailAddressMapper.daoToDto(any())).thenReturn(registeredEmailAddressResponseDTO);
        when(registeredEmailAddressRepository.insert(registeredEmailAddressDAO)).thenReturn(registeredEmailAddressDAO);

        RegisteredEmailAddressResponseDTO response = registeredEmailAddressService.createRegisteredEmailAddress(transaction,
                registeredEmailAddressDTO,
                REQUEST_ID,
                USER_ID);

        assertEquals(SUBMISSION_ID, response.getId());

        verify(registeredEmailAddressMapper, times(1)).dtoToDao(any());
        verify(transactionService, times(1)).updateTransaction(transactionApiCaptor.capture(), any());

        String submissionUri = String.format("/transactions/%s/registered-email-address", transaction.getId());

        Transaction transactionSent = transactionApiCaptor.getValue();
        assertEquals(submissionUri, transactionSent.getResources().get(submissionUri).getLinks().get("resource"));
        assertEquals(submissionUri + "/validation-status", transactionSent.getResources()
                .get(submissionUri).getLinks().get("validation_status"));

        verify(registeredEmailAddressMapper, times(1)).daoToDto(any());
    }

    @Test
    void testCreateRegisteredEmailAddressIsUnSuccessful() {

        Transaction transaction = buildTransaction();
        Resource resource = new Resource();
        resource.setKind(FILING_KIND);
        Map<String, Resource> resourceMap = new HashMap<>();
        resourceMap.put("test", resource);
        transaction.setResources(resourceMap);

        RegisteredEmailAddressDTO registeredEmailAddressDTO = buildRegisteredEmailAddressDTO();
        try {
            registeredEmailAddressService.createRegisteredEmailAddress(transaction,
                    registeredEmailAddressDTO,
                    REQUEST_ID,
                    USER_ID);
            fail();
        } catch (Exception e) {
            assertEquals(e.getMessage(),
                    String.format("Transaction id: %s has an existing Registered Email Address submission",
                            TRANSACTION_ID));
        }

    }

    @Test
    void testUpdateRegisteredEmailAddressIsSuccessful() throws ServiceException, SubmissionNotFoundException {
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
        when(registeredEmailAddressRepository.save(registeredEmailAddressDAO)).thenReturn(registeredEmailAddressDAO);
        when(registeredEmailAddressMapper.daoToDto(any())).thenReturn(registeredEmailAddressResponseDTO);

        RegisteredEmailAddressResponseDTO response = registeredEmailAddressService.updateRegisteredEmailAddress(transaction,
                registeredEmailAddressDTO,
                REQUEST_ID,
                USER_ID);

        assertEquals(SUBMISSION_ID, response.getId());
        assertEquals(newEmail, response.getData().getRegisteredEmailAddress());

        verify(registeredEmailAddressMapper, times(1)).daoToDto(any());
    }


    @Test
    void testUpdateRegisteredEmailAddressIsUnSuccessful() {
        Transaction transaction = buildTransaction();
        transaction.setStatus(CLOSED);

        try {
            registeredEmailAddressService.updateRegisteredEmailAddress(transaction,
                    buildRegisteredEmailAddressDTO(),
                    REQUEST_ID,
                    USER_ID);
            fail();
        } catch (Exception e) {
            assertEquals(e.getMessage(),
                    format("Transaction %s can only be edited when status is %s ",
                            transaction.getId(),
                            OPEN));

        }
    }

    @Test
    void getValidationStatusIsSuccessful() throws SubmissionNotFoundException {
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
        } catch (Exception ex) {
            assertEquals(String.format("Registered Email Address for TransactionId : %s Not Found", TRANSACTION_ID),
                    ex.getMessage());

        }

        verify(registeredEmailAddressRepository, times(1)).findByTransactionId(TRANSACTION_ID);
        verify(validationService, times(0)).validateRegisteredEmailAddress(any(), any());

    }

    @Test
    void getRegisteredEmailAddressIsSuccessful() throws SubmissionNotFoundException {
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
        } catch (Exception ex) {
            assertEquals(format("Registered Email Address for TransactionId : %s Not Found", TRANSACTION_ID), ex.getMessage());
        }

        verify(registeredEmailAddressRepository, times(1)).findByTransactionId(TRANSACTION_ID);
    }

    private Transaction buildTransaction() {
        Transaction transaction = new Transaction();
        transaction.setId(TRANSACTION_ID);
        return transaction;
    }

    private RegisteredEmailAddressDTO buildRegisteredEmailAddressDTO() {
        RegisteredEmailAddressDTO registeredEmailAddressDTO = new RegisteredEmailAddressDTO();
        registeredEmailAddressDTO.setRegisteredEmailAddress("test@Test.com");
        return registeredEmailAddressDTO;
    }

    private RegisteredEmailAddressDAO buildRegisteredEmailAddressDAO() {
        RegisteredEmailAddressData registeredEmailAddressData = new RegisteredEmailAddressData();
        registeredEmailAddressData.setRegisteredEmailAddress("test@Test.com");

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


}