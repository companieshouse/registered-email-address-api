package uk.gov.companieshouse.registeredemailaddressapi.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.company.RegisteredEmailAddressJson;
import uk.gov.companieshouse.registeredemailaddressapi.exception.ServiceException;
import uk.gov.companieshouse.registeredemailaddressapi.utils.ApiLogger;

import java.util.HashMap;

@Service
@Qualifier("mockEmailRetrieverImpl")
@ConditionalOnExpression("'${env.name}'.equals('stagsbox') || '${env.name}'.equals('livesbox')")
public class MockEmailRetrieverImpl implements PrivateEmailDataRetrievalService {

    @Override
    public RegisteredEmailAddressJson getRegisteredEmailAddress(String companyNumber) throws ServiceException {

        var logMap = new HashMap<String, Object>();

        if (companyNumber.endsWith("ERR")) {
            ApiLogger.info("Mocking Registered Email Address for sandbox environment", logMap);
            RegisteredEmailAddressJson registeredEmailAddressJson = new RegisteredEmailAddressJson();
            registeredEmailAddressJson.setRegisteredEmailAddress("mockexistingemail@companieshouse.gov.uk");
            return registeredEmailAddressJson;
        } else {
            ApiLogger.info("Error Retrieving Registered Email Address for Company " + companyNumber, logMap);
            throw new ServiceException("Error Retrieving Registered Email Address for Company");
        }
    }
}
