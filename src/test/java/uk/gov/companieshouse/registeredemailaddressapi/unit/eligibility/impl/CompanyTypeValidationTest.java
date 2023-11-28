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
import uk.gov.companieshouse.registeredemailaddressapi.eligibility.impl.CompanyTypeValidation;
import uk.gov.companieshouse.registeredemailaddressapi.exception.EligibilityException;

class CompanyTypeValidationTest {

    private static final String ALLOWED_VALUE = "Allowed_Type";
    private static final Set<String> ALLOWED_LIST = Collections.singleton(ALLOWED_VALUE);

    private CompanyTypeValidation companyTypeValidation;

    @BeforeEach
    void init() {
        companyTypeValidation = new CompanyTypeValidation(ALLOWED_LIST);
    }

    @Test
    void validateThrowsOnInvalidType() {
        CompanyProfileApi companyProfileApi = new CompanyProfileApi();
        companyProfileApi.setType("Invalid_Type");

        var ex = assertThrows(EligibilityException.class, () -> companyTypeValidation.validate(companyProfileApi));

        assertEquals(EligibilityStatusCode.INVALID_COMPANY_TYPE, ex.getEligibilityStatusCode());
    }

    @Test
    void validateDoesNotThrowOnValidType() {
        CompanyProfileApi companyProfileApi = new CompanyProfileApi();
        companyProfileApi.setType(ALLOWED_VALUE);

        assertDoesNotThrow(() -> companyTypeValidation.validate(companyProfileApi));
    }

    @Test
    void validateThrowsOnNullType() {
        CompanyProfileApi companyProfileApi = new CompanyProfileApi();
        companyProfileApi.setType(null);

        var ex = assertThrows(EligibilityException.class, () -> companyTypeValidation.validate(companyProfileApi));

        assertEquals(EligibilityStatusCode.INVALID_COMPANY_TYPE, ex.getEligibilityStatusCode());
    }
}
