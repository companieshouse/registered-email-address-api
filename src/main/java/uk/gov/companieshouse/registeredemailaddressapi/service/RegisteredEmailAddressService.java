package uk.gov.companieshouse.registeredemailaddressapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.registeredemailaddressapi.mapper.RegisteredEmailAddressMapper;
import uk.gov.companieshouse.registeredemailaddressapi.model.dao.RegisteredEmailAddressDAO;
import uk.gov.companieshouse.registeredemailaddressapi.model.dto.RegisteredEmailAddressDTO;
import uk.gov.companieshouse.registeredemailaddressapi.repository.RegisteredEmailAddressRepository;
import uk.gov.companieshouse.registeredemailaddressapi.utils.ApiLogger;

@Service
public class RegisteredEmailAddressService {
    private final RegisteredEmailAddressMapper registeredEmailAddressMapper;
    private final RegisteredEmailAddressRepository registeredEmailAddressRepository;

    @Autowired
    public RegisteredEmailAddressService(RegisteredEmailAddressMapper registeredEmailAddressMapper, RegisteredEmailAddressRepository registeredEmailAddressRepository) {
        this.registeredEmailAddressMapper = registeredEmailAddressMapper;
        this.registeredEmailAddressRepository = registeredEmailAddressRepository;
    }

    public RegisteredEmailAddressDTO createRegisteredEmailAddress(String transactionId,
                                               RegisteredEmailAddressDTO registeredEmailAddressDTO,
                                               String requestId,
                                               String userId){

        ApiLogger.debugContext(requestId, " -  createRegisteredEmailAddress(...)");

        RegisteredEmailAddressDAO registeredEmailAddressDAO = registeredEmailAddressMapper
                                                                .dtoToDao(registeredEmailAddressDTO);

        ApiLogger.debugContext(requestId, " -  insert registered email address into DB");


        RegisteredEmailAddressDAO createdRegisteredEmailAddress = registeredEmailAddressRepository
                .insert(registeredEmailAddressDAO);


        return registeredEmailAddressMapper
                .daoToDto(createdRegisteredEmailAddress);

    }

}
