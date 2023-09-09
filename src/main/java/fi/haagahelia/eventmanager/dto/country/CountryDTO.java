package fi.haagahelia.eventmanager.dto.country;

import fi.haagahelia.eventmanager.domain.Country;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.hateoas.RepresentationModel;

@Builder
@Data
@EqualsAndHashCode(callSuper = false)
public class CountryDTO extends RepresentationModel<CountryDTO> {
    private Long id;
    private String name;
    //@JsonIgnore
   //private List<AddressDTO> adressDTOList;

    public static CountryDTO convert(Country country) {
        /**
        List<AddressDTO> addressDTOList = new ArrayList<>();
        for (Address address : country.getAddresses()) {
            AddressDTO addressDTO = AddressDTO.convert(address);
            addressDTOList.add(addressDTO);
        }
         */

        CountryDTOBuilder countryDAOBuilder = builder()
                .id(country.getId())
                .name(country.getName());
        /**
        if (!addressDTOList.isEmpty()) {
            countryDAOBuilder.adressDTOList(addressDTOList);
        }
        */
        return countryDAOBuilder.build();
    }
}
