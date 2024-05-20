package uk.gov.companieshouse.registeredemailaddressapi.interceptor;

import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static uk.gov.companieshouse.registeredemailaddressapi.utils.Constants.ERIC_REQUEST_ID_KEY;
import static uk.gov.companieshouse.registeredemailaddressapi.utils.Constants.TRANSACTION_ID_KEY;
import static uk.gov.companieshouse.registeredemailaddressapi.utils.Constants.TRANSACTION_ID_REGEX;
import static uk.gov.companieshouse.registeredemailaddressapi.utils.Constants.TRANSACTION_KEY;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import com.fasterxml.jackson.databind.ObjectMapper;

import uk.gov.companieshouse.registeredemailaddressapi.service.TransactionService;
import uk.gov.companieshouse.registeredemailaddressapi.utils.ApiLogger;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;

/**
 * Retrieves the transaction data and sets it into the request.
 */
@Component
public class TransactionInterceptor implements HandlerInterceptor {

    private final TransactionService transactionService;
    private static final Pattern TRANSACTION_ID_PATTERN = Pattern.compile(TRANSACTION_ID_REGEX);

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

        if (!TRANSACTION_ID_PATTERN.matcher(transactionId).matches()) {
            ApiLogger.debugContext(reqId, "Invalid transaction id", logMap);

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            var errorsList = Map.of("errors", Map.of("error", "Invalid transaction id"));
            response.setContentType("application/json");
            var objectMapper = new ObjectMapper();
            response.getWriter().write(objectMapper.writeValueAsString(errorsList));
            return false;
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
