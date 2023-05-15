package uk.gov.companieshouse.registeredemailaddressapi.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.companieshouse.api.interceptor.InternalUserInterceptor;
import uk.gov.companieshouse.api.interceptor.TokenPermissionsInterceptor;
import uk.gov.companieshouse.registeredemailaddressapi.interceptor.TransactionInterceptor;
import uk.gov.companieshouse.registeredemailaddressapi.interceptor.LoggingInterceptor;
import uk.gov.companieshouse.registeredemailaddressapi.interceptor.UserAuthenticationInterceptor;
import uk.gov.companieshouse.registeredemailaddressapi.interceptor.FilingInterceptor;

@Configuration
@ComponentScan("uk.gov.companieshouse.api.interceptor")
public class InterceptorConfig implements WebMvcConfigurer {
    static final String TRANSACTIONS = "/transactions/**";
    static final String FILINGS = "/private/transactions/**/filings";

    static final String[] USER_AUTH_ENDPOINTS = {
            TRANSACTIONS
    };

    static final String[] INTERNAL_AUTH_ENDPOINTS = {
        FILINGS
    };
  
    @Autowired
    private LoggingInterceptor loggingInterceptor;

    @Autowired
    private TransactionInterceptor transactionInterceptor;

    @Autowired
    private UserAuthenticationInterceptor userAuthenticationInterceptor;

    @Autowired
    private InternalUserInterceptor internalUserInterceptor;

    @Autowired
    private FilingInterceptor filingInterceptor;

    /**
     * Setup the interceptors to run against endpoints when the endpoints are called
     * Interceptors are executed in the order they are added to the registry
     *
     * @param registry The spring interceptor registry
     */
    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        addTokenPermissionsInterceptor(registry);
        addLoggingInterceptor(registry);
        addTransactionInterceptor(registry);
        addUserAuthenticationEndpointsInterceptor(registry);
        addInternalUserAuthenticationEndpointsInterceptor(registry);
        addFilingInterceptor(registry);
    }

    /**
     * Interceptor to insert TokenPermissions into the request for authentication
     * @param registry The spring interceptor registry
     */
    private void addTokenPermissionsInterceptor(InterceptorRegistry registry) {
        registry.addInterceptor(getTokenPermissionsInterceptor())
                .addPathPatterns(USER_AUTH_ENDPOINTS);
    }
    private TokenPermissionsInterceptor getTokenPermissionsInterceptor() {
        return new TokenPermissionsInterceptor();
    }


    /**
     * Interceptor that logs all calls to endpoints
     * @param registry The spring interceptor registry
     */
    private void addLoggingInterceptor(InterceptorRegistry registry) {
        registry.addInterceptor(loggingInterceptor);
    }

    /**
     * Interceptor to get transaction and put in request for endpoints that require a transaction
     *
     * @param registry The spring interceptor registry
     */
    private void addTransactionInterceptor(InterceptorRegistry registry) {
        registry.addInterceptor(transactionInterceptor)
                .addPathPatterns(TRANSACTIONS, FILINGS);
    }

     /**
     * Interceptor to authenticate access to specified endpoints using user permissions
     * @param registry The spring interceptor registry
     */
    private void addUserAuthenticationEndpointsInterceptor(InterceptorRegistry registry) {
        registry.addInterceptor(userAuthenticationInterceptor)
                .addPathPatterns(USER_AUTH_ENDPOINTS);
    }

    /**
     * Interceptor to authenticate access to specified endpoints using internal permissions
     * @param registry The spring interceptor registry
     */
    private void addInternalUserAuthenticationEndpointsInterceptor(InterceptorRegistry registry) {
        registry.addInterceptor(internalUserInterceptor)
                .addPathPatterns(INTERNAL_AUTH_ENDPOINTS);
    }

    /**
     * Interceptor to check specific conditions for the /filings endpoint
     * @param registry The spring interceptor registry
     */
    private void addFilingInterceptor(InterceptorRegistry registry) {
        registry.addInterceptor(filingInterceptor)
                .addPathPatterns(FILINGS);
    }
}
