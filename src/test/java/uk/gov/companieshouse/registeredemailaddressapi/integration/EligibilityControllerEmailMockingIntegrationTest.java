package uk.gov.companieshouse.registeredemailaddressapi.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.registeredemailaddressapi.integration.utils.Helper;
import uk.gov.companieshouse.registeredemailaddressapi.service.CompanyProfileService;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"env.name = livesbox"})
class EligibilityControllerEmailMockingIntegrationTest {

    private static final Helper HELPER = new Helper();

    @Autowired
    private MockMvc mvc;

    @MockBean
    private CompanyProfileService companyProfileService;


    @Test
    void EligibilityEndpointMockEmailResponseTest() throws Exception {

        final String companyNumber = "12345ERR";
        CompanyProfileApi companyProfileApi = HELPER.generateCompanyProfileApi(companyNumber);
        given(companyProfileService.getCompanyProfile(companyNumber)).willReturn(companyProfileApi);

        this.mvc.perform(get("/registered-email-address/company/"+companyNumber+"/eligibility")
                .contentType("application/json").header("ERIC-Identity", "123")
                .header("X-Request-Id", "123456"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"eligibility_status_code\":\"COMPANY_VALID_FOR_SERVICE\"}"));
    }

    @Test
    void EligibilityEndpointMockNullResponseTest() throws Exception {

        final String companyNumber = "12345678";
        CompanyProfileApi companyProfileApi = HELPER.generateCompanyProfileApi(companyNumber);
        given(companyProfileService.getCompanyProfile(companyNumber)).willReturn(companyProfileApi);

        this.mvc.perform(get("/registered-email-address/company/"+companyNumber+"/eligibility")
                .contentType("application/json").header("ERIC-Identity", "123")
                .header("X-Request-Id", "123456"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"eligibility_status_code\":\"INVALID_NO_REGISTERED_EMAIL_ADDRESS_EXISTS\"}"));
    }


}
