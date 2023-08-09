package uk.gov.companieshouse.registeredemailaddressapi.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.registeredemailaddressapi.controller.HealthCheckController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ContextConfiguration(classes = HealthCheckController.class)
@AutoConfigureMockMvc
class HealthCheckControllerTest {
    @Autowired
    private MockMvc mvc;

    @Test
    void HealthCheckEndpointTest() throws Exception {
        this.mvc.perform(get("/registered-email-address/healthcheck"))
                .andExpect(status().isOk())
                .andExpect(content().string("Registered Email Address Service is Healthy"));
    }

}