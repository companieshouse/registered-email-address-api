package uk.gov.companieshouse.registeredemailaddressapi.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import uk.gov.companieshouse.registeredemailaddressapi.model.dao.RegisteredEmailAddressDAO;

import org.springframework.stereotype.Repository;

@Repository
public interface RegisteredEmailAddressRepository extends MongoRepository<RegisteredEmailAddressDAO, String> {

    @Query("{transaction_id:'?0'}")
    RegisteredEmailAddressDAO findByTransactionId(String transactionId);
}
