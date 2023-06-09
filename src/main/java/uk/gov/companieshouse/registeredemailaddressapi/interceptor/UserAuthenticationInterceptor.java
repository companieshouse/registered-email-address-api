package uk.gov.companieshouse.registeredemailaddressapi.interceptor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import org.springframework.web.servlet.HandlerMapping;
import uk.gov.companieshouse.api.util.security.AuthorisationUtil;
import uk.gov.companieshouse.api.util.security.Permission.Key;
import uk.gov.companieshouse.api.util.security.Permission.Value;
import uk.gov.companieshouse.api.util.security.SecurityConstants;
import uk.gov.companieshouse.api.util.security.TokenPermissions;
import uk.gov.companieshouse.registeredemailaddressapi.utils.ApiLogger;

import static uk.gov.companieshouse.registeredemailaddressapi.utils.Constants.ERIC_REQUEST_ID_KEY;
import static uk.gov.companieshouse.registeredemailaddressapi.utils.Constants.TRANSACTION_ID_KEY;

@Component("UserAuthenticationInterceptor")
public class UserAuthenticationInterceptor implements HandlerInterceptor {

    /**
     * Pre handle method to authorize the request before it reaches the controller.
     * Retrieves the TokenPermissions stored in the request (which must have been
     * previously added by the TokenPermissionsInterceptor) and checks the relevant
     * permissions
     */
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        final Map<String, String> pathVariables =
                (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        final var transactionId = pathVariables.get(TRANSACTION_ID_KEY);

        var logMap = new HashMap<String, Object>();
        logMap.put(TRANSACTION_ID_KEY, transactionId);
        String reqId = request.getHeader(ERIC_REQUEST_ID_KEY);

        // skip token permission checks if an api key is used
        if (SecurityConstants.API_KEY_IDENTITY_TYPE.equals(AuthorisationUtil.getAuthorisedIdentityType(request))) {
            ApiLogger.debugContext(reqId, "UserAuthenticationInterceptor skipping token permission checks for api key request", logMap);
            return true;
        }

        // TokenPermissions should have been set up in the request by TokenPermissionsInterceptor
        final var tokenPermissions = getTokenPermissions(request)
                .orElseThrow(() -> new IllegalStateException("UserAuthenticationInterceptor - TokenPermissions object not present in request"));

        // Check the user has the company_rea=update permission
        boolean hasCompanyRegisteredEmailAddressUpdatePermission = tokenPermissions.hasPermission(Key.COMPANY_REA_UPDATE, Value.UPDATE);

        var authInfoMap = new HashMap<String, Object>();
        authInfoMap.put(TRANSACTION_ID_KEY, transactionId);
        authInfoMap.put("request_method", request.getMethod());
        authInfoMap.put("has_company_registered_email_address_update_permission", hasCompanyRegisteredEmailAddressUpdatePermission);

        if (hasCompanyRegisteredEmailAddressUpdatePermission) {
            ApiLogger.debugContext(reqId, "UserAuthenticationInterceptor authorised with company_rea=update permission",
                    authInfoMap);
            return true;
        }

        ApiLogger.errorContext(reqId, "UserAuthenticationInterceptor unauthorised", null, authInfoMap);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return false;
    }

    protected Optional<TokenPermissions> getTokenPermissions(HttpServletRequest request) {
        return AuthorisationUtil.getTokenPermissions(request);
    }
}