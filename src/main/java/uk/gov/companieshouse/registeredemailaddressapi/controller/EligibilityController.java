package uk.gov.companieshouse.registeredemailaddressapi.controller;

import static uk.gov.companieshouse.registeredemailaddressapi.utils.Constants.COMPANY_NUMBER_REGEX;
import static uk.gov.companieshouse.registeredemailaddressapi.utils.Constants.ERIC_REQUEST_ID_KEY;

import java.util.HashMap;

import javax.validation.constraints.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import uk.gov.companieshouse.registeredemailaddressapi.eligibility.EligibilityStatusCode;
import uk.gov.companieshouse.registeredemailaddressapi.exception.CompanyNotFoundException;
import uk.gov.companieshouse.registeredemailaddressapi.model.response.CompanyValidationResponse;
import uk.gov.companieshouse.registeredemailaddressapi.service.CompanyProfileService;
import uk.gov.companieshouse.registeredemailaddressapi.service.EligibilityService;
import uk.gov.companieshouse.registeredemailaddressapi.utils.ApiLogger;

@RestController
@Validated
public class EligibilityController {

    @Autowired
    private CompanyProfileService companyProfileService;

    @Autowired
    private EligibilityService eligibilityService;

    @GetMapping("/registered-email-address/company/{company-number}/eligibility")
    public ResponseEntity<CompanyValidationResponse> getEligibility(
            @PathVariable("company-number") @Pattern(regexp = COMPANY_NUMBER_REGEX,
                message = "Invalid company number") String companyNumber,
            @RequestHeader(value = ERIC_REQUEST_ID_KEY) String requestId) {

        var logMap = new HashMap<String, Object>();
        logMap.put("company_number", companyNumber);
        ApiLogger.infoContext(requestId, "Calling service to retrieve company eligibility", logMap);

        try {
            var companyProfile = companyProfileService.getCompanyProfile(companyNumber);
            var companyValidationResponse = eligibilityService.checkCompanyEligibility(companyProfile);

            return ResponseEntity.ok().body(companyValidationResponse);
        } catch (CompanyNotFoundException e) {
            var companyNotFoundResponse = new CompanyValidationResponse();
            companyNotFoundResponse.setEligibilityStatusCode(EligibilityStatusCode.COMPANY_NOT_FOUND);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(companyNotFoundResponse);
        } catch (Exception e) {
            ApiLogger.errorContext(requestId, "Error checking eligibility of company.", e, logMap);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
