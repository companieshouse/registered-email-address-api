package uk.gov.companieshouse.registeredemailaddressapi.integration;

import static java.lang.String.format;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.companieshouse.api.model.transaction.TransactionStatus.CLOSED;
import static uk.gov.companieshouse.api.model.transaction.TransactionStatus.OPEN;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.model.company.RegisteredEmailAddressJson;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.registeredemailaddressapi.exception.ServiceException;
import uk.gov.companieshouse.registeredemailaddressapi.integration.utils.Helper;
import uk.gov.companieshouse.registeredemailaddressapi.interceptor.UserAuthenticationInterceptor;
import uk.gov.companieshouse.registeredemailaddressapi.model.dao.RegisteredEmailAddressDAO;
import uk.gov.companieshouse.registeredemailaddressapi.model.dto.RegisteredEmailAddressDTO;
import uk.gov.companieshouse.registeredemailaddressapi.repository.RegisteredEmailAddressRepository;
import uk.gov.companieshouse.registeredemailaddressapi.service.CompanyProfileService;
import uk.gov.companieshouse.registeredemailaddressapi.service.PrivateEmailDataRetrievalService;
import uk.gov.companieshouse.registeredemailaddressapi.service.TransactionService;

@SpringBootTest
@AutoConfigureMockMvc
class RegisteredEmailAddressControllerIntegrationTest {

    Helper helper = new Helper();

    @Autowired
    private MockMvc mvc;

    @MockBean
    protected RegisteredEmailAddressRepository registeredEmailAddressRepository;

    @MockBean
    protected TransactionService transactionService;

    @MockBean
    @Qualifier("oracleQueryApiDataRetrievalServiceImpl")
    protected PrivateEmailDataRetrievalService privateEmailDataRetrievalService;

    @MockBean
    protected UserAuthenticationInterceptor userAuthenticationInterceptor;

    @MockBean
    protected CompanyProfileService companyProfileService;


