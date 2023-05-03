package uk.gov.companieshouse.registeredemailaddressapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.transaction.Resource;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.registeredemailaddressapi.exception.ServiceException;
import uk.gov.companieshouse.registeredemailaddressapi.mapper.RegisteredEmailAddressMapper;
import uk.gov.companieshouse.registeredemailaddressapi.model.dao.RegisteredEmailAddressDAO;
import uk.gov.companieshouse.registeredemailaddressapi.model.dto.RegisteredEmailAddressDTO;
import uk.gov.companieshouse.registeredemailaddressapi.model.dto.RegisteredEmailAddress;
import uk.gov.companieshouse.registeredemailaddressapi.repository.RegisteredEmailAddressRepository;
import uk.gov.companieshouse.registeredemailaddressapi.utils.ApiLogger;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static uk.gov.companieshouse.registeredemailaddressapi.utils.Constants.*;

@Service
public class RegisteredEmailAddressService {
    private final RegisteredEmailAddressMapper registeredEmailAddressMapper;
    private final RegisteredEmailAddressRepository registeredEmailAddressRepository;

    private final TransactionService transactionService;


    @Autowired
    public RegisteredEmailAddressService(RegisteredEmailAddressMapper registeredEmailAddressMapper, RegisteredEmailAddressRepository registeredEmailAddressRepository, TransactionService transactionService) {
        this.registeredEmailAddressMapper = registeredEmailAddressMapper;
        this.registeredEmailAddressRepository = registeredEmailAddressRepository;
        this.transactionService = transactionService;

    }

    public RegisteredEmailAddressDTO createRegisteredEmailAddress(Transaction transaction,
                                                                  RegisteredEmailAddress registeredEmailAddressDTO,

                                                                  String requestId,
                                                                  String userId) throws ServiceException {

        ApiLogger.debugContext(requestId, " -  createRegisteredEmailAddress(...)");

        RegisteredEmailAddressDAO registeredEmailAddressDAO = registeredEmailAddressMapper
                .dtoToDao(registeredEmailAddressDTO);

        ApiLogger.debugContext(requestId, " -  insert registered email address into DB");


        RegisteredEmailAddressDAO createdRegisteredEmailAddress = registeredEmailAddressRepository
                .insert(registeredEmailAddressDAO);

        final String submissionId = createdRegisteredEmailAddress.getId();
        final String submissionUri = generateTransactionUri(transaction.getId(), submissionId);
        updateRegisteredEmailAddressWithMetaData(createdRegisteredEmailAddress, submissionUri, requestId, userId);

        // create the Resource to be added to the Transaction (includes various links to the resource)
        Resource registeredEmailAddressResource = createRegisteredEmailAddressTransactionResource(submissionUri);
//
//        // Update company name set on the transaction and add a link to newly created Registered Email address
//        // submission (aka resource) to the transaction (and potentially also a link for the 'resume' journey)
        updateTransactionWithLinksAndCompanyName(transaction,
                submissionUri, registeredEmailAddressResource, requestId);
//
        ApiLogger.infoContext(requestId, String.format("Registered Email address Submission created for transaction id: %s with registered email address submission id: %s",
                transaction.getId(), submissionId));

        ApiLogger.debugContext(requestId, " -  registered email address into DB success");

        return registeredEmailAddressMapper
                .daoToDto(createdRegisteredEmailAddress);


    }


    private String generateTransactionUri(String transactionId, String submissionId) {
        return String.format(TRANSACTION_URI_PATTERN, transactionId, submissionId);
    }

    private void updateRegisteredEmailAddressWithMetaData(RegisteredEmailAddressDAO submission,
                                                          String submissionUri,
                                                          String requestId,
                                                          String userId) {
        submission.setLinks(Collections.singletonMap(LINK_SELF, submissionUri));
        submission.setCreatedAt(LocalDateTime.now());
        submission.setHttpRequestId(requestId);
        submission.setCreatedByUserId(userId);

        registeredEmailAddressRepository.save(submission);
    }

    private Resource createRegisteredEmailAddressTransactionResource(String submissionUri) {
        var overseasEntityResource = new Resource();
        overseasEntityResource.setKind(FILING_KIND_OVERSEAS_ENTITY);

        Map<String, String> linksMap = new HashMap<>();
        linksMap.put("resource", submissionUri);
        linksMap.put("validation_status", submissionUri + VALIDATION_STATUS_URI_SUFFIX);

        overseasEntityResource.setLinks(linksMap);
        return overseasEntityResource;
    }

    private void updateTransactionWithLinksAndCompanyName(Transaction transaction,
                                                          String submissionUri,
                                                          Resource resource,
                                                          String loggingContext) throws ServiceException {

        transaction.setResources(Collections.singletonMap(submissionUri, resource));
        transactionService.updateTransaction(transaction, loggingContext);
    }

}




