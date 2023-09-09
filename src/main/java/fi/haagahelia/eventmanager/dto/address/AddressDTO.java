package fi.haagahelia.eventmanager.dto.address;

import fi.haagahelia.eventmanager.dto.country.CountryDTO;
import fi.haagahelia.eventmanager.domain.Address;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.hateoas.RepresentationModel;

@Builder
@Data
@EqualsAndHashCode(callSuper = false)
public class AddressDTO extends RepresentationModel<AddressDTO>{
    private Long id;
    private String number;
    private String street;
    private String city;
    private String postcode;
    private CountryDTO country;

    public static AddressDTO convert(Address address) {
        AddressDTOBuilder daoBuilder = builder()
                .id(address.getId())
                .number(address.getNumber())
                .street(address.getStreet())
                .city(address.getCity())
                .postcode(address.getPostcode());
        if (address.getCountry() != null) {
            daoBuilder.country(CountryDTO.convert(address.getCountry()));
        }
        return daoBuilder.build();
    }

}
