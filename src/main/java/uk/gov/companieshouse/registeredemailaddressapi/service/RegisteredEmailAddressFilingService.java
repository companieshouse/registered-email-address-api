package uk.gov.companieshouse.registeredemailaddressapi.service;

import java.util.Objects;
import java.util.Map;
import java.util.Optional;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.function.Supplier;
import java.time.format.DateTimeFormatter;
import static java.lang.String.format;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.filinggenerator.FilingApi;
import uk.gov.companieshouse.registeredemailaddressapi.utils.ApiLogger;
import uk.gov.companieshouse.registeredemailaddressapi.exception.SubmissionNotFoundException;
import uk.gov.companieshouse.registeredemailaddressapi.model.dto.RegisteredEmailAddressDTO;
import uk.gov.companieshouse.registeredemailaddressapi.repository.RegisteredEmailAddressRepository;

import static uk.gov.companieshouse.registeredemailaddressapi.utils.Constants.COMPANY_NUMBER;
import static uk.gov.companieshouse.registeredemailaddressapi.utils.Constants.TRANSACTION_ID_KEY;
import static uk.gov.companieshouse.registeredemailaddressapi.utils.Constants.DATE_FORMATTER_PATTERN;
import static uk.gov.companieshouse.registeredemailaddressapi.utils.Constants.FILING_KIND;

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
        throws SubmissionNotFoundException {       
        var filing = new FilingApi();
        filing.setKind(FILING_KIND);
        setRegisteredEmailAddressFilingData(filing, transaction);
        return filing;
    }

    private void setRegisteredEmailAddressFilingData(FilingApi filing, Transaction transaction)
        throws SubmissionNotFoundException {
        var transactionId = transaction.getId();
        var logMap = new HashMap<String, Object>();
        logMap.put(TRANSACTION_ID_KEY, transaction.getId());

        Map<String, Object> registeredEmailAddressData = new HashMap<>();

        var registeredEmailAddressDAO = registeredEmailAddressRepository.findByTransactionId(transactionId);

        RegisteredEmailAddressDTO dataDto = setRegisteredEmailAddressSubmissionData(registeredEmailAddressData, registeredEmailAddressDAO.getId(), logMap);
        registeredEmailAddressData.put(COMPANY_NUMBER, transaction.getCompanyNumber());
        filing.setData(registeredEmailAddressData);
        setDescriptionFields(filing, dataDto.isForUpdate());
    }

    private RegisteredEmailAddressDTO setRegisteredEmailAddressSubmissionData(Map<String, Object> data, String registeredEmailAddressId, Map<String, Object> logMap)
        throws SubmissionNotFoundException {
        Optional<RegisteredEmailAddressDTO> submissionOpt =
            registeredEmailAddressService.getRegisteredEmailAddressSubmission(registeredEmailAddressId);

        RegisteredEmailAddressDTO registeredEmailAddressSubmission = submissionOpt
                .orElseThrow(() ->
                        new SubmissionNotFoundException(
                                format(SERVICE_EXCEPTION, registeredEmailAddressId)));

        if (Objects.isNull(registeredEmailAddressSubmission.getRegisteredEmailAddress())) {
            data.put(FILING_KIND, null);
        } else {
            data.put(FILING_KIND, registeredEmailAddressSubmission.getRegisteredEmailAddress());
        }
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