package uk.gov.companieshouse.registeredemailaddressapi.service;

import static java.lang.String.format;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.company.RegisteredEmailAddressJson;
import uk.gov.companieshouse.registeredemailaddressapi.exception.ServiceException;
import uk.gov.companieshouse.registeredemailaddressapi.utils.ApiLogger;

import java.util.HashMap;

@Service
@ConditionalOnExpression("'${env.name}'.equals('stagsbox') || '${env.name}'.equals('livesbox')")
public class MockEmailRetrieverImpl implements PrivateDataRetrievalService {

    private static final String MOCK_EMAIL = "mockexistingemail@companieshouse.gov.uk";

    @Override
    public RegisteredEmailAddressJson getRegisteredEmailAddress(String companyNumber) throws ServiceException {

        var logMap = new HashMap<String, Object>();
        ApiLogger.info(format("Mocking Registered Email Address lookup for sandbox environment - returning mock email address for company number %s", companyNumber), logMap);
        var registeredEmailAddressJson = new RegisteredEmailAddressJson();
        registeredEmailAddressJson.setRegisteredEmailAddress(MOCK_EMAIL);
        return registeredEmailAddressJson;

    }
}
