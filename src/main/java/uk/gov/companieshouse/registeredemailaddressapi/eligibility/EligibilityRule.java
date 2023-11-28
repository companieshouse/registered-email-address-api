package uk.gov.companieshouse.registeredemailaddressapi.eligibility;

import uk.gov.companieshouse.registeredemailaddressapi.exception.EligibilityException;
import uk.gov.companieshouse.registeredemailaddressapi.exception.ServiceException;

public interface EligibilityRule<T> {
    void validate(T input) throws EligibilityException, ServiceException;
}
