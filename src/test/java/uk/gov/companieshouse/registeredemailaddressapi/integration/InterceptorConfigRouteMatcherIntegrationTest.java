package uk.gov.companieshouse.registeredemailaddressapi.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.ServletRequestPathUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class InterceptorConfigRouteMatchingTest {

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Test
    void interceptorsMatchIntendedRoutesTest() throws Exception {

        Map<String,Set<String>> testCases = new HashMap<>();

        Set<String> LOGGING_INTERCEPTOR = Set.of("LoggingInterceptor");
        Set<String> COMPANY_INTERCEPTORS = Set.of("LoggingInterceptor", "TokenPermissionsInterceptor", "UserAuthenticationInterceptor");
        Set<String> TRANSACTION_INTERCEPTORS =
            Set.of("LoggingInterceptor", "TokenPermissionsInterceptor", "UserAuthenticationInterceptor", "TransactionInterceptor");
        Set<String> FILINGS_INTERCEPTORS =
            Set.of("LoggingInterceptor", "InternalUserInterceptor", "TransactionInterceptor", "FilingInterceptor");

        // logging only
        testCases.put("/registered-email-address/healthcheck", LOGGING_INTERCEPTOR);

        // company endpoints
        testCases.put("/registered-email-address/company/12345678/eligibility", COMPANY_INTERCEPTORS);

        // transaction endpoints
        testCases.put("/transactions/111111-222222-333333/registered-email-address", TRANSACTION_INTERCEPTORS);
        testCases.put("/transactions/111111-222222-333333/registered-email-address/validation-status", TRANSACTION_INTERCEPTORS);

        // filings
        testCases.put("/private/transactions/111111-222222-333333/registered-email-address/filings", FILINGS_INTERCEPTORS);

        for (String requestPath : testCases.keySet()){

            Set<String> expectedInterceptors = testCases.get(requestPath);

            MockHttpServletRequest request = new MockHttpServletRequest("GET", requestPath);
            if (!ServletRequestPathUtils.hasParsedRequestPath(request)) {
                ServletRequestPathUtils.parseAndCache(request);
            }
            HandlerExecutionChain chain;
            try {
                chain = requestMappingHandlerMapping.getHandler(request);
            } catch (HttpRequestMethodNotSupportedException e) {
                request = new MockHttpServletRequest("POST", requestPath);
                chain = requestMappingHandlerMapping.getHandler(request);
            }
            assertNotNull(chain, "No handler found for path "+requestPath);

            Set<String> foundInterceptors = chain.getInterceptorList()
                .stream()
                .map((s) -> s.getClass())
                .filter((s) -> s.getPackageName().startsWith("uk.gov.companieshouse"))
                .map((s) -> s.getSimpleName())
                .collect(Collectors.toSet());

            assertEquals(expectedInterceptors, foundInterceptors, "Interceptors not as expected for path "+requestPath);
        }
    }
}