    @Test
    void testCreateRegisteredEmailAddressSuccessTest() throws Exception {
        String email = "Test@Test.com";
        String companyNumber = "123456";

        Transaction transaction = helper.generateTransaction(companyNumber);
        RegisteredEmailAddressDTO registeredEmailAddressDTO = helper.generateRegisteredEmailAddressDTO(email);
        RegisteredEmailAddressDAO registeredEmailAddressDAO = helper.generateRegisteredEmailAddressDAO(email, transaction.getId());

        CompanyProfileApi companyProfileApi = helper.generateCompanyProfileApi(companyNumber);

        when(transactionService.getTransaction(any(), any(), any())).thenReturn(transaction);
        when(userAuthenticationInterceptor.preHandle(any(), any(), any())).thenReturn(true);
        when(registeredEmailAddressRepository.insert(any(RegisteredEmailAddressDAO.class))).thenReturn(registeredEmailAddressDAO);
        when(companyProfileService.getCompanyProfile(companyNumber)).thenReturn(companyProfileApi);

        RegisteredEmailAddressJson emailResponse = helper.generateRegisteredEmailAddressJson(email);
        when(privateEmailDataRetrievalService.getRegisteredEmailAddress(companyNumber)).thenReturn(emailResponse);

        mvc.perform(post("/transactions/" + transaction.getId() + "/registered-email-address")
                        .contentType("application/json").header("ERIC-Identity", "123")
                        .header("X-Request-Id", "123456").content(helper.writeToJson(registeredEmailAddressDTO)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.data.registered_email_address").value("Test@Test.com"))
                .andExpect(jsonPath("$.data.accept_appropriate_email_address_statement").value(true));

    }

    @Test
    void testCreateRegisteredEmailAddressEmptyEmailFailureTest() throws Exception {

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
    void testCreateRegisteredEmailAddressRegexFailureTest() throws Exception {
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
    void testCreateRegisteredEmailFailureNoExistingEmailAddress() throws Exception {
        String email = "Test@Test.com";
        String companyNumber = "123456";

        Transaction transaction = helper.generateTransaction(companyNumber);
        RegisteredEmailAddressDTO registeredEmailAddressDTO = helper.generateRegisteredEmailAddressDTO(email);
        RegisteredEmailAddressDAO registeredEmailAddressDAO = helper
                .generateRegisteredEmailAddressDAO(email, transaction.getId());

        CompanyProfileApi companyProfileApi = helper.generateCompanyProfileApi(companyNumber);
        when(companyProfileService.getCompanyProfile(companyNumber)).thenReturn(companyProfileApi);

        when(transactionService.getTransaction(any(), any(), any())).thenReturn(transaction);
        when(userAuthenticationInterceptor.preHandle(any(), any(), any())).thenReturn(true);
        when(registeredEmailAddressRepository.insert(any(RegisteredEmailAddressDAO.class))).thenReturn(registeredEmailAddressDAO);

        RegisteredEmailAddressJson emailResponse = helper.generateRegisteredEmailAddressJson(null);
        when(privateEmailDataRetrievalService.getRegisteredEmailAddress(companyNumber)).thenReturn(emailResponse);

        this.mvc.perform(post("/transactions/" + transaction.getId() + "/registered-email-address")
                        .contentType("application/json").header("ERIC-Identity", "123")
                        .header("X-Request-Id", "123456")
                        .content(helper.writeToJson(registeredEmailAddressDTO)))

                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("Transaction id: ")))
                .andExpect(content().string(containsString("; company number: ")))
                .andExpect(content().string(containsString(" has no existing Registered Email Address")));
    }

    @Test
    void testCreateRegisteredEmailFailureSubmissionAlreadyExistsAddress() throws Exception {
        String email = "Test@Test.com";
        String companyNumber = "123456";

        Transaction transaction = helper.generateTransaction(companyNumber);
        RegisteredEmailAddressDTO registeredEmailAddressDTO = helper.generateRegisteredEmailAddressDTO(email);
        RegisteredEmailAddressDAO registeredEmailAddressDAO = helper
                .generateRegisteredEmailAddressDAO(email, transaction.getId());

        CompanyProfileApi companyProfileApi = helper.generateCompanyProfileApi(companyNumber);

        when(transactionService.getTransaction(any(), any(), any())).thenReturn(transaction);
        when(userAuthenticationInterceptor.preHandle(any(), any(), any())).thenReturn(true);
        when(registeredEmailAddressRepository.insert(any(RegisteredEmailAddressDAO.class))).thenReturn(registeredEmailAddressDAO);
        when(companyProfileService.getCompanyProfile(companyNumber)).thenReturn(companyProfileApi);

        RegisteredEmailAddressJson emailResponse = helper.generateRegisteredEmailAddressJson(email);
        when(privateEmailDataRetrievalService.getRegisteredEmailAddress(companyNumber)).thenReturn(emailResponse);

        // Create an existing submission
        mvc.perform(post("/transactions/" + transaction.getId() + "/registered-email-address")
                        .contentType("application/json").header("ERIC-Identity", "123")
                        .header("X-Request-Id", "123456").content(helper.writeToJson(registeredEmailAddressDTO)));

        // Test the failure case
        this.mvc.perform(post("/transactions/" + transaction.getId() + "/registered-email-address")
                        .contentType("application/json").header("ERIC-Identity", "123")
                        .header("X-Request-Id", "123456")
                        .content(helper.writeToJson(registeredEmailAddressDTO)))

                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("Transaction id: ")))
                .andExpect(content().string(containsString(" has an existing Registered Email Address submission")));
    }

