package uk.gov.companieshouse.registeredemailaddressapi.unit.configuration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import uk.gov.companieshouse.api.interceptor.InternalUserInterceptor;
import uk.gov.companieshouse.registeredemailaddressapi.configuration.InterceptorConfig;
import uk.gov.companieshouse.registeredemailaddressapi.interceptor.LoggingInterceptor;
import uk.gov.companieshouse.registeredemailaddressapi.interceptor.TransactionInterceptor;
import uk.gov.companieshouse.registeredemailaddressapi.interceptor.UserAuthenticationInterceptor;
import uk.gov.companieshouse.registeredemailaddressapi.interceptor.FilingInterceptor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InterceptorConfigTest {

    static final String TRANSACTIONS = "/transactions/**";
    static final String FILINGS = "/private/transactions/**/filings";

    static final String[] USER_AUTH_ENDPOINTS = {
            TRANSACTIONS
    };

    static final String[] INTERNAL_AUTH_ENDPOINTS = {
            FILINGS
    };

    @Mock
    private InterceptorRegistry interceptorRegistry;

    @Mock
    private InterceptorRegistration interceptorRegistration;

    @Mock
    private LoggingInterceptor loggingInterceptor;

    @Mock
    private TransactionInterceptor transactionInterceptor;

    @Mock
    private UserAuthenticationInterceptor userAuthenticationInterceptor;

    @Mock
    private InternalUserInterceptor internalUserInterceptor;

    @Mock
    private FilingInterceptor filingInterceptor;

    @InjectMocks
    private InterceptorConfig interceptorConfig;

    @Test
    void addInterceptorsTest() {
        when(interceptorRegistry.addInterceptor(any())).thenReturn(interceptorRegistration);
        when(interceptorRegistration.addPathPatterns(any(String.class))).thenReturn(interceptorRegistration);

        interceptorConfig.addInterceptors(interceptorRegistry);

        InOrder inOrder = inOrder(interceptorRegistry, interceptorRegistration);

        // Logging interceptor check
        inOrder.verify(interceptorRegistry).addInterceptor(loggingInterceptor);

        // Transaction interceptor check
        inOrder.verify(interceptorRegistry).addInterceptor(transactionInterceptor);
        inOrder.verify(interceptorRegistration).addPathPatterns(TRANSACTIONS, FILINGS);

        // User authentication interceptor check
        inOrder.verify(interceptorRegistry).addInterceptor(userAuthenticationInterceptor);
        inOrder.verify(interceptorRegistration).addPathPatterns(USER_AUTH_ENDPOINTS);

        // Internal User auth interceptor check
        inOrder.verify(interceptorRegistry).addInterceptor(internalUserInterceptor);
        inOrder.verify(interceptorRegistration).addPathPatterns(INTERNAL_AUTH_ENDPOINTS);

        // Filing interceptor check
        inOrder.verify(interceptorRegistry).addInterceptor(filingInterceptor);
        inOrder.verify(interceptorRegistration).addPathPatterns(FILINGS);
    }
}
