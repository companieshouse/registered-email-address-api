package uk.gov.companieshouse.registeredemailaddressapi.service;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.company.RegisteredEmailAddressJson;
import uk.gov.companieshouse.registeredemailaddressapi.exception.ServiceException;

@Component
public interface PrivateDataRetrievalService {

    RegisteredEmailAddressJson getRegisteredEmailAddress(String companyNumber) throws ServiceException;
}
