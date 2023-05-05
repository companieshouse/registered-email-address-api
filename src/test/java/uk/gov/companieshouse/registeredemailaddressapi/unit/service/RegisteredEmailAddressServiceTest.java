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
import uk.gov.companieshouse.registeredemailaddressapi.exception.ServiceException;
import uk.gov.companieshouse.registeredemailaddressapi.mapper.RegisteredEmailAddressMapper;
import uk.gov.companieshouse.registeredemailaddressapi.model.dao.RegisteredEmailAddressDAO;
import uk.gov.companieshouse.registeredemailaddressapi.model.dto.RegisteredEmailAddressDTO;
import uk.gov.companieshouse.registeredemailaddressapi.repository.RegisteredEmailAddressRepository;
import uk.gov.companieshouse.registeredemailaddressapi.service.RegisteredEmailAddressService;
import uk.gov.companieshouse.registeredemailaddressapi.service.TransactionService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static uk.gov.companieshouse.registeredemailaddressapi.utils.Constants.FILING_KIND_REGISTERED_EMAIL_ADDRESS;

@ExtendWith(MockitoExtension.class)
class RegisteredEmailAddressServiceTest {

    private static final String REQUEST_ID = UUID.randomUUID().toString();
    private static final String USER_ID = UUID.randomUUID().toString();
    private static final String SUBMISSION_ID = UUID.randomUUID().toString();
    private static final String TRANSACTION_ID = UUID.randomUUID().toString();

    private RegisteredEmailAddressDTO submissionDao = new RegisteredEmailAddressDTO();
    private RegisteredEmailAddressDAO submissionDto = new RegisteredEmailAddressDAO();

    @Mock
    private TransactionService transactionService;

    @Mock
    private RegisteredEmailAddressMapper registeredEmailAddressMapper;

    @Mock
    private RegisteredEmailAddressRepository registeredEmailAddressRepository;

    @InjectMocks
    private RegisteredEmailAddressService registeredEmailAddressService;

    @Captor
    private ArgumentCaptor<Transaction> transactionApiCaptor;

    @Test
    void testCreateRegisteredEmailAddressIsSuccessful() throws ServiceException {
        Transaction transaction = buildTransaction();
        RegisteredEmailAddressDTO registeredEmailAddressDTO = buildRegisteredEmailAddressDTO();
        RegisteredEmailAddressDAO registeredEmailAddressDAO = buildRegisteredEmailAddressDAO();

        when(registeredEmailAddressMapper.dtoToDao(any())).thenReturn(registeredEmailAddressDAO);
        when(registeredEmailAddressMapper.daoToDto(any())).thenReturn(registeredEmailAddressDTO);
        when(registeredEmailAddressRepository.insert(registeredEmailAddressDAO)).thenReturn(registeredEmailAddressDAO);

        RegisteredEmailAddressDTO response = registeredEmailAddressService.createRegisteredEmailAddress(transaction,
                registeredEmailAddressDTO,
                REQUEST_ID,
                USER_ID);

        assertEquals(SUBMISSION_ID, response.getId());

        verify(registeredEmailAddressMapper, times(1)).dtoToDao(any());
        verify(transactionService, times(1)).updateTransaction(transactionApiCaptor.capture(), any());

        String submissionUri = String.format("/transactions/%s/registered-email-address/%s", transaction.getId(),
                registeredEmailAddressDAO.getId());

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
        resource.setKind(FILING_KIND_REGISTERED_EMAIL_ADDRESS);
        Map<String, Resource> resourceMap = new HashMap<>();
        resourceMap.put("test", resource);
        transaction.setResources(resourceMap);

        RegisteredEmailAddressDTO registeredEmailAddressDTO = buildRegisteredEmailAddressDTO();
        try {
            RegisteredEmailAddressDTO response = registeredEmailAddressService.createRegisteredEmailAddress(transaction,
                    registeredEmailAddressDTO,
                    REQUEST_ID,
                    USER_ID);
        }
        catch (Exception e){
            assertEquals(e.getMessage(),
                    String.format("Transaction id: %s has an existing Registered Email Address submission",
                            TRANSACTION_ID));
        }

    }

    private Transaction buildTransaction() {
        Transaction transaction = new Transaction();
        transaction.setId(TRANSACTION_ID);
        return transaction;
    }

    private RegisteredEmailAddressDTO buildRegisteredEmailAddressDTO() {
        RegisteredEmailAddressDTO registeredEmailAddressDTO = new RegisteredEmailAddressDTO();
        registeredEmailAddressDTO.setRegisteredEmailAddress("test@Test.com");
        registeredEmailAddressDTO.setId(SUBMISSION_ID);
        return registeredEmailAddressDTO;
    }

    private RegisteredEmailAddressDAO buildRegisteredEmailAddressDAO() {
        RegisteredEmailAddressDAO registeredEmailAddressDAO = new RegisteredEmailAddressDAO();
        registeredEmailAddressDAO.setRegisteredEmailAddress("test@Test.com");
        registeredEmailAddressDAO.setId(SUBMISSION_ID);
        return registeredEmailAddressDAO;
    }


}