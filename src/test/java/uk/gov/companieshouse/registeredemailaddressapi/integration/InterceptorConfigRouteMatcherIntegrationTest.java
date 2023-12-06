package uk.gov.companieshouse.registeredemailaddressapi.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
    private RequestMappingHandlerMapping mapping;

    @Test
    void interceptorsMatchIntendedRoutesTest() throws Exception {

        Map<String,Set<String>> testCases = new HashMap<>();
        Set<String> LOGGING_INTERCEPTOR = Set.of("LoggingInterceptor");
        Set<String> TRANSACTION_INTERCEPTORS =
            Set.of("TransactionInterceptor", "TokenPermissionsInterceptor", "LoggingInterceptor", "UserAuthenticationInterceptor");

        // LoggingInterceptor - just logs details of request at start and end
        testCases.put("/registered-email-address/healthcheck", LOGGING_INTERCEPTOR);
        testCases.put("/registered-email-address/company/12345678/eligibility", LOGGING_INTERCEPTOR);

        /* Transactions:
            - TransactionInterceptor - sets transaction ID & retrieved txn details in request
            - TokenPermissionsInterceptor - creates a TokenPermissions object and sets it into the request
            - LoggingInterceptor - just logs details of request at start and end
            - AUTH: UserAuthenticationInterceptor - OK if using API key OR user has a scope with the company_rea=update permission
        */
        testCases.put("/transactions/111111-222222-333333/registered-email-address", TRANSACTION_INTERCEPTORS);
        testCases.put("/transactions/111111-222222-333333/registered-email-address/validation-status", TRANSACTION_INTERCEPTORS);

        /* Internal endpoint (filings):
            - LoggingInterceptor - just logs details of request at start and end
            - AUTH: InternalUserInterceptor - must be API key auth with internal user privileges
            - TransactionInterceptor - sets transaction ID & retrieved txn details in request
            - FilingInterceptor - checks that transaction is closed
        */
        testCases.put("/private/transactions/111111-222222-333333/registered-email-address/filings",
            Set.of("LoggingInterceptor", "InternalUserInterceptor", "TransactionInterceptor", "FilingInterceptor"));

        for (String requestPath : testCases.keySet()){

            Set<String> expectedInterceptors = testCases.get(requestPath);

            MockHttpServletRequest request = new MockHttpServletRequest("GET", requestPath);
            if (!ServletRequestPathUtils.hasParsedRequestPath(request)) {
                ServletRequestPathUtils.parseAndCache(request);
            }
            HandlerExecutionChain chain;
            try {
                chain = mapping.getHandler(request);
            } catch (HttpRequestMethodNotSupportedException e) {
                request = new MockHttpServletRequest("POST", requestPath);
                chain = mapping.getHandler(request);
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
