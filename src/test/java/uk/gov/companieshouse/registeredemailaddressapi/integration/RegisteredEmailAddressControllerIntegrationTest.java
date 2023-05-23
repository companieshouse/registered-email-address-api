package uk.gov.companieshouse.registeredemailaddressapi.integration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.registeredemailaddressapi.integration.utils.Helper;
import uk.gov.companieshouse.registeredemailaddressapi.interceptor.UserAuthenticationInterceptor;
import uk.gov.companieshouse.registeredemailaddressapi.model.dao.RegisteredEmailAddressDAO;
import uk.gov.companieshouse.registeredemailaddressapi.model.dto.RegisteredEmailAddressDTO;
import uk.gov.companieshouse.registeredemailaddressapi.repository.RegisteredEmailAddressRepository;
import uk.gov.companieshouse.registeredemailaddressapi.service.TransactionService;

import static java.lang.String.format;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.companieshouse.api.model.transaction.TransactionStatus.CLOSED;
import static uk.gov.companieshouse.api.model.transaction.TransactionStatus.OPEN;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
public class RegisteredEmailAddressControllerIntegrationTest {

    Helper helper = new Helper();

    @Autowired
    private MockMvc   mvc;

    @MockBean
    protected RegisteredEmailAddressRepository registeredEmailAddressRepository;

    @MockBean
    protected TransactionService transactionService;

    @MockBean
    protected UserAuthenticationInterceptor userAuthenticationInterceptor;


    @Test
    public void testCreateRegisteredEmailAddressSuccessTest() throws Exception {
        String email = "Test@Test.com";
        Transaction transaction = helper.generateTransaction();
        RegisteredEmailAddressDTO registeredEmailAddressDTO = helper.generateRegisteredEmailAddressDTO(email);
        RegisteredEmailAddressDAO registeredEmailAddressDAO = helper
                .generateRegisteredEmailAddressDAO(email, transaction.getId());

        when(transactionService.getTransaction(any(), any(), any())).thenReturn(transaction);
        when(userAuthenticationInterceptor.preHandle(any(), any(), any())).thenReturn(true);
        when(registeredEmailAddressRepository.insert(any(RegisteredEmailAddressDAO.class)))
                .thenReturn(registeredEmailAddressDAO);

        mvc.perform(post("/transactions/" + transaction.getId() + "/registered-email-address")
                        .contentType("application/json").header("ERIC-Identity", "123")
                        .header("X-Request-Id", "123456").content(helper.writeToJson(registeredEmailAddressDTO)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.registered_email_address").value("Test@Test.com"));
    }

    @Test
    public void testCreateRegisteredEmailAddressFailureTest() throws Exception {

        RegisteredEmailAddressDTO registeredEmailAddressDTO = helper.generateRegisteredEmailAddressDTO(null);
        Transaction transaction = helper.generateTransaction();

        when(userAuthenticationInterceptor.preHandle(any(), any(), any())).thenReturn(true);
        when(transactionService.getTransaction(any(), any(), any())).thenReturn(transaction);
        when(userAuthenticationInterceptor.preHandle(any(), any(), any())).thenReturn(true);

        this.mvc.perform(post("/transactions/" + transaction.getId() + "/registered-email-address")
                        .contentType("application/json").header("ERIC-Identity", "123")
                        .header("X-Request-Id", "123456")
                        .content(helper.writeToJson(registeredEmailAddressDTO)))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]")
                        .value("registered_email_address must not be blank"));
    }

