package uk.gov.companieshouse.registeredemailaddressapi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.api.model.filinggenerator.FilingApi;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.registeredemailaddressapi.exception.SubmissionNotFoundException;
import uk.gov.companieshouse.registeredemailaddressapi.service.RegisteredEmailAddressFilingService;
import uk.gov.companieshouse.registeredemailaddressapi.utils.ApiLogger;

import java.util.HashMap;

import static uk.gov.companieshouse.registeredemailaddressapi.utils.Constants.TRANSACTION_ID_KEY;
import static uk.gov.companieshouse.registeredemailaddressapi.utils.Constants.TRANSACTION_KEY;

@RestController
@RequestMapping()
public class RegisteredEmailAddressFilingController {
    // controller logging constant(s)
    private static final String GET_REA_FILINGS = "- Get REA filings request - ";

    @Autowired
    private final RegisteredEmailAddressFilingService registeredEmailAddressFilingService;

    public RegisteredEmailAddressFilingController(RegisteredEmailAddressFilingService registeredEmailAddressFilingService) {
        this.registeredEmailAddressFilingService = registeredEmailAddressFilingService;
    }

    @GetMapping("/private/transactions/{" + TRANSACTION_ID_KEY + "}/registered-email-address/filings")
    public ResponseEntity<FilingApi[]> getRegisteredEmailAddressFilings(
            @RequestAttribute(TRANSACTION_KEY) Transaction transaction,
            @PathVariable(TRANSACTION_ID_KEY) String transactionId) {

        HashMap<String, Object> logMap = new HashMap<>();
        logMap.put(TRANSACTION_ID_KEY, transaction.getId());

        try {
            ApiLogger.infoContext(transactionId, GET_REA_FILINGS, logMap);

            FilingApi registeredEmailAddressFilings = registeredEmailAddressFilingService.generateRegisteredEmailAddressFilings(transaction);

            return ResponseEntity.ok(new FilingApi[]{registeredEmailAddressFilings});
        } catch (SubmissionNotFoundException e) {
            ApiLogger.errorContext(transactionId, e.getMessage(), e, logMap);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            ApiLogger.errorContext(transactionId, e.getMessage(), e, logMap);
            return ResponseEntity.internalServerError().build();
        }
    }
}
