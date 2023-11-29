package uk.gov.companieshouse.registeredemailaddressapi.service;

import uk.gov.companieshouse.api.model.company.RegisteredEmailAddressJson;
import uk.gov.companieshouse.registeredemailaddressapi.exception.ServiceException;

public interface PrivateDataRetrievalService {

    RegisteredEmailAddressJson getRegisteredEmailAddress(String companyNumber) throws ServiceException;
}
