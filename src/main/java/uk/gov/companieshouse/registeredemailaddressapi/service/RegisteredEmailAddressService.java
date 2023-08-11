package uk.gov.companieshouse.registeredemailaddressapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.GenerateEtagUtil;
import uk.gov.companieshouse.api.model.transaction.Resource;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusResponse;
import uk.gov.companieshouse.registeredemailaddressapi.exception.NoExistingEmailAddressException;
import uk.gov.companieshouse.registeredemailaddressapi.exception.ServiceException;
import uk.gov.companieshouse.registeredemailaddressapi.exception.SubmissionAlreadyExistsException;
import uk.gov.companieshouse.registeredemailaddressapi.exception.SubmissionNotFoundException;
import uk.gov.companieshouse.registeredemailaddressapi.mapper.RegisteredEmailAddressMapper;
import uk.gov.companieshouse.registeredemailaddressapi.model.dao.RegisteredEmailAddressDAO;
import uk.gov.companieshouse.registeredemailaddressapi.model.dto.RegisteredEmailAddressDTO;
import uk.gov.companieshouse.registeredemailaddressapi.model.dto.RegisteredEmailAddressResponseDTO;
import uk.gov.companieshouse.registeredemailaddressapi.repository.RegisteredEmailAddressRepository;
import uk.gov.companieshouse.registeredemailaddressapi.utils.ApiLogger;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Map.entry;
import static uk.gov.companieshouse.api.model.transaction.TransactionStatus.OPEN;
import static uk.gov.companieshouse.registeredemailaddressapi.utils.Constants.*;

@Service
public class RegisteredEmailAddressService {

    private final RegisteredEmailAddressMapper registeredEmailAddressMapper;
    private final RegisteredEmailAddressRepository registeredEmailAddressRepository;
    private final TransactionService transactionService;
    private final ValidationService validationService;
    private final PrivateDataRetrievalService privateDataRetrievalService;

    private static final String TRANSACTION_REFERENCE_TEMPLATE = "UpdateRegisteredEmailAddressReference_%s";

    @Autowired
    public RegisteredEmailAddressService(
            RegisteredEmailAddressMapper registeredEmailAddressMapper,
            RegisteredEmailAddressRepository registeredEmailAddressRepository,
            TransactionService transactionService,
            ValidationService validationService,
            PrivateDataRetrievalService privateDataRetrievalService) {
        this.registeredEmailAddressMapper = registeredEmailAddressMapper;
        this.registeredEmailAddressRepository = registeredEmailAddressRepository;
        this.transactionService = transactionService;
        this.validationService = validationService;
        this.privateDataRetrievalService = privateDataRetrievalService;
    }

    public RegisteredEmailAddressResponseDTO createRegisteredEmailAddress(Transaction transaction,
                                                                  RegisteredEmailAddressDTO registeredEmailAddressDTO,
                                                                  String requestId,
                                                                  String userId) throws ServiceException, NoExistingEmailAddressException, SubmissionAlreadyExistsException {

        ApiLogger.debugContext(requestId, " -  createRegisteredEmailAddress(...)");

        // Throws NoExistingEmailAddressException
        checkCompanyHasExistingRegisteredEmailAddress(transaction, requestId);

        // Throws SubmissionAlreadyExistsException
        checkHasExistingReaSubmission(transaction, requestId);

        var registeredEmailAddressDAO = registeredEmailAddressMapper
                .dtoToDao(registeredEmailAddressDTO);

        registeredEmailAddressDAO.setTransactionId(transaction.getId());
        registeredEmailAddressDAO.getData().setEtag(GenerateEtagUtil.generateEtag());
        registeredEmailAddressDAO.setCreatedAt(LocalDateTime.now());
        registeredEmailAddressDAO.getData().setKind(FILING_KIND);

        ApiLogger.debugContext(requestId, " -  insert registered email address into DB");

        RegisteredEmailAddressDAO createdRegisteredEmailAddress = registeredEmailAddressRepository
                .insert(registeredEmailAddressDAO);

        final String submissionId = createdRegisteredEmailAddress.getId();
        final String submissionUri = generateTransactionUri(transaction.getId());
        updateRegisteredEmailAddressWithMetaData(createdRegisteredEmailAddress, submissionUri, requestId, userId);

        // create the Resource to be added to the Transaction (includes various links to the resource)
        var registeredEmailAddressResource = createRegisteredEmailAddressTransactionResource(submissionUri);
        
        // Update company name set on the transaction and add a link to newly created Registered Email address
        // submission (aka resource) to the transaction (and potentially also a link for the 'resume' journey)
        updateTransactionWithLinks(transaction,
                submissionUri, registeredEmailAddressResource, requestId, createdRegisteredEmailAddress.getId());

        ApiLogger.infoContext(requestId, format("Registered Email address Submission created for transaction id: %s with registered email address submission id: %s",
                transaction.getId(), submissionId));

        ApiLogger.debugContext(requestId, " -  registered email address into DB success");

        return registeredEmailAddressMapper.daoToDto(createdRegisteredEmailAddress);
    }

