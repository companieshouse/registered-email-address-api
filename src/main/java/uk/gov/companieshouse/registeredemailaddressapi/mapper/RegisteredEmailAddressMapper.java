package uk.gov.companieshouse.registeredemailaddressapi.mapper;

import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.registeredemailaddressapi.model.dao.RegisteredEmailAddressDAO;
import uk.gov.companieshouse.registeredemailaddressapi.model.dto.RegisteredEmailAddressDTO;

@Component
@Mapper(componentModel = "spring")
public interface RegisteredEmailAddressMapper {

    RegisteredEmailAddressDTO daoToDto(RegisteredEmailAddressDAO registeredEmailAddressDAO);

    RegisteredEmailAddressDAO dtoToDao(RegisteredEmailAddressDTO registeredEmailAddressDTO);
}