    //Test Update End points
    @Test
    void testUpdateRegisteredEmailAddressSuccessfulTest() throws Exception {
        String email = "UpdateTest@Test.com";
        String companyNumber = "123456";

        Transaction transaction = helper.generateTransaction(companyNumber);
        transaction.setStatus(OPEN);
        RegisteredEmailAddressDTO registeredEmailAddressDTO = helper.generateRegisteredEmailAddressDTO(email);
        RegisteredEmailAddressDAO registeredEmailAddressDAO = helper.generateRegisteredEmailAddressDAO(email, transaction.getId());

        CompanyProfileApi companyProfileApi = helper.generateCompanyProfileApi(companyNumber);

        when(transactionService.getTransaction(any(), any(), any())).thenReturn(transaction);
        when(userAuthenticationInterceptor.preHandle(any(), any(), any())).thenReturn(true);
        when(registeredEmailAddressRepository.findByTransactionId(transaction.getId())).thenReturn(registeredEmailAddressDAO);
        when(registeredEmailAddressRepository.save(any(RegisteredEmailAddressDAO.class))).thenReturn(registeredEmailAddressDAO);
        when(companyProfileService.getCompanyProfile(companyNumber)).thenReturn(companyProfileApi);

        RegisteredEmailAddressJson emailResponse = helper.generateRegisteredEmailAddressJson(email);
        when(privateEmailDataRetrievalService.getRegisteredEmailAddress(companyNumber)).thenReturn(emailResponse);

        mvc.perform(put("/transactions/" + transaction.getId() + "/registered-email-address")
                        .contentType("application/json").header("ERIC-Identity", "123")
                        .header("X-Request-Id", "123456").content(helper.writeToJson(registeredEmailAddressDTO)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.data.registered_email_address").value("UpdateTest@Test.com"))
                .andExpect(jsonPath("$.data.accept_appropriate_email_address_statement").value(true));

    }

    @Test
    void testUpdateRegisteredEmailAddressIncorrectStatusTest() throws Exception {


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
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$")
                        .value(format("Transaction %s can only be edited when status is OPEN",
                                transaction.getId())));
    }

    @Test
    void testUpdateRegisteredEmailAddressTransactionNotFound() throws Exception {


        String email = "UpdateTest@Test.com";
        String transactionId = "xxxxx-xxxxx-xxxxx";
        RegisteredEmailAddressDTO registeredEmailAddressDTO = helper.generateRegisteredEmailAddressDTO(email);
        RegisteredEmailAddressDAO registeredEmailAddressDAO = helper
                .generateRegisteredEmailAddressDAO(email, transactionId);

        when(transactionService.getTransaction(any(), any(), any())).thenThrow(new ServiceException("Error Retrieving Transaction " + transactionId));
        when(userAuthenticationInterceptor.preHandle(any(), any(), any())).thenReturn(true);
        when(registeredEmailAddressRepository.save(any(RegisteredEmailAddressDAO.class)))
                .thenReturn(registeredEmailAddressDAO);

        mvc.perform(put("/transactions/" + transactionId + "/registered-email-address")
                        .contentType("application/json").header("ERIC-Identity", "123")
                        .header("X-Request-Id", "123456").content(helper.writeToJson(registeredEmailAddressDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$")
                        .value("Error Retrieving Transaction " + transactionId));
    }

    @Test
    void testUpdateRegisteredEmailFailureNoExistingEmailAddress() throws Exception {
        String email = "UpdateTest@Test.com";
        String companyNumber = "123456";

        Transaction transaction = helper.generateTransaction(companyNumber);
        transaction.setStatus(OPEN);
        RegisteredEmailAddressDTO registeredEmailAddressDTO = helper.generateRegisteredEmailAddressDTO(email);
        RegisteredEmailAddressDAO registeredEmailAddressDAO = helper.generateRegisteredEmailAddressDAO(email, transaction.getId());

        CompanyProfileApi companyProfileApi = helper.generateCompanyProfileApi(companyNumber);
        when(companyProfileService.getCompanyProfile(companyNumber)).thenReturn(companyProfileApi);

        when(transactionService.getTransaction(any(), any(), any())).thenReturn(transaction);
        when(userAuthenticationInterceptor.preHandle(any(), any(), any())).thenReturn(true);
        when(registeredEmailAddressRepository.findByTransactionId(transaction.getId())).thenReturn(registeredEmailAddressDAO);
        when(registeredEmailAddressRepository.save(any(RegisteredEmailAddressDAO.class))).thenReturn(registeredEmailAddressDAO);

        RegisteredEmailAddressJson emailResponse = helper.generateRegisteredEmailAddressJson(null);
        when(privateEmailDataRetrievalService.getRegisteredEmailAddress(companyNumber)).thenReturn(emailResponse);

        mvc.perform(put("/transactions/" + transaction.getId() + "/registered-email-address")
                        .contentType("application/json").header("ERIC-Identity", "123")
                        .header("X-Request-Id", "123456").content(helper.writeToJson(registeredEmailAddressDTO)))

                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("Transaction id: ")))
                .andExpect(content().string(containsString("; company number: ")))
                .andExpect(content().string(containsString(" has no existing Registered Email Address")));
    }


    // Test ValidationStatus Endpoints
    @Test
    void testGetValidationStatusTest() throws Exception {
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
    void testGetValidationStatusTest_invalid() throws Exception {
        Transaction transaction = helper.generateTransaction();
        String email = "TestTest.com";
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
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.is_valid").value(false));

    }

    @Test
    void testGetValidationStatusFailureTest() throws Exception {
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
