package uk.gov.companieshouse.registeredemailaddressapi.integration;

import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.registeredemailaddressapi.integration.utils.Helper;
import uk.gov.companieshouse.registeredemailaddressapi.integration.utils.MongoDbConfig;
import uk.gov.companieshouse.registeredemailaddressapi.interceptor.UserAuthenticationInterceptor;
import uk.gov.companieshouse.registeredemailaddressapi.model.dto.RegisteredEmailAddressDTO;
import uk.gov.companieshouse.registeredemailaddressapi.service.TransactionService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
@Testcontainers
public class RegisteredEmailAddressControllerIntegrationTest extends MongoDbConfig {

    Helper helper = new Helper();

    @Autowired
    private MockMvc mvc;

    @MockBean
    protected TransactionService transactionService;

    @MockBean
    protected UserAuthenticationInterceptor userAuthenticationInterceptor;

    @AfterEach
    void cleanUp() {
        registeredEmailAddressRepository.deleteAll();
    }

    //Test createRegisteredEmailAddress endpoints

    @Test
    public void testCreateRegisteredEmailAddressSuccessTest() throws Exception {
        Transaction transaction = helper.generateTransaction();
        RegisteredEmailAddressDTO registeredEmailAddressDTO = helper.generateRegisteredEmailAddressDTO("Test@Test.com");

        when(transactionService.getTransaction(any(), any(), any())).thenReturn(transaction);
        when(userAuthenticationInterceptor.preHandle(any(), any(), any())).thenReturn(true);

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


    // Test ValidationStatus Endpoints
    @Test
    public void testGetValidationStatusTest() throws Exception {
        Transaction transaction = helper.generateTransaction();
        insertIntoDb(transaction.getId(), "Test@Test.com");

        when(transactionService.getTransaction(any(), any(), any())).thenReturn(transaction);
        when(userAuthenticationInterceptor.preHandle(any(), any(), any())).thenReturn(true);

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