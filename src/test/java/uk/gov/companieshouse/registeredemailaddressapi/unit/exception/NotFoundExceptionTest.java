package uk.gov.companieshouse.registeredemailaddressapi.unit.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.registeredemailaddressapi.exception.NotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class NotFoundExceptionTest {

    @Test
    void testSubmissionNotFoundExceptionTest() {
        String msg = "message";
        var exception = new NotFoundException(msg);
        assertEquals(msg, exception.getMessage());
    }
}
