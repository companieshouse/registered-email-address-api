package uk.gov.companieshouse.registeredemailaddressapi.utils;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;

import java.util.List;
import java.util.regex.Pattern;

@Component
public class ValidationUtils {

    private static final Pattern EMAIL_NOTIFY_REGEX =
            Pattern.compile("^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~\\-]+@([^.@][^@\\s]+)$");
    private static final Pattern HOSTNAME_REGEX =
            Pattern.compile("^([a-z0-9]+)(-?-[a-z0-9]+)*$", Pattern.CASE_INSENSITIVE);
    private static final Pattern TLD_PART_REGEX =
            Pattern.compile(
                    "^(?:[a-z]{2,63}|xn--[a-z0-9]+(?:-[a-z0-9]+){1,4})(?:$|[^-])",
                    Pattern.CASE_INSENSITIVE);

    public static final String NOT_NULL_ERROR_MESSAGE = "%s must not be null";
    public static final String INVALID_EMAIL_ERROR_MESSAGE = "Email address is not in the correct format for %s, like name@example.com";
    public static final String ACCEPTED_EMAIL_ADDRESS_STATEMENT_ERROR_MESSAGE = "The Appropriate Email Address Statement has not been accepted.";

    private ValidationUtils() { }

    public static boolean isNotNull(Object toTest, String qualifiedFieldName, List<ValidationStatusError> errs, String loggingContext) {
        if (toTest == null) {
            setErrorMsg(errs, qualifiedFieldName, NOT_NULL_ERROR_MESSAGE.replace("%s", qualifiedFieldName));
            ApiLogger.infoContext(loggingContext , qualifiedFieldName + " Field is null");
            return false;
        }
        return true;
    }
    public static void validateEmailAddress(String email, String qualifiedFieldName, List<ValidationStatusError> errs, String loggingContext) {


        if (!isEmailAddressValid(email)) {
            setErrorMsg(errs, qualifiedFieldName, String.format(INVALID_EMAIL_ERROR_MESSAGE, qualifiedFieldName));
            ApiLogger.infoContext(loggingContext, "Email address is not in the correct format for " + qualifiedFieldName);
        }
    }

    public static boolean isEmailAddressValid(String email){
        if (email == null || email.isBlank()) {
            return false;
        }
        if (email.contains("..")) {
            return false;
        }
        var matcher = EMAIL_NOTIFY_REGEX.matcher(email);
        if (!matcher.matches()) {
            return false;
        }

        var hostname = matcher.group(1);
        String[] parts = hostname.split("\\.");
        if (parts.length < 2) {
            return false;
        }
        for (String part : parts) {
            if (!HOSTNAME_REGEX.matcher(part).matches()) {
                return false;
            }
        }
        return TLD_PART_REGEX.matcher(parts[parts.length - 1]).matches();
    }
    public static void isEmailAddressStatementAccepted(boolean acceptedEmailAddressStatement, String qualifiedFieldName, List<ValidationStatusError> errs, String loggingContext) {
        if (!acceptedEmailAddressStatement) {
            setErrorMsg(errs, qualifiedFieldName, String.format(ACCEPTED_EMAIL_ADDRESS_STATEMENT_ERROR_MESSAGE, qualifiedFieldName));
            ApiLogger.infoContext(loggingContext, "The " + qualifiedFieldName + " must be True.");
        }
    }

    public static void setErrorMsg(List<ValidationStatusError> errs, String qualifiedFieldName, String msg){
        ValidationStatusError validationStatusError = new ValidationStatusError();
        validationStatusError.setError(msg);
        validationStatusError.setLocation(qualifiedFieldName);
        validationStatusError.setType("ch:validation");
        validationStatusError.setLocationType("json-path");
        errs.add(validationStatusError);
    }



}
