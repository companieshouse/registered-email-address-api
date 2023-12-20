package uk.gov.companieshouse.registeredemailaddressapi.integration.utils;

import java.util.Random;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;

import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.model.company.RegisteredEmailAddressJson;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.registeredemailaddressapi.model.dao.RegisteredEmailAddressDAO;
import uk.gov.companieshouse.registeredemailaddressapi.model.dao.RegisteredEmailAddressData;
import uk.gov.companieshouse.registeredemailaddressapi.model.dto.RegisteredEmailAddressDTO;
import uk.gov.companieshouse.registeredemailaddressapi.model.dto.RegisteredEmailAddressResponseDTO;
import uk.gov.companieshouse.registeredemailaddressapi.model.dto.RegisteredEmailAddressResponseData;

public class Helper {

    public Transaction generateTransaction(){
        Transaction transaction = new Transaction();
        Random random = new Random(Integer.MAX_VALUE);
        String rand = String.format("%018d", random.nextInt()); // 18 digit int
        String[] tranId = rand.split("(?<=\\G.{6})"); // split into groups of 6
        transaction.setId(String.join("-", tranId)); // join with -
        return transaction;
    }

    public Transaction generateTransaction(String companyNumber) {
        Transaction transaction = generateTransaction();
        transaction.setCompanyNumber(companyNumber);
        return transaction;
    }

    public CompanyProfileApi generateCompanyProfileApi(String companyNumber) {
        CompanyProfileApi companyProfileApi = new CompanyProfileApi();
        companyProfileApi.setCompanyNumber(companyNumber);
        companyProfileApi.setCompanyStatus("active");
        companyProfileApi.setType("ltd");
        return companyProfileApi;
    }

    public RegisteredEmailAddressJson generateRegisteredEmailAddressJson(String companyEmail) {
        RegisteredEmailAddressJson response = new RegisteredEmailAddressJson();
        response.setRegisteredEmailAddress(companyEmail);
        return response;
    }

    public RegisteredEmailAddressDTO generateRegisteredEmailAddressDTO(String email){
        RegisteredEmailAddressDTO registeredEmailAddressDTO = new RegisteredEmailAddressDTO();
        registeredEmailAddressDTO.setRegisteredEmailAddress(email);
        registeredEmailAddressDTO.setAcceptAppropriateEmailAddressStatement(true);
        return registeredEmailAddressDTO;
    }

    public RegisteredEmailAddressDAO generateRegisteredEmailAddressDAO(String email, String transactionId){
        RegisteredEmailAddressData registeredEmailAddressData =  new RegisteredEmailAddressData();
        registeredEmailAddressData.setRegisteredEmailAddress(email);
        registeredEmailAddressData.setAcceptAppropriateEmailAddressStatement(true);
        RegisteredEmailAddressDAO registeredEmailAddressDAO =  new RegisteredEmailAddressDAO();
        registeredEmailAddressDAO.setData(registeredEmailAddressData);
        registeredEmailAddressDAO.setTransactionId(transactionId);
        registeredEmailAddressDAO.setId(UUID.randomUUID().toString());
        return registeredEmailAddressDAO;
    }

    public RegisteredEmailAddressResponseDTO generateRegisteredEmailAddressResponseDTO(String email, String transactionId){
        RegisteredEmailAddressResponseData registeredEmailAddressData =  new RegisteredEmailAddressResponseData();
        registeredEmailAddressData.setRegisteredEmailAddress(email);
        registeredEmailAddressData.setAcceptAppropriateEmailAddressStatement(true);
        RegisteredEmailAddressResponseDTO registeredEmailAddressResponseDTO =  new RegisteredEmailAddressResponseDTO();
        registeredEmailAddressResponseDTO.setData(registeredEmailAddressData);
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
