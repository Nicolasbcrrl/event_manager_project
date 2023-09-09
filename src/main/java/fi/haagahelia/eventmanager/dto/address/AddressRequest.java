package fi.haagahelia.eventmanager.dto.address;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressRequest {
    private String number;
    private String street;
    private String postcode;
    private String city;
    private String countryName;
}
