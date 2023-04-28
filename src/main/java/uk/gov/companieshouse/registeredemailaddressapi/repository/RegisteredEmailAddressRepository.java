package uk.gov.companieshouse.registeredemailaddressapi.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.gov.companieshouse.registeredemailaddressapi.model.dao.RegisteredEmailAddressDAO;

@Repository
public interface RegisteredEmailAddressRepository extends MongoRepository<RegisteredEmailAddressDAO, String> {
}
