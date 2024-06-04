package uk.gov.companieshouse.registeredemailaddressapi.unit.interceptor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.HandlerMapping;

import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.util.security.Permission;
import uk.gov.companieshouse.api.util.security.SecurityConstants;
import uk.gov.companieshouse.api.util.security.TokenPermissions;
import uk.gov.companieshouse.registeredemailaddressapi.interceptor.UserAuthenticationInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.registeredemailaddressapi.utils.Constants.ERIC_REQUEST_ID_KEY;
import static uk.gov.companieshouse.registeredemailaddressapi.utils.Constants.TRANSACTION_ID_KEY;
import static uk.gov.companieshouse.registeredemailaddressapi.utils.Constants.TRANSACTION_KEY;
import static uk.gov.companieshouse.registeredemailaddressapi.utils.Constants.COMPANY_NUMBER_KEY;

@ExtendWith(MockitoExtension.class)
class UserAuthenticationInterceptorTest {

    private static final String TX_ID = "12345678";
    private static final String REQ_ID = "43hj5jh345";
    private static final String COMPANY_NUMBER = "87654321";
    private static final String TOKEN_PERMISSIONS = "token_permissions";
    private static final String ERIC_IDENTITY_TYPE = "ERIC-Identity-Type";
    private static Map<String, String> pathParams = new HashMap<String, String>();


    @Mock
    private HttpServletRequest mockHttpServletRequest;

    @Mock
    private TokenPermissions mockTokenPermissions;

    @InjectMocks
    private UserAuthenticationInterceptor userAuthenticationInterceptor;

    @BeforeEach
    void init() {
        pathParams.clear();
        pathParams.put(TRANSACTION_ID_KEY, TX_ID);
        pathParams.put(COMPANY_NUMBER_KEY, COMPANY_NUMBER);

        when(mockHttpServletRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).thenReturn(pathParams);
    }

    @Test
    void testInterceptorReturnsTrueWhenRequestHasCorrectTokenPermission() {
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
        Object mockHandler = new Object();

        when(mockHttpServletRequest.getAttribute(TOKEN_PERMISSIONS)).thenReturn(mockTokenPermissions);
        when(mockTokenPermissions.hasPermission(Permission.Key.COMPANY_NUMBER, COMPANY_NUMBER)).thenReturn(true);
        when(mockTokenPermissions.hasPermission(Permission.Key.COMPANY_REA_UPDATE, Permission.Value.UPDATE)).thenReturn(true);

        var result = userAuthenticationInterceptor.preHandle(mockHttpServletRequest, mockHttpServletResponse, mockHandler);
        assertTrue(result);
        assertEquals(HttpServletResponse.SC_OK,  mockHttpServletResponse.getStatus());
    }

    @Test
    void testInterceptorReturnsTrueWhenTransactionRequestHasCorrectTokenPermission() {
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
        Object mockHandler = new Object();

        pathParams.remove(COMPANY_NUMBER_KEY);
        Transaction transaction = new Transaction();
        transaction.setCompanyNumber(COMPANY_NUMBER);;
        when(mockHttpServletRequest.getAttribute(TOKEN_PERMISSIONS)).thenReturn(mockTokenPermissions);
        when(mockHttpServletRequest.getAttribute(TRANSACTION_KEY)).thenReturn(transaction);
        when(mockTokenPermissions.hasPermission(Permission.Key.COMPANY_NUMBER, COMPANY_NUMBER)).thenReturn(true);
        when(mockTokenPermissions.hasPermission(Permission.Key.COMPANY_REA_UPDATE, Permission.Value.UPDATE)).thenReturn(true);

        var result = userAuthenticationInterceptor.preHandle(mockHttpServletRequest, mockHttpServletResponse, mockHandler);
        assertTrue(result);
        assertEquals(HttpServletResponse.SC_OK,  mockHttpServletResponse.getStatus());
    }

    @Test
    void testInterceptorReturnsFalseWhenRequestHasTokenPermissionForIncorrectCompany() {
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
        Object mockHandler = new Object();

        when(mockHttpServletRequest.getAttribute(TOKEN_PERMISSIONS)).thenReturn(mockTokenPermissions);
        when(mockTokenPermissions.hasPermission(Permission.Key.COMPANY_NUMBER, COMPANY_NUMBER)).thenReturn(false);
        when(mockTokenPermissions.hasPermission(Permission.Key.COMPANY_REA_UPDATE, Permission.Value.UPDATE)).thenReturn(true);

        var result = userAuthenticationInterceptor.preHandle(mockHttpServletRequest, mockHttpServletResponse, mockHandler);
        assertFalse(result);
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, mockHttpServletResponse.getStatus());
    }

    @Test
    void testInterceptorReturnsBadRequestWhenRequestHasNoCompanyNumber() {
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
        Object mockHandler = new Object();

        pathParams.remove(COMPANY_NUMBER_KEY);
        when(mockHttpServletRequest.getAttribute(TOKEN_PERMISSIONS)).thenReturn(mockTokenPermissions);

        var result = userAuthenticationInterceptor.preHandle(mockHttpServletRequest, mockHttpServletResponse, mockHandler);
        assertFalse(result);
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, mockHttpServletResponse.getStatus());
    }

    @Test
    void testInterceptorReturnsFalseWhenRequestHasIncorrectTokenPermission() {
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
        Object mockHandler = new Object();

        when(mockHttpServletRequest.getAttribute(TOKEN_PERMISSIONS)).thenReturn(mockTokenPermissions);
        when(mockTokenPermissions.hasPermission(Permission.Key.COMPANY_NUMBER, COMPANY_NUMBER)).thenReturn(true);
        when(mockTokenPermissions.hasPermission(Permission.Key.COMPANY_REA_UPDATE, Permission.Value.UPDATE)).thenReturn(false);

        var result = userAuthenticationInterceptor.preHandle(mockHttpServletRequest, mockHttpServletResponse, mockHandler);
        assertFalse(result);
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, mockHttpServletResponse.getStatus());
    }

    @Test
    void testInterceptorReturnsTrueWhenAnApiKeyIsUsed() {
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
        Object mockHandler = new Object();

        when(mockHttpServletRequest.getHeader(ERIC_REQUEST_ID_KEY)).thenReturn(REQ_ID);
        when(mockHttpServletRequest.getHeader(ERIC_IDENTITY_TYPE)).thenReturn(SecurityConstants.API_KEY_IDENTITY_TYPE);

        var result = userAuthenticationInterceptor.preHandle(mockHttpServletRequest, mockHttpServletResponse, mockHandler);
        assertTrue(result);
        assertEquals(HttpServletResponse.SC_OK,  mockHttpServletResponse.getStatus());
    }
}
