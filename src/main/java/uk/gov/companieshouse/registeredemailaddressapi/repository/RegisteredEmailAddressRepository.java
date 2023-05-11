package uk.gov.companieshouse.registeredemailaddressapi.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import uk.gov.companieshouse.registeredemailaddressapi.model.dao.RegisteredEmailAddressDAO;

import org.springframework.stereotype.Repository;

@Repository
public interface RegisteredEmailAddressRepository extends MongoRepository<RegisteredEmailAddressDAO, String> {
}
