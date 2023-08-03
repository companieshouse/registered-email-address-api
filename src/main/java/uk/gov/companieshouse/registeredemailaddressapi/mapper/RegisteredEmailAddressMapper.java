package uk.gov.companieshouse.registeredemailaddressapi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.registeredemailaddressapi.model.dao.RegisteredEmailAddressDAO;
import uk.gov.companieshouse.registeredemailaddressapi.model.dto.RegisteredEmailAddressDTO;
import uk.gov.companieshouse.registeredemailaddressapi.model.dto.RegisteredEmailAddressResponseDTO;

@Component
@Mapper(componentModel = "spring")
public interface RegisteredEmailAddressMapper {

    RegisteredEmailAddressResponseDTO daoToDto(RegisteredEmailAddressDAO registeredEmailAddressDAO);

    @Mapping(target = "data.registeredEmailAddress", source = "registeredEmailAddress")
    @Mapping(target = "data.acceptAppropriateEmailAddressStatement", source = "acceptAppropriateEmailAddressStatement")
    RegisteredEmailAddressDAO dtoToDao( RegisteredEmailAddressDTO registeredEmailAddressDTO);


}
