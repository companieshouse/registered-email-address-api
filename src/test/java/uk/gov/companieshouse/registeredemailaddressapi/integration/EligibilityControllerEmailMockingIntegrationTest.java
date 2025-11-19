package uk.gov.companieshouse.registeredemailaddressapi.integration;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.registeredemailaddressapi.integration.utils.Helper;
import uk.gov.companieshouse.registeredemailaddressapi.service.CompanyProfileService;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"env.name = livesbox"})
class EligibilityControllerEmailMockingIntegrationTest {

    private static final Helper HELPER = new Helper();

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CompanyProfileService companyProfileService;


    @Test
    void EligibilityEndpointMockEmailResponseTest() throws Exception {

        final String companyNumber = "RE123456";
        CompanyProfileApi companyProfileApi = HELPER.generateCompanyProfileApi(companyNumber);
        given(companyProfileService.getCompanyProfile(companyNumber)).willReturn(companyProfileApi);

        this.mvc.perform(get("/registered-email-address/company/"+companyNumber+"/eligibility")
                .contentType("application/json").header("ERIC-Identity", "123")
                .header("X-Request-Id", "123456")
                .header("ERIC-Authorised-Token-Permissions", "company_number="+companyNumber+" company_rea=update"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"eligibility_status_code\":\"COMPANY_VALID_FOR_SERVICE\"}"));
    }

    @Test
    void EligibilityEndpointInvalidCompanyNumberTest() throws Exception {

        final String companyNumber = "12345678999"; // too long
        CompanyProfileApi companyProfileApi = HELPER.generateCompanyProfileApi(companyNumber);
        given(companyProfileService.getCompanyProfile(companyNumber)).willReturn(companyProfileApi);

        this.mvc.perform(get("/registered-email-address/company/"+companyNumber+"/eligibility")
                .contentType("application/json").header("ERIC-Identity", "123")
                .header("X-Request-Id", "123456")
                .header("ERIC-Authorised-Token-Permissions", "company_number="+companyNumber+" company_rea=update"))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{\"errors\":[\"Invalid company number\"]}"));
    }

}