    public RegisteredEmailAddressResponseDTO updateRegisteredEmailAddress(Transaction transaction,
                                                                          RegisteredEmailAddressDTO registeredEmailAddressDTO,
                                                                          String requestId,
                                                                          String userId) throws ServiceException, SubmissionNotFoundException, NoExistingEmailAddressException {

        ApiLogger.debugContext(requestId, " -  updateRegisteredEmailAddress(...)");
            if (transaction.getStatus() != null && transaction.getStatus().equals(OPEN)) {
                var registeredEmailAddress = getRegisteredEmailAddressDAO(transaction.getId(), requestId);

                // Throws NoExistingEmailAddressException
                checkCompanyHasExistingRegisteredEmailAddress(transaction, requestId);

                if(!registeredEmailAddressDTO.getRegisteredEmailAddress().isEmpty()){
                    registeredEmailAddress.getData()
                            .setRegisteredEmailAddress(registeredEmailAddressDTO.getRegisteredEmailAddress());
                }

                if(!registeredEmailAddressDTO.isAcceptAppropriateEmailAddressStatement() ==
                        registeredEmailAddress.getData().isAcceptAppropriateEmailAddressStatement()){
                    registeredEmailAddress.getData()
                            .setAcceptAppropriateEmailAddressStatement(registeredEmailAddressDTO.isAcceptAppropriateEmailAddressStatement());
                }



                registeredEmailAddress.setLastModifiedByUserId(userId);
                registeredEmailAddress.setUpdatedAt(LocalDateTime.now());
                RegisteredEmailAddressDAO createdRegisteredEmailAddress = registeredEmailAddressRepository
                        .save(registeredEmailAddress);

                return registeredEmailAddressMapper
                        .daoToDto(createdRegisteredEmailAddress);

            } else {
                String message = format("Transaction %s can only be edited when status is %s ",
                        transaction.getId(),
                        OPEN);
                ApiLogger.infoContext(requestId, message);
                throw new ServiceException(message);
        }

    }

    public ValidationStatusResponse getValidationStatus(String transactionId, String requestId) throws SubmissionNotFoundException {
        try {
            var registeredEmailAddress = registeredEmailAddressRepository
                    .findByTransactionId(transactionId);
            return validationService.validateRegisteredEmailAddress(registeredEmailAddress, requestId);
        } catch (Exception ex) {
            var message = format("Registered Email Address for TransactionId : %s Not Found",
                    transactionId);
            ApiLogger.errorContext(requestId, message, ex);
            throw new SubmissionNotFoundException(message, ex);
        }
    }

    public RegisteredEmailAddressResponseDTO getRegisteredEmailAddress(String transactionId, String requestId) throws SubmissionNotFoundException {
        var registeredEmailAddressDAO = getRegisteredEmailAddressDAO(transactionId, requestId);
        return  registeredEmailAddressMapper.daoToDto(registeredEmailAddressDAO);
    }



