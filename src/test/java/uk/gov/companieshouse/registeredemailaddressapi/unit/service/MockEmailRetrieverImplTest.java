package uk.gov.companieshouse.registeredemailaddressapi.unit.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.model.company.RegisteredEmailAddressJson;
import uk.gov.companieshouse.registeredemailaddressapi.exception.ServiceException;
import uk.gov.companieshouse.registeredemailaddressapi.service.MockEmailRetrieverImpl;

class MockEmailRetrieverImplTest {

    private static final String COMPANY_NUMBER = "12345678";
    private static final String COMPANY_EMAIL_MOCK = "mockexistingemail@companieshouse.gov.uk";


    private MockEmailRetrieverImpl mockEmailRetrieverImpl = new MockEmailRetrieverImpl();


    @Nested
    class GetRegisteredEmailAddressTests {

        @Test
        void testMockEmailAddressForTestCompany() throws ServiceException {

            RegisteredEmailAddressJson response = mockEmailRetrieverImpl.getRegisteredEmailAddress(COMPANY_NUMBER);
            assertEquals(COMPANY_EMAIL_MOCK, response.getRegisteredEmailAddress());
        }
    }
}
