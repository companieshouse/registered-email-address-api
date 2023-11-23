package uk.gov.companieshouse.registeredemailaddressapi.unit.eligibility.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.registeredemailaddressapi.eligibility.EligibilityStatusCode;
import uk.gov.companieshouse.registeredemailaddressapi.eligibility.impl.CompanyStatusValidation;
import uk.gov.companieshouse.registeredemailaddressapi.exception.EligibilityException;

class CompanyStatusValidationTest {

    private static final String ALLOWED_VALUE = "AllowedStatus";
    private static final Set<String> ALLOWED_LIST = Collections.singleton(ALLOWED_VALUE);

    private CompanyStatusValidation companyStatusValidation;

    @BeforeEach
    void init() {
        companyStatusValidation = new CompanyStatusValidation(ALLOWED_LIST);
    }

    @Test
    void validateDoesNotThrowOnAllowedValue() {
        CompanyProfileApi companyProfileApi = new CompanyProfileApi();
        companyProfileApi.setCompanyStatus(ALLOWED_VALUE);

        assertDoesNotThrow(() -> companyStatusValidation.validate(companyProfileApi));
    }

    @Test
    void validateThrowsOnDisallowedValue() {
        CompanyProfileApi companyProfileApi = new CompanyProfileApi();
        companyProfileApi.setCompanyStatus("Disallowed_Value");

        var ex = assertThrows(EligibilityException.class, () ->
                companyStatusValidation.validate(companyProfileApi));

        assertEquals(EligibilityStatusCode.INVALID_COMPANY_STATUS, ex.getEligibilityStatusCode());
    }

    @Test
    void validateThrowsOnNullStatus() {
        CompanyProfileApi companyProfileApi = new CompanyProfileApi();
        companyProfileApi.setCompanyStatus(null);

        var ex = assertThrows(EligibilityException.class, () -> companyStatusValidation.validate(companyProfileApi));

        assertEquals(EligibilityStatusCode.INVALID_COMPANY_STATUS, ex.getEligibilityStatusCode());
    }
}