    private RegisteredEmailAddressDAO getRegisteredEmailAddressDAO(String transactionId, String requestId) throws SubmissionNotFoundException {
        var registeredEmailAddress = registeredEmailAddressRepository.findByTransactionId(transactionId);

        if (registeredEmailAddress == null) {
            var message = format("Registered Email Address for TransactionId : %s Not Found", transactionId);
            throw new SubmissionNotFoundException(message);
        }

        ApiLogger.debugContext(requestId, format("Registered Email Address found for Transaction %s.", transactionId));

        return registeredEmailAddress;
    }
    public Optional<RegisteredEmailAddressResponseDTO> getRegisteredEmailAddressSubmission(String submissionId) {
        var submission = registeredEmailAddressRepository.findById(submissionId);
        if (submission.isPresent()) {
            var registeredEmailAddressSubmissionDao = submission.get();
            ApiLogger.info(format("%s: Registered Email Address Submission found. About to return", registeredEmailAddressSubmissionDao.getId()));
            return Optional.of(registeredEmailAddressMapper.daoToDto(registeredEmailAddressSubmissionDao));
        } else {
            return Optional.empty();
        }
    }

    //Private Methods

    private void checkCompanyHasExistingRegisteredEmailAddress(Transaction transaction, String requestId) throws ServiceException, NoExistingEmailAddressException {
        ApiLogger.infoContext(requestId, String.format("Check Registered Email Address exists for %s", transaction.getCompanyNumber()));
        var registeredEmailAddressJson = privateDataRetrievalService.getRegisteredEmailAddress(transaction.getCompanyNumber());

        if (registeredEmailAddressJson != null &&
            registeredEmailAddressJson.getRegisteredEmailAddress() != null &&
            !registeredEmailAddressJson.getRegisteredEmailAddress().isEmpty()) {
            ApiLogger.infoContext(requestId, String.format("Retrieved registered email address %s for Company Number %s",
                    registeredEmailAddressJson.getRegisteredEmailAddress(), transaction.getCompanyNumber()));
            return;
        }
        String message = format("Transaction id: %s; company number: %s has no existing Registered Email Address",
                transaction.getId(), transaction.getCompanyNumber());
        ApiLogger.infoContext(requestId, message);
        throw new NoExistingEmailAddressException(message);

    }
    private void checkHasExistingReaSubmission(Transaction transaction, String requestId) throws SubmissionAlreadyExistsException {
        if(transaction.getResources() != null && transaction.getResources().entrySet().stream().anyMatch(resourceEntry ->
                FILING_KIND.equals(resourceEntry.getValue().getKind()))){
            String message = format("Transaction id: %s has an existing Registered Email Address submission",
                    transaction.getId());
            ApiLogger.infoContext(requestId, message);
            throw new SubmissionAlreadyExistsException(message);
        }
    }

    private String generateTransactionUri(String transactionId) {
        return format(TRANSACTION_URI_PATTERN, transactionId);
    }

    private void updateRegisteredEmailAddressWithMetaData(RegisteredEmailAddressDAO submission,
                                                          String submissionUri,
                                                          String requestId,
                                                          String userId) {

        Map<String, String> links = Map.ofEntries(
                entry(LINK_SELF, submissionUri),
                entry(LINK_VALIDATION, submissionUri + VALIDATION_STATUS_URI_SUFFIX)
        );
        submission.setLinks(links);
        submission.setUpdatedAt(LocalDateTime.now());
        submission.setHttpRequestId(requestId);
        submission.setLastModifiedByUserId(userId);
        registeredEmailAddressRepository.save(submission);
    }

    private Resource createRegisteredEmailAddressTransactionResource(String submissionUri) {
        var registeredEmailAddressResource = new Resource();
        registeredEmailAddressResource.setKind(FILING_KIND);

        Map<String, String> linksMap = new HashMap<>();
        linksMap.put("resource", submissionUri);
        linksMap.put("validation_status", submissionUri + VALIDATION_STATUS_URI_SUFFIX);

        registeredEmailAddressResource.setLinks(linksMap);
        return registeredEmailAddressResource;
    }

    private void updateTransactionWithLinks(Transaction transaction,
                                            String submissionUri,
                                            Resource resource,
                                            String loggingContext,
                                            String objectId) throws ServiceException {

        transaction.setResources(Collections.singletonMap(submissionUri, resource));
        transaction.setReference(format(TRANSACTION_REFERENCE_TEMPLATE, objectId));
        transactionService.updateTransaction(transaction, loggingContext);
    }

}




