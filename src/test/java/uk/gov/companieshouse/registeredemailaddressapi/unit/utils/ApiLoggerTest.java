package uk.gov.companieshouse.registeredemailaddressapi.unit.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.registeredemailaddressapi.utils.ApiLogger;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiLoggerTest {

    private static final String CONTEXT = "CONTEXT";
    private static final String TEST_MESSAGE = "TEST";
    private static final String LOG_MAP_KEY = "COMPANY_NUMBER";
    private static final String LOG_MAP_VALUE = "00006400";

    private Map<String, Object> logMap;

    @BeforeEach
    void setup() {
        logMap = new HashMap<>();
        logMap.put(LOG_MAP_KEY, LOG_MAP_VALUE);
    }

    @Test
    void testDebugContextLoggingDoesNotModifyLogMap() {
        ApiLogger.debugContext(CONTEXT, TEST_MESSAGE, logMap);

        assertEquals(1, logMap.size());
        assertEquals(LOG_MAP_VALUE, logMap.get(LOG_MAP_KEY));
    }

    @Test
    void testDebugLoggingDoesNotModifyLogMap() {
        ApiLogger.debug(CONTEXT, logMap);

        assertEquals(1, logMap.size());
        assertEquals(LOG_MAP_VALUE, logMap.get(LOG_MAP_KEY));
    }

    @Test
    void testInfoDoesNotModifyLogMap() {
        ApiLogger.info(TEST_MESSAGE);

        assertEquals(1, logMap.size());
        assertEquals(LOG_MAP_VALUE, logMap.get(LOG_MAP_KEY));
    }


    @Test
    void testInfoLoggingDoesNotModifyLogMap() {
        ApiLogger.info(TEST_MESSAGE, logMap);

        assertEquals(1, logMap.size());
        assertEquals(LOG_MAP_VALUE, logMap.get(LOG_MAP_KEY));
    }

    @Test
    void testInfoContextDoesNotModifyLogMap() {
        ApiLogger.infoContext(CONTEXT, TEST_MESSAGE);

        assertEquals(1, logMap.size());
        assertEquals(LOG_MAP_VALUE, logMap.get(LOG_MAP_KEY));
    }
    @Test
    void testInfoContextLoggingDoesNotModifyLogMap() {
        ApiLogger.infoContext(CONTEXT, TEST_MESSAGE, logMap);

        assertEquals(1, logMap.size());
        assertEquals(LOG_MAP_VALUE, logMap.get(LOG_MAP_KEY));
    }

    @Test
    void testErrorContextLoggingDoesNotModifyLogMap() {
        ApiLogger.errorContext(CONTEXT, TEST_MESSAGE, new Exception("TEST"), logMap);

        assertEquals(1, logMap.size());
        assertEquals(LOG_MAP_VALUE, logMap.get(LOG_MAP_KEY));
    }

    @Test
    void testErrorLoggingDoesNotModifyLogMap() {
        ApiLogger.error(TEST_MESSAGE, new Exception(TEST_MESSAGE), logMap);

        assertEquals(1, logMap.size());
        assertEquals(LOG_MAP_VALUE, logMap.get(LOG_MAP_KEY));
    }

    @Test
    void testErrorContextDoesNotModifyLogMap() {
        ApiLogger.errorContext(TEST_MESSAGE, new Exception(TEST_MESSAGE));

        assertEquals(1, logMap.size());
        assertEquals(LOG_MAP_VALUE, logMap.get(LOG_MAP_KEY));
    }

    @Test
    void testErrorLoggingContextDoesNotModifyLogMap() {
        ApiLogger.errorContext(CONTEXT, TEST_MESSAGE, new Exception("TEST"));

        assertEquals(1, logMap.size());
        assertEquals(LOG_MAP_VALUE, logMap.get(LOG_MAP_KEY));
    }

}
