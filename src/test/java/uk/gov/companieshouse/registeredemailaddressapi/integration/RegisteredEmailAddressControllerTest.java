package uk.gov.companieshouse.registeredemailaddressapi.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.registeredemailaddressapi.client.ApiClientService;
import uk.gov.companieshouse.registeredemailaddressapi.controller.RegisteredEmailAddressController;
import uk.gov.companieshouse.registeredemailaddressapi.interceptor.TransactionInterceptor;
import uk.gov.companieshouse.registeredemailaddressapi.model.dao.RegisteredEmailAddressDAO;
import uk.gov.companieshouse.registeredemailaddressapi.model.dto.RegisteredEmailAddressDTO;
import uk.gov.companieshouse.registeredemailaddressapi.repository.RegisteredEmailAddressRepository;
import uk.gov.companieshouse.registeredemailaddressapi.service.TransactionService;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class RegisteredEmailAddressControllerTest {


    //TODO test broken as TransactionInterceptor methods are not being mocked

    //TODO - introduce test containers

    @Mock
    TransactionService transactionService;

    @Mock
    RegisteredEmailAddressRepository registeredEmailAddressRepository;

    @Autowired
    private MockMvc mvc;

    @Test
    public void HealthCheckEndpointTest() throws Exception {
        this.mvc.perform(get("/registered-email-address/healthcheck"))
                .andExpect(status().isOk())
                .andExpect(content().string("Registered Email Address Service is Healthy"));
    }


    @Test
    public void testCreateRegisteredEmailAddressSuccessTest() throws Exception {

        String id = UUID.randomUUID().toString();
        Transaction transaction = new Transaction();
        transaction.setId(id);
        when(transactionService.getTransaction(anyString(), anyString(), anyString()))
                .thenReturn(new Transaction());

        when(registeredEmailAddressRepository.insert(any(RegisteredEmailAddressDAO.class)))
                .thenReturn(getRegisteredEmailAddressDAO());

        RegisteredEmailAddressDTO registeredEmailAddressDTO = new RegisteredEmailAddressDTO();
        registeredEmailAddressDTO.setRegisteredEmailAddress("Test@Test.com");

        this.mvc.perform(post("/transactions/" + id + "/registered-email-address")
                        .contentType("application/json")
                        .header("ERIC-Identity", "123")
                        .header("X-Request-Id", "123456")
                        .content(writeToJson(registeredEmailAddressDTO))
                )

                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.registered_email_address").value("Test@Test.com"));
    }

    @Test
    public void testCreateRegisteredEmailAddressFailureTest() throws Exception {

        RegisteredEmailAddressDTO registeredEmailAddressDTO = new RegisteredEmailAddressDTO();

        this.mvc.perform(post("/registered-email-address/transactions/123456/registered-email-address")
                        .contentType("application/json")
                        .header("ERIC-Identity", "123")
                        .header("X-Request-Id", "123456")
                        .content(writeToJson(registeredEmailAddressDTO))
                )

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]")
                        .value("registered_email_address must not be blank"));
    }

    @Test
    public void testCreateRegisteredEmailAddressRegexFailureTest() throws Exception {

        RegisteredEmailAddressDTO registeredEmailAddress = new RegisteredEmailAddressDTO();
        registeredEmailAddress.setRegisteredEmailAddress("223j&kg");

        this.mvc.perform(post("/registered-email-address/transactions/123456/registered-email-address")
                        .contentType("application/json")
                        .header("ERIC-Identity", "123")
                        .header("X-Request-Id", "123456")
                        .content(writeToJson(registeredEmailAddress))
                )

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