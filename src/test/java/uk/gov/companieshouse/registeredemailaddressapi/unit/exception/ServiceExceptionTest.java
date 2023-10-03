package uk.gov.companieshouse.registeredemailaddressapi.unit.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.companieshouse.registeredemailaddressapi.exception.ServiceException;

@ExtendWith(MockitoExtension.class)
class ServiceExceptionTest {

    @Test
    void testServiceException() {
        String msg = "message";
        var exception = new ServiceException(msg);
        assertEquals(msg, exception.getMessage());
    }
}
