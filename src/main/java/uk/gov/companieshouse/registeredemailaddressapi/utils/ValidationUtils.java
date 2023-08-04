package uk.gov.companieshouse.registeredemailaddressapi.utils;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.service.rest.err.Err;
import uk.gov.companieshouse.service.rest.err.Errors;

import java.util.regex.Pattern;

@Component
public class ValidationUtils {

    public static final String NOT_NULL_ERROR_MESSAGE = "%s must not be null";
    public static final String INVALID_EMAIL_ERROR_MESSAGE = "Email address is not in the correct format for %s, like name@example.com";
    public static final String ACCEPTED_EMAIL_ADDRESS_STATEMENT_ERROR_MESSAGE = "You need to accept the registered email address statement.";

    private ValidationUtils() { }

    public static boolean isNotNull(Object toTest, String qualifiedFieldName, Errors errs, String loggingContext) {
        if (toTest == null) {
            setErrorMsgToLocation(errs, qualifiedFieldName, NOT_NULL_ERROR_MESSAGE.replace("%s", qualifiedFieldName));
            ApiLogger.infoContext(loggingContext , qualifiedFieldName + " Field is null");
            return false;
        }
        return true;
    }
    public static void isValidEmailAddress(String email, String qualifiedFieldName, Errors errs, String loggingContext) {
        var regex = "^.+@.+\\..+$";
        var pattern = Pattern.compile(regex);
        var matcher = pattern.matcher(email);

        if (!matcher.matches()) {
            setErrorMsgToLocation(errs, qualifiedFieldName, String.format(INVALID_EMAIL_ERROR_MESSAGE, qualifiedFieldName));
            ApiLogger.infoContext(loggingContext, "Email address is not in the correct format for " + qualifiedFieldName);
        }
    }
    public static void isEmailAddressStatementAccepted(boolean acceptedEmailAddressStatement, String qualifiedFieldName, Errors errs, String loggingContext) {
        if (!acceptedEmailAddressStatement) {
            setErrorMsgToLocation(errs, qualifiedFieldName, String.format(ACCEPTED_EMAIL_ADDRESS_STATEMENT_ERROR_MESSAGE, qualifiedFieldName));
            ApiLogger.infoContext(loggingContext, "The " + qualifiedFieldName + " must be True.");
        }
    }

    public static void setErrorMsgToLocation(Errors errors, String qualifiedFieldName, String msg){
        final var error = Err.invalidBodyBuilderWithLocation(qualifiedFieldName).withError(msg).build();
        errors.addError(error);
    }



}
