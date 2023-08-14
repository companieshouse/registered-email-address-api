package uk.gov.companieshouse.registeredemailaddressapi.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.filinggenerator.FilingApi;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.registeredemailaddressapi.exception.NotFoundException;
import uk.gov.companieshouse.registeredemailaddressapi.model.dto.RegisteredEmailAddressResponseDTO;
import uk.gov.companieshouse.registeredemailaddressapi.repository.RegisteredEmailAddressRepository;
import uk.gov.companieshouse.registeredemailaddressapi.utils.ApiLogger;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import static java.lang.String.format;
import static uk.gov.companieshouse.registeredemailaddressapi.utils.Constants.*;

@Service
public class RegisteredEmailAddressFilingService {
    
    // class constants
    private static final String SERVICE_EXCEPTION = "Empty data set returned when generating filing for %s";
    private static final String DEBUG_MESSAGE     = "Submission data has been set on filing";
    private static final String DATE_PLACEHOLDER  = "{date}";

    @Value("${REGISTERED_EMAIL_ADDRESS_FILING_DESCRIPTION_IDENTIFIER}")
    private String filingDescriptionIdentifier;

    @Value("${REGISTERED_EMAIL_ADDRESS_FILING_DESCRIPTION}")
    private String filingDescription;

    @Value("${REGISTERED_EMAIL_ADDRESS_UPDATE_FILING_DESCRIPTION}")
    private String updateFilingDescription;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMATTER_PATTERN);
    private final RegisteredEmailAddressService registeredEmailAddressService;
    private final Supplier<LocalDate> dateNowSupplier;
    private final RegisteredEmailAddressRepository registeredEmailAddressRepository;

    public RegisteredEmailAddressFilingService( RegisteredEmailAddressService registeredEmailAddressService,
                                                RegisteredEmailAddressRepository registeredEmailAddressRepository,
                                                Supplier<LocalDate> dateNowSupplier) {
        this.registeredEmailAddressService = registeredEmailAddressService;
        this.registeredEmailAddressRepository = registeredEmailAddressRepository;
        this.dateNowSupplier = dateNowSupplier;
    }

    public FilingApi generateRegisteredEmailAddressFilings(Transaction transaction)
        throws NotFoundException {
        var filing = new FilingApi();
        filing.setKind(FILING_KIND);
        setRegisteredEmailAddressFilingData(filing, transaction);
        return filing;
    }

    private void setRegisteredEmailAddressFilingData(FilingApi filing, Transaction transaction)
        throws NotFoundException {
        var transactionId = transaction.getId();
        var logMap = new HashMap<String, Object>();
        logMap.put(TRANSACTION_ID_KEY, transaction.getId());

        Map<String, Object> registeredEmailAddressData = new HashMap<>();

        var registeredEmailAddressDAO = registeredEmailAddressRepository.findByTransactionId(transactionId);

        RegisteredEmailAddressResponseDTO dataDto = setRegisteredEmailAddressSubmissionData(registeredEmailAddressData, registeredEmailAddressDAO.getId(), logMap);
        registeredEmailAddressData.put(COMPANY_NUMBER, transaction.getCompanyNumber());
        filing.setData(registeredEmailAddressData);
        setDescriptionFields(filing, dataDto.isForUpdate());
    }

    private RegisteredEmailAddressResponseDTO setRegisteredEmailAddressSubmissionData(Map<String, Object> data, String registeredEmailAddressId, Map<String, Object> logMap)
        throws NotFoundException {
        Optional<RegisteredEmailAddressResponseDTO> submissionOpt =
            registeredEmailAddressService.getRegisteredEmailAddressSubmission(registeredEmailAddressId);

        RegisteredEmailAddressResponseDTO registeredEmailAddressSubmission = submissionOpt
                .orElseThrow(() ->
                        new NotFoundException(
                                format(SERVICE_EXCEPTION, registeredEmailAddressId)));

        if (Objects.isNull(registeredEmailAddressSubmission.getData().getRegisteredEmailAddress())) {
            data.put(REGISTERED_EMAIL_ADDRESS, null);
        } else {
            data.put(REGISTERED_EMAIL_ADDRESS, registeredEmailAddressSubmission.getData().getRegisteredEmailAddress());
        }
        data.put(ACCEPT_EMAIL_STATEMENT, registeredEmailAddressSubmission.getData().isAcceptAppropriateEmailAddressStatement());
        ApiLogger.debug(DEBUG_MESSAGE, logMap);
        return registeredEmailAddressSubmission;
    }

    private void setDescriptionFields(FilingApi filing, boolean isUpdateFiling) {
        String formattedDate = dateNowSupplier.get().format(formatter);
        filing.setDescriptionIdentifier(filingDescriptionIdentifier);
        if (isUpdateFiling) {
            filing.setDescription(updateFilingDescription.replace(DATE_PLACEHOLDER, formattedDate));
        } else {
            filing.setDescription(filingDescription.replace(DATE_PLACEHOLDER, formattedDate));
        }
        Map<String, String> values = new HashMap<>();
        filing.setDescriptionValues(values);
    }

}
