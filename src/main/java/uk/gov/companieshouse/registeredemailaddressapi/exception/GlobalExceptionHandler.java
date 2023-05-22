package uk.gov.companieshouse.registeredemailaddressapi.exception;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.owasp.encoder.Encode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import uk.gov.companieshouse.registeredemailaddressapi.utils.ApiLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.companieshouse.registeredemailaddressapi.utils.Constants.ERIC_REQUEST_ID_KEY;

@ControllerAdvice
public class GlobalExceptionHandler {

    @Value("${GLOBAL_EXCEPTION_HANDLER_TRUNCATE_LENGTH_CHARS:15000}")
    private int truncationLength;


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest webRequest) {
        logException(ex, webRequest);

        ArrayList<String> errorsList = new ArrayList<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            errorsList.add(error.getDefaultMessage());
        });

        Map<String, ArrayList<String>> errors = new HashMap<>();
        errors.put("errors", errorsList);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(SubmissionNotFoundException.class)
    public ResponseEntity<Object> handleSubmissionException(Exception ex, WebRequest webRequest) {
        logException(ex, webRequest);

        return  ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<Object> handleServiceException(Exception ex, WebRequest webRequest) {
        logException(ex, webRequest);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception ex, WebRequest webRequest) {
        logException(ex, webRequest);

        return  ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }

    private void logException(Exception ex, WebRequest webRequest) {
        String context = webRequest.getHeader(ERIC_REQUEST_ID_KEY);
        String sanitisedExceptionMessage = truncate(Encode.forJava(ex.getMessage()));
        String sanitisedStackTrace = truncate(Encode.forJava(ExceptionUtils.getStackTrace(ex)));
        String sanitisedRootCause = truncate(Encode.forJava(ExceptionUtils.getStackTrace(ExceptionUtils.getRootCause(ex))));

        HashMap<String, Object> logMap = new HashMap<String, Object>();
        logMap.put("error", ex.getClass());
        logMap.put("stackTrace", sanitisedStackTrace);
        logMap.put("rootCause", sanitisedRootCause);

        ApiLogger.errorContext(context, sanitisedExceptionMessage, null, logMap);

    }

    private String truncate(String input) {
        return StringUtils.truncate(input, truncationLength);
    }

}