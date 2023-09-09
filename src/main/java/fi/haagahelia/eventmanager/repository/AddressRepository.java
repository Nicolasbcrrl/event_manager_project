package fi.haagahelia.eventmanager.repository;

import fi.haagahelia.eventmanager.domain.Activity;
import fi.haagahelia.eventmanager.domain.Address;
import fi.haagahelia.eventmanager.domain.Country;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {
    Address findAddressById(Long id);
    Address findAddressByStreet(String street);
    Address findAddressByCountry_Name(String countryName);
    Address findAddressByCountry(Country country);
    Boolean existsAddressByNumberAndStreetAndPostcodeAndCityAndCountry(String number, String street, String postcode, String city, Country country);
    List<Address> findAll();


}