    @Test
    public void testCreateRegisteredEmailAddressRegexFailureTest() throws Exception {
        Transaction transaction = helper.generateTransaction();
        RegisteredEmailAddressDTO registeredEmailAddressDTO = helper.generateRegisteredEmailAddressDTO("223j&kg");

        when(transactionService.getTransaction(any(), any(), any())).thenReturn(transaction);
        when(userAuthenticationInterceptor.preHandle(any(), any(), any())).thenReturn(true);

        this.mvc.perform(post("/transactions/" + transaction.getId() + "/registered-email-address")
                        .contentType("application/json").header("ERIC-Identity", "123")
                        .header("X-Request-Id", "123456")
                        .content(helper.writeToJson(registeredEmailAddressDTO)))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]")
                        .value("registered_email_address must have a valid email format"));
    }

    @Test
    public void testUpdateRegisteredEmailAddressSuccessfulTest() throws Exception {


        String email = "UpdateTest@Test.com";
        Transaction transaction = helper.generateTransaction();
        transaction.setStatus(OPEN);
        RegisteredEmailAddressDTO registeredEmailAddressDTO = helper.generateRegisteredEmailAddressDTO(email);
        RegisteredEmailAddressDAO registeredEmailAddressDAO = helper
                .generateRegisteredEmailAddressDAO(email, transaction.getId());

        when(transactionService.getTransaction(any(), any(), any())).thenReturn(transaction);
        when(userAuthenticationInterceptor.preHandle(any(), any(), any())).thenReturn(true);
        when(registeredEmailAddressRepository.findByTransactionId(transaction.getId()))
                .thenReturn(registeredEmailAddressDAO);
        when(registeredEmailAddressRepository.save(any(RegisteredEmailAddressDAO.class)))
                .thenReturn(registeredEmailAddressDAO);

        mvc.perform(put("/transactions/" + transaction.getId() + "/registered-email-address")
                        .contentType("application/json").header("ERIC-Identity", "123")
                        .header("X-Request-Id", "123456").content(helper.writeToJson(registeredEmailAddressDTO)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.registered_email_address").value("UpdateTest@Test.com"));
    }

    //Test Update End points
    @Test
    public void testUpdateRegisteredEmailAddressIncorrectStatusTest() throws Exception {


        String email = "UpdateTest@Test.com";
        Transaction transaction = helper.generateTransaction();
        transaction.setStatus(CLOSED);
        RegisteredEmailAddressDTO registeredEmailAddressDTO = helper.generateRegisteredEmailAddressDTO(email);
        RegisteredEmailAddressDAO registeredEmailAddressDAO = helper
                .generateRegisteredEmailAddressDAO(email, transaction.getId());

        when(transactionService.getTransaction(any(), any(), any())).thenReturn(transaction);
        when(userAuthenticationInterceptor.preHandle(any(), any(), any())).thenReturn(true);
        when(registeredEmailAddressRepository.findByTransactionId(transaction.getId()))
                .thenReturn(registeredEmailAddressDAO);
        when(registeredEmailAddressRepository.save(any(RegisteredEmailAddressDAO.class)))
                .thenReturn(registeredEmailAddressDAO);

        mvc.perform(put("/transactions/" + transaction.getId() + "/registered-email-address")
                        .contentType("application/json").header("ERIC-Identity", "123")
                        .header("X-Request-Id", "123456").content(helper.writeToJson(registeredEmailAddressDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$")
                        .value(format("Transaction %s can only be edited when status is OPEN",
                                transaction.getId())));
    }



    // Test ValidationStatus Endpoints
    @Test
    public void testGetValidationStatusTest() throws Exception {
        Transaction transaction = helper.generateTransaction();
        String email = "Test@Test.com";
        RegisteredEmailAddressDAO registeredEmailAddressDAO = helper
                .generateRegisteredEmailAddressDAO(email, transaction.getId());


        when(transactionService.getTransaction(any(), any(), any())).thenReturn(transaction);
        when(userAuthenticationInterceptor.preHandle(any(), any(), any())).thenReturn(true);
        when(registeredEmailAddressRepository.findByTransactionId(transaction.getId()))
                .thenReturn(registeredEmailAddressDAO);

        this.mvc.perform(get("/transactions/" + transaction.getId() + "/registered-email-address/validation-status")
                        .contentType("application/json").header("ERIC-Identity", "123")
                        .header("X-Request-Id", "123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.is_valid").value(true));

    }

    @Test
    public void testGetValidationStatusFailureTest() throws Exception {
        Transaction transaction = helper.generateTransaction();

        when(transactionService.getTransaction(any(), any(), any())).thenReturn(transaction);
        when(userAuthenticationInterceptor.preHandle(any(), any(), any())).thenReturn(true);

        this.mvc.perform(get("/transactions/" + transaction.getId() + "/registered-email-address/validation-status")
                        .contentType("application/json").header("ERIC-Identity", "123")
                        .header("X-Request-Id", "123456"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$")
                        .value(String.format("Registered Email Address for TransactionId : %s Not Found", transaction.getId())));
    }

}