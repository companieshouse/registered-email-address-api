package uk.gov.companieshouse.registeredemailaddressapi.integration.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.registeredemailaddressapi.model.dao.RegisteredEmailAddressDAO;
import uk.gov.companieshouse.registeredemailaddressapi.model.dao.RegisteredEmailAddressData;
import uk.gov.companieshouse.registeredemailaddressapi.model.dto.RegisteredEmailAddressDTO;
import uk.gov.companieshouse.registeredemailaddressapi.model.dto.RegisteredEmailAddressResponseDTO;
import uk.gov.companieshouse.registeredemailaddressapi.model.dto.RegisteredEmailAddressResponseData;

import java.util.UUID;

public class Helper {

    public Transaction generateTransaction(){
        Transaction transaction = new Transaction();
        String id = UUID.randomUUID().toString();
        transaction.setId(id);
        return transaction;
    }
    public RegisteredEmailAddressDTO generateRegisteredEmailAddressDTO(String email){
        RegisteredEmailAddressDTO registeredEmailAddressDTO = new RegisteredEmailAddressDTO();
        registeredEmailAddressDTO.setRegisteredEmailAddress(email);
        return registeredEmailAddressDTO;
    }

    public RegisteredEmailAddressDAO generateRegisteredEmailAddressDAO(String email, String transactionId){
        RegisteredEmailAddressData registeredEmailAddressData =  new RegisteredEmailAddressData();
        registeredEmailAddressData.setRegisteredEmailAddress(email);
        RegisteredEmailAddressDAO registeredEmailAddressDAO =  new RegisteredEmailAddressDAO();
        registeredEmailAddressDAO.setData(registeredEmailAddressData);
        registeredEmailAddressDAO.setTransactionId(transactionId);
        registeredEmailAddressDAO.setId(UUID.randomUUID().toString());
        return registeredEmailAddressDAO;
    }

    public RegisteredEmailAddressResponseDTO generateRegisteredEmailAddressResponseDTO(String email, String transactionId){
        RegisteredEmailAddressResponseData registeredEmailAddressData =  new RegisteredEmailAddressResponseData();
        registeredEmailAddressData.setRegisteredEmailAddress(email);
        RegisteredEmailAddressResponseDTO registeredEmailAddressResponseDTO =  new RegisteredEmailAddressResponseDTO();
        registeredEmailAddressResponseDTO.setData(registeredEmailAddressData);
        registeredEmailAddressResponseDTO.setTransactionId(transactionId);
        registeredEmailAddressResponseDTO.setId(UUID.randomUUID().toString());
        return registeredEmailAddressResponseDTO;
    }
    public String writeToJson(Object object) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer();
        return ow.writeValueAsString(object);

    }
}
