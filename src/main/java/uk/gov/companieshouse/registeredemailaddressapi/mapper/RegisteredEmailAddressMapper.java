package uk.gov.companieshouse.registeredemailaddressapi.mapper;

import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.registeredemailaddressapi.model.dao.RegisteredEmailAddressDAO;
import uk.gov.companieshouse.registeredemailaddressapi.model.dto.RegisteredEmailAddressDTO;
import uk.gov.companieshouse.registeredemailaddressapi.model.dto.RegisteredEmailAddress;

@Component
@Mapper(componentModel = "spring")
public interface RegisteredEmailAddressMapper {

      RegisteredEmailAddressDTO daoToDto(RegisteredEmailAddressDAO registeredEmailAddressDAO);

      RegisteredEmailAddressDAO dtoToDao(RegisteredEmailAddress registeredEmailAddressDTO);
}
