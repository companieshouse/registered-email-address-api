package uk.gov.companieshouse.registeredemailaddressapi.service;

import static java.lang.String.format;
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
public class MockEmailRetrieverImpl implements PrivateDataRetrievalService {

    @Override
    public RegisteredEmailAddressJson getRegisteredEmailAddress(String companyNumber) throws ServiceException {

        var logMap = new HashMap<String, Object>();

        if (companyNumber.endsWith("ERR")) {
            ApiLogger.info(format("Mocking Registered Email Address lookup for sandbox environment - returning mock email address for company number %s", companyNumber), logMap);
            RegisteredEmailAddressJson registeredEmailAddressJson = new RegisteredEmailAddressJson();
            registeredEmailAddressJson.setRegisteredEmailAddress("mockexistingemail@companieshouse.gov.uk");
            return registeredEmailAddressJson;
        } else {
            ApiLogger.info(format("Mocking Registered Email Address lookup for sandbox environment - returning no email address as company number %s does not end with ERR", companyNumber), logMap);
            return null;
        }
    }
}
