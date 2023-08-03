package uk.gov.companieshouse.registeredemailaddressapi.unit.utils;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.registeredemailaddressapi.utils.ValidationUtils;
import uk.gov.companieshouse.service.rest.err.Err;
import uk.gov.companieshouse.service.rest.err.Errors;

import static org.junit.jupiter.api.Assertions.*;
import static uk.gov.companieshouse.registeredemailaddressapi.utils.ValidationUtils.INVALID_EMAIL_ERROR_MESSAGE;
import static uk.gov.companieshouse.registeredemailaddressapi.utils.ValidationUtils.isValidEmailAddress;

@ExtendWith(MockitoExtension.class)
public class ValidationUtilsTest {


    private static final String TO_TEST = "toTest";

    private static final String EMAIL_TEST = "validemailaddress@valid.com";
    private static final String DUMMY_PARENT_FIELD = "parentField";
    private static final String LOGGING_CONTEXT ="12345";
    private Errors errors;

    @BeforeEach
    void setUp() {
        errors = new Errors();
    }

    @Test
    @DisplayName("Validate a string is not null successfully")
    void validateNotNull_Successful() {
        assertTrue(ValidationUtils.isNotNull(TO_TEST, DUMMY_PARENT_FIELD, errors, LOGGING_CONTEXT));
    }

    @Test
    @DisplayName("Validate a string is not null unsuccessfully - null object")
    void validateNotNull_Unsuccessful() {
        Err err = Err.invalidBodyBuilderWithLocation(DUMMY_PARENT_FIELD)
                .withError(ValidationUtils.NOT_NULL_ERROR_MESSAGE.replace("%s", DUMMY_PARENT_FIELD)).build();

        boolean isNotNull = ValidationUtils.isNotNull(null, DUMMY_PARENT_FIELD, errors, LOGGING_CONTEXT);

        assertFalse(isNotNull);
        assertEquals(1, errors.size());
        assertTrue(errors.containsError(err));
    }

    @Test
    @DisplayName("Validate Email successfully")
    void validateEmail_Successful() {
        String errMsg = INVALID_EMAIL_ERROR_MESSAGE.replace("%s", DUMMY_PARENT_FIELD);
        Err err = Err.invalidBodyBuilderWithLocation(DUMMY_PARENT_FIELD).withError(errMsg).build();

        isValidEmailAddress(EMAIL_TEST, DUMMY_PARENT_FIELD, errors, LOGGING_CONTEXT);

        assertEquals(0, errors.size());
        assertFalse(errors.containsError(err));
    }

    @Test
    @DisplayName("Validate Email unsuccessfully")
    void validateEmail_Unsuccessful() {
        String errMsg = INVALID_EMAIL_ERROR_MESSAGE.replace("%s", DUMMY_PARENT_FIELD);
        Err err = Err.invalidBodyBuilderWithLocation(DUMMY_PARENT_FIELD).withError(errMsg).build();

        isValidEmailAddress("lorem@ipsum", DUMMY_PARENT_FIELD, errors, LOGGING_CONTEXT);

        assertEquals(1, errors.size());
        assertTrue(errors.containsError(err));
    }

}
