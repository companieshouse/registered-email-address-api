package uk.gov.companieshouse.registeredemailaddressapi.unit.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.WebRequest;
import uk.gov.companieshouse.registeredemailaddressapi.exception.GlobalExceptionHandler;
import uk.gov.companieshouse.registeredemailaddressapi.utils.ApiLogger;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static uk.gov.companieshouse.registeredemailaddressapi.utils.Constants.ERIC_REQUEST_ID_KEY;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private static final String REQUEST_ID = "1234";
    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private WebRequest webRequest;

    @Captor
    private ArgumentCaptor<Map<String, Object>> logMapCaptor;

    @BeforeEach
    void setUp() {
        this.globalExceptionHandler = new GlobalExceptionHandler();
        setTruncationLength(1000);
    }

    @Test
    void testHandleExceptionReturnsCorrectResponse() {
        when(webRequest.getHeader(ERIC_REQUEST_ID_KEY)).thenReturn(REQUEST_ID);

        ResponseEntity<Object> entity = globalExceptionHandler.handleException(new Exception(), webRequest);

        assertNotNull(entity);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, entity.getStatusCode());
    }

    @Test
    void testHandleExceptionEncodesException() {
        Throwable rootCause = new Throwable("root cause \n");
        Exception exception = new Exception("exception message \n", rootCause);

        when(webRequest.getHeader(ERIC_REQUEST_ID_KEY)).thenReturn(REQUEST_ID);

        try (MockedStatic<ApiLogger> apiLogger = mockStatic(ApiLogger.class)) {

            globalExceptionHandler.handleException(exception, webRequest);

            apiLogger.verify(() -> ApiLogger.errorContext(
                    eq(REQUEST_ID),
                    eq("exception message \\n"),
                    eq(null),
                    logMapCaptor.capture()), times(1));
        }
    }

    @Test
    void testHandleExceptionTruncatesException() {
        setTruncationLength(20);
        Throwable rootCause = new Throwable("root cause");
        Exception exception = new Exception("12345678901234567890123", rootCause);

        when(webRequest.getHeader(ERIC_REQUEST_ID_KEY)).thenReturn(REQUEST_ID);

        try (MockedStatic<ApiLogger> apiLogger = mockStatic(ApiLogger.class)) {
            globalExceptionHandler.handleException(exception, webRequest);

            apiLogger.verify(() -> ApiLogger.errorContext(
                    eq(REQUEST_ID),
                    eq("12345678901234567890"),
                    eq(null),
                    logMapCaptor.capture()), times(1));
        }
    }

    private void setTruncationLength(int length) {
        ReflectionTestUtils.setField(globalExceptionHandler, "truncationLength", length);
    }
}
