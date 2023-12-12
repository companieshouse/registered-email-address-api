package uk.gov.companieshouse.registeredemailaddressapi.unit.interceptor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.HandlerMapping;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.registeredemailaddressapi.exception.ServiceException;
import uk.gov.companieshouse.registeredemailaddressapi.interceptor.TransactionInterceptor;
import uk.gov.companieshouse.registeredemailaddressapi.service.TransactionService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static uk.gov.companieshouse.registeredemailaddressapi.utils.Constants.TRANSACTION_ID_KEY;

@ExtendWith(MockitoExtension.class)
class TransactionInterceptorTest {

    private static final String TX_ID = "123456-654321-123456";
    private static final String TX_ID_TOO_LONG = "123456-654321-1234567";
    private static final String TX_ID_ILLEGAL_CHAR = "123456-654321-12345!";
    private static final String PASSTHROUGH_HEADER = "passthrough";
    private static final String LOGGING_CONTEXT = "fg4536";

    @Mock
    private TransactionService transactionService;

    @Mock
    private HttpServletRequest mockHttpServletRequest;

    @InjectMocks
    private TransactionInterceptor transactionInterceptor;

    @Test
    void testPreHandleIsSuccessful() throws Exception {
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
        Object mockHandler = new Object();
        Transaction dummyTransaction = new Transaction();
        dummyTransaction.setId(TX_ID);

        var pathParams = new HashMap<String, String>();
        pathParams.put(TRANSACTION_ID_KEY, TX_ID);

        when(transactionService.getTransaction(eq(TX_ID), eq(PASSTHROUGH_HEADER), any())).thenReturn(dummyTransaction);
        when(mockHttpServletRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).thenReturn(pathParams);
        when(mockHttpServletRequest.getHeader("ERIC-Access-Token")).thenReturn(PASSTHROUGH_HEADER);

        assertTrue(transactionInterceptor.preHandle(mockHttpServletRequest, mockHttpServletResponse, mockHandler));
        verify(mockHttpServletRequest, times(1)).setAttribute("transaction", dummyTransaction);
    }

    @Test
    void testPreHandleIsUnsuccessfulWhenTxnIdIsTooLong() throws Exception {
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
        Object mockHandler = new Object();

        var pathParams = new HashMap<String, String>();
        pathParams.put(TRANSACTION_ID_KEY, TX_ID_TOO_LONG);

        when(mockHttpServletRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).thenReturn(pathParams);
        when(mockHttpServletRequest.getHeader("ERIC-Access-Token")).thenReturn(PASSTHROUGH_HEADER);

        assertFalse(transactionInterceptor.preHandle(mockHttpServletRequest, mockHttpServletResponse, mockHandler));
        assertEquals(HttpServletResponse.SC_BAD_REQUEST,  mockHttpServletResponse.getStatus());
        assertEquals("{\"errors\":{\"error\":\"Invalid transaction id\"}}", mockHttpServletResponse.getContentAsString());
    }

    @Test
    void testPreHandleIsUnsuccessfulWhenTxnIdContainsIllegalChar() throws Exception {
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
        Object mockHandler = new Object();

        var pathParams = new HashMap<String, String>();
        pathParams.put(TRANSACTION_ID_KEY, TX_ID_ILLEGAL_CHAR);

        when(mockHttpServletRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).thenReturn(pathParams);
        when(mockHttpServletRequest.getHeader("ERIC-Access-Token")).thenReturn(PASSTHROUGH_HEADER);

        assertFalse(transactionInterceptor.preHandle(mockHttpServletRequest, mockHttpServletResponse, mockHandler));
        assertEquals(HttpServletResponse.SC_BAD_REQUEST,  mockHttpServletResponse.getStatus());
        assertEquals("{\"errors\":{\"error\":\"Invalid transaction id\"}}", mockHttpServletResponse.getContentAsString());
    }

    @Test
    void testPreHandleIsUnsuccessfulWhenServiceExceptionCaught() throws Exception {
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
        Object mockHandler = new Object();

        var pathParams = new HashMap<String, String>();
        pathParams.put(TRANSACTION_ID_KEY, TX_ID);

        when(mockHttpServletRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).thenReturn(pathParams);
        when(mockHttpServletRequest.getHeader("ERIC-Access-Token")).thenReturn(PASSTHROUGH_HEADER);
        when(transactionService.getTransaction(TX_ID, PASSTHROUGH_HEADER, LOGGING_CONTEXT)).thenThrow(ServiceException.class);

        assertFalse(transactionInterceptor.preHandle(mockHttpServletRequest, mockHttpServletResponse, mockHandler));
        assertEquals(HttpServletResponse.SC_NOT_FOUND,  mockHttpServletResponse.getStatus());
    }
}
