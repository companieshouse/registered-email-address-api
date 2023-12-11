package uk.gov.companieshouse.registeredemailaddressapi.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import uk.gov.companieshouse.registeredemailaddressapi.service.TransactionService;
import uk.gov.companieshouse.registeredemailaddressapi.utils.ApiLogger;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static uk.gov.companieshouse.registeredemailaddressapi.utils.Constants.*;

/**
 * Retrieves the transaction data and sets it into the request.
 */
@Component
public class TransactionInterceptor implements HandlerInterceptor {

    private final TransactionService transactionService;

    @Autowired
    public TransactionInterceptor(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws IOException {
        final Map<String, String> pathVariables =
                (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        final var transactionId = pathVariables.get(TRANSACTION_ID_KEY);
        String passthroughHeader = request.getHeader(ApiSdkManager.getEricPassthroughTokenHeader());

        var logMap = new HashMap<String, Object>();
        logMap.put(TRANSACTION_ID_KEY, transactionId);
        String reqId = request.getHeader(ERIC_REQUEST_ID_KEY);
        if (!Pattern.matches(TRANSACTION_ID_REGEX,transactionId)){
            ApiLogger.debugContext(reqId, "Transaction id did not pass validation", logMap);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return false; // TODO set body
        }
        try {
            ApiLogger.debugContext(reqId, "Getting transaction for request.", logMap);

            final var transaction = transactionService.getTransaction(transactionId, passthroughHeader, reqId);
            ApiLogger.debugContext(reqId, "Transaction retrieved.", logMap);

            request.setAttribute(TRANSACTION_KEY, transaction);
            return true;
        } catch (Exception e) {
            ApiLogger.errorContext(reqId, "Error retrieving transaction " + transactionId, e, logMap);
            response.setStatus(SC_NOT_FOUND);
            response.getWriter().write(e.getMessage());
            return false;
        }
    }
}
