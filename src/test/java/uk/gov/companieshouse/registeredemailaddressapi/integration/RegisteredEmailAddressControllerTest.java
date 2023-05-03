package uk.gov.companieshouse.registeredemailaddressapi.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.registeredemailaddressapi.interceptor.TransactionInterceptor;
import uk.gov.companieshouse.registeredemailaddressapi.model.dao.RegisteredEmailAddressDAO;
import uk.gov.companieshouse.registeredemailaddressapi.model.dto.RegisteredEmailAddressDTO;
import uk.gov.companieshouse.registeredemailaddressapi.repository.RegisteredEmailAddressRepository;
import uk.gov.companieshouse.registeredemailaddressapi.service.TransactionService;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class RegisteredEmailAddressControllerTest {


    //TODO - introduce test containers

    @MockBean
    protected TransactionService transactionService;
    @InjectMocks
    protected TransactionInterceptor transactionInterceptor;

    @MockBean
    RegisteredEmailAddressRepository registeredEmailAddressRepository;

    @Autowired
    private MockMvc mvc;



    @BeforeEach
    void setUp() throws Exception {

    }

    @Test
    public void testCreateRegisteredEmailAddressSuccessTest() throws Exception {
        Transaction transaction = new Transaction();
        String id = UUID.randomUUID().toString();
        transaction.setId(id);
        when(transactionService.getTransaction(any(), any(), any())).thenReturn(transaction);

        when(registeredEmailAddressRepository.insert(any(RegisteredEmailAddressDAO.class)))
                .thenReturn(getRegisteredEmailAddressDAO());

        RegisteredEmailAddressDTO registeredEmailAddressDTO = new RegisteredEmailAddressDTO();
        registeredEmailAddressDTO.setRegisteredEmailAddress("Test@Test.com");

        this.mvc.perform(post("/transactions/" + transaction.getId() + "/registered-email-address")
                        .contentType("application/json").header("ERIC-Identity", "123")
                        .header("X-Request-Id", "123456").content(writeToJson(registeredEmailAddressDTO)))

                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.registered_email_address").value("Test@Test.com"));
    }

    @Test
    public void testCreateRegisteredEmailAddressFailureTest() throws Exception {
        Transaction transaction = new Transaction();
        String id = UUID.randomUUID().toString();
        transaction.setId(id);
        RegisteredEmailAddressDTO registeredEmailAddressDTO = new RegisteredEmailAddressDTO();
        when(transactionService.getTransaction(any(), any(), any())).thenReturn(transaction);

        this.mvc.perform(post("/transactions/" + transaction.getId() + "/registered-email-address")
                        .contentType("application/json").header("ERIC-Identity", "123")
                        .header("X-Request-Id", "123456")
                        .content(writeToJson(registeredEmailAddressDTO)))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]")
                        .value("registered_email_address must not be blank"));
    }

    @Test
    public void testCreateRegisteredEmailAddressRegexFailureTest() throws Exception {
        Transaction transaction = new Transaction();
        String id = UUID.randomUUID().toString();
        transaction.setId(id);

        RegisteredEmailAddressDTO registeredEmailAddress = new RegisteredEmailAddressDTO();
        registeredEmailAddress.setRegisteredEmailAddress("223j&kg");

        when(transactionService.getTransaction(any(), any(), any())).thenReturn(transaction);

        this.mvc.perform(post("/transactions/" + transaction.getId() + "/registered-email-address")
                        .contentType("application/json").header("ERIC-Identity", "123")
                        .header("X-Request-Id", "123456")
                        .content(writeToJson(registeredEmailAddress)))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]")
                        .value("registered_email_address must have a valid email format"));
    }


    private RegisteredEmailAddressDAO getRegisteredEmailAddressDAO() {
        RegisteredEmailAddressDAO registeredEmailAddressDAO = new RegisteredEmailAddressDAO();
        registeredEmailAddressDAO.setId(UUID.randomUUID().toString());
        registeredEmailAddressDAO.setRegisteredEmailAddress("Test@Test.com");
        return registeredEmailAddressDAO;
    }

    private String writeToJson(Object object) throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer();
        return ow.writeValueAsString(object);

    }


}