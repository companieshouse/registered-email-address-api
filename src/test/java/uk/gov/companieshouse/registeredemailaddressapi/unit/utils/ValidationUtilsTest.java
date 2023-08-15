package uk.gov.companieshouse.registeredemailaddressapi.unit.utils;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.registeredemailaddressapi.utils.ValidationUtils;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static uk.gov.companieshouse.registeredemailaddressapi.utils.ValidationUtils.isValidEmailAddress;

@ExtendWith(MockitoExtension.class)
class ValidationUtilsTest {


    private static final String TO_TEST = "toTest";

    private static final String EMAIL_TEST = "validemailaddress@valid.com";
    private static final String DUMMY_PARENT_FIELD = "parentField";
    private static final String LOGGING_CONTEXT ="12345";
    private ArrayList<ValidationStatusError> errors;

    @BeforeEach
    void setUp() {

    }

    @Test
    @DisplayName("Validate a string is not null successfully")
    void validateNotNull_Successful() {
        assertTrue(ValidationUtils.isNotNull(TO_TEST, DUMMY_PARENT_FIELD, errors, LOGGING_CONTEXT));
    }

    @Test
    @DisplayName("Validate a string is not null unsuccessfully - null object")
    void validateNotNull_Unsuccessful() {
        var errors = new ArrayList<ValidationStatusError>();
        boolean isNotNull = ValidationUtils.isNotNull(null, DUMMY_PARENT_FIELD, errors, LOGGING_CONTEXT);

        assertFalse(isNotNull);
        assertEquals(1, errors.size());
        assertEquals(DUMMY_PARENT_FIELD + " must not be null", errors.get(0).getError());
    }

    @Test
    @DisplayName("Validate Email successfully")
    void validateEmail_Successful() {
        var errors = new ArrayList<ValidationStatusError>();
        isValidEmailAddress(EMAIL_TEST, DUMMY_PARENT_FIELD, errors, LOGGING_CONTEXT);
        assertEquals(0, errors.size());
        assertTrue(errors.isEmpty());
    }

    @Test
    @DisplayName("Validate Email unsuccessfully")
    void validateEmail_Unsuccessful() {
        var errors = new ArrayList<ValidationStatusError>();
        isValidEmailAddress("lorem@ipsum", DUMMY_PARENT_FIELD, errors, LOGGING_CONTEXT);
        assertEquals(1, errors.size());
        assertEquals("Email address is not in the correct format for parentField, like name@example.com", errors.get(0).getError());
    }

}
