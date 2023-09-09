package fi.haagahelia.eventmanager.controller.address;

import fi.haagahelia.eventmanager.controller.activity.ActivityService;
import fi.haagahelia.eventmanager.domain.Address;
import fi.haagahelia.eventmanager.domain.Country;
import fi.haagahelia.eventmanager.domain.User;
import fi.haagahelia.eventmanager.dto.address.AddressDTO;
import fi.haagahelia.eventmanager.dto.address.AddressRequest;
import fi.haagahelia.eventmanager.repository.AddressRepository;
import fi.haagahelia.eventmanager.repository.CountryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.util.Pair;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Log4j2
@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final CountryRepository countryRepository;
    private final ActivityService activityService;

    /*----------------------------------------------- GLOBAL TOOLS ---------------------------------------------------*/

    /**
     * This methode is used to create and add the HATEOAS links to the addressDTO.
     * @param addressDTO - the addressDTO to which the links will be added
     * @return - the addressDTO with the added links
     */
    private AddressDTO createHateoasAddressLinks(AddressDTO addressDTO){
        Link selfLink = linkTo(AddressController.class).slash(String.valueOf(addressDTO.getId())).withSelfRel();
        Link collectionLink = linkTo(AddressController.class).slash("/").withRel("addresses");
        addressDTO.add(selfLink, collectionLink);
        return addressDTO;
    }

    /**
     * This methode is used to control all the information the request made by the user.
     * In this méthode we check the following constraints:
     *          - number : not null, not empty
     *          - street : not null, not empty, at least 2 characters long
     *          - city : not null, not empty, at least 2 characters long
     *          - country : not null, not empty, at least 2 characters long
     *          - postalCode : not null, not empty, at least 2 characters long
     * @param addressRequest - the request that contains all the information made by the user
     * @return - a pair that contains the HttpStatus and a String that contains the error message
     */
    private Pair<HttpStatus, String> rules(AddressRequest addressRequest){
        if(addressRequest == null){
            return Pair.of(HttpStatus.BAD_REQUEST, "ADDRESS REQUEST IS NULL");
        }
        if (addressRequest.getNumber().isEmpty()){
            return Pair.of(HttpStatus.BAD_REQUEST, "NUMBER IS EMPTY");
        }
        if (addressRequest.getNumber() == null){
            return Pair.of(HttpStatus.BAD_REQUEST, "NUMBER IS NULL");
        }
        if (addressRequest.getStreet().isEmpty()){
            return Pair.of(HttpStatus.BAD_REQUEST, "STREET IS EMPTY");
        }
        if (addressRequest.getStreet() == null){
            return Pair.of(HttpStatus.BAD_REQUEST, "STREET IS NULL");
        }
        if (addressRequest.getStreet().length() < 2){
            return Pair.of(HttpStatus.BAD_REQUEST, "STREET NAME MUST BE AT LEAST 2 CHARACTERS LONG");
        }
        if (addressRequest.getCity().isEmpty()){
            return Pair.of(HttpStatus.BAD_REQUEST, "CITY IS EMPTY");
        }
        if (addressRequest.getCity() == null){
            return Pair.of(HttpStatus.BAD_REQUEST, "CITY IS NULL");
        }
        if(addressRequest.getCity().length() < 2){
            return Pair.of(HttpStatus.BAD_REQUEST, "CITY NAME MUST BE AT LEAST 2 CHARACTERS LONG");
        }
        if (addressRequest.getPostcode().isEmpty()){
            return Pair.of(HttpStatus.BAD_REQUEST, "POSTCODE IS EMPTY");
        }
        if (addressRequest.getPostcode() == null){
            return Pair.of(HttpStatus.BAD_REQUEST, "POSTCODE IS NULL");
        }
        if (addressRequest.getCountryName().isEmpty()){
            return Pair.of(HttpStatus.BAD_REQUEST, "COUNTRY IS EMPTY");
        }
        if (addressRequest.getCountryName() == null){
            return Pair.of(HttpStatus.BAD_REQUEST, "COUNTRY IS NULL");
        }
        if (addressRequest.getCountryName().length() < 2){
            return Pair.of(HttpStatus.BAD_REQUEST, "COUNTRY NAME MUST BE AT LEAST 2 CHARACTERS LONG");
        }
        return Pair.of(HttpStatus.OK, "REQUEST IS VALID");
    }

    /**
     * This methode is used to check if the address already exists in the database.
     * @param addressRequest - the request that contains all the information made by the user
     * @return - true if the address already exists, false if it doesn't
     */
    private Boolean checkIfAddressExists(AddressRequest addressRequest){
        Country country = countryRepository.findCountryByName(addressRequest.getCountryName());
        if(addressRepository.existsAddressByNumberAndStreetAndPostcodeAndCityAndCountry(addressRequest.getNumber(),
                addressRequest.getStreet(), addressRequest.getPostcode(), addressRequest.getCity(), country)){
            return true;
        }
        return false;
    }

    /*--------------------------------------------- DISPLAY METHODS --------------------------------------------------*/

    /**
     * This function is used to return all addresses of the database. It is called by the getAllAddresses function in the AddressController class.
     * First we collect all addresses from the database in a list. After that we check if the list is empty.
     * If it is empty the function return a ResponseEntity with the HttpStatus.NO_CONTENT.
     * If the list is not empty we go through the list and create a new AddressDTO for each address, adding the HATEOAS links.
     * Finally, we return a ResponseEntity with the HttpStatus.OK and the list of AddressDTOs.
     * @param user - the user that made the request
     * @return - a ResponseEntity with the HttpStatus and the list of AddressDTOs
     */
    public ResponseEntity<?> getAllAddresses(User user){
        log.info("USER " + user.getUsername().toUpperCase() + " REQUESTED ALL ADDRESSES.");
        try {
            List<Address> addresses = addressRepository.findAll();
            if(addresses.isEmpty()){
                log.info("USER " + user.getUsername().toUpperCase() + " REQUESTED ALL ADDRESSES BUT THERE ARE NONE.");
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            List<AddressDTO> addressDTOS = new ArrayList<>();
            for(Address address : addresses){
                AddressDTO addressDTO = AddressDTO.convert(address);
                createHateoasAddressLinks(addressDTO);
                addressDTOS.add(addressDTO);
            }
            return new ResponseEntity<>(addressDTOS, HttpStatus.OK);
        }catch (Exception e){
            log.error("Error getting all addresses: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * This function is used to get an address by its id. It is called by the AddressController class.
     * First we check if the address exists in the database. If it doesn't we return a ResponseEntity with the HttpStatus.NOT_FOUND.
     * If the address exists we create an AddressDTO for it and add the HATEOAS links.
     * Finally, we return a ResponseEntity with the HttpStatus.OK and the AddressDTO.
     * @param user - the user that made the request
     * @param id - the id of the address
     * @return  - a ResponseEntity with the HttpStatus and the AddressDTO
     */
    public ResponseEntity<?> getAddressById(User user, Long id){
        log.info("USER " + user.getUsername().toUpperCase() + " REQUESTED ADDRESS WITH ID " + id);
        try {
            if(!addressRepository.existsById(id)){
                log.info("USER " + user.getUsername().toUpperCase() + " REQUESTED ADDRESS WITH ID " + id + " BUT IT DOES NOT EXIST.");
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            Address address = addressRepository.findAddressById(id);
            AddressDTO addressDTO = AddressDTO.convert(address);
            createHateoasAddressLinks(addressDTO);
            return new ResponseEntity<>(addressDTO, HttpStatus.OK);
        }catch (Exception e){
            log.error("Error getting address with id " + id + ": " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /*--------------------------------------------- UPDATE METHODS --------------------------------------------------*/


    /**
     * This function is used to update an address. It is called by the AddressController class.
     * First we check if the all the fields in the request are valid. If they are not we return a ResponseEntity with
     * the HttpStatus.BAD_REQUEST and a message with more information on the error.
     * After that we check the request, we check if the address exists in the database. If it doesn't we return a ResponseEntity
     * with the HttpStatus.NOT_FOUND. If it exists we check if the address updated by the user already exists in the database.
     * If it does we return a ResponseEntity with the HttpStatus.CONFLICT. If it doesn't we update the address and return a
     * ResponseEntity with the HttpStatus.OK.
     * @param user - the user that made the request
     * @param id - the id of the address
     * @param addressR - the request that contains all the information made by the user
     * @return - a ResponseEntity with the HttpStatus
     */
    public ResponseEntity<?> updateAddress(User user, Long id, AddressRequest addressR){
        try {
            log.info("USER " + user.getUsername().toUpperCase() + " REQUESTED TO UPDATE ADDRESS WITH ID " + id + ". " + addressR.toString());
            Pair<HttpStatus, String> rules = rules(addressR);
            if (rules.getFirst().equals(HttpStatus.OK)){
                if(!addressRepository.existsById(id)){
                    log.info("USER " + user.getUsername().toUpperCase() + " REQUESTED TO UPDATE ADDRESS WITH ID " + id + " BUT IT DOES NOT EXIST.");
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                }
                if(checkIfAddressExists(addressR)){
                    log.info("USER " + user.getUsername().toUpperCase() + " REQUESTED TO UPDATE ADDRESS WITH ID " + id + " BUT IT ALREADY EXISTS.");
                    return new ResponseEntity<>(HttpStatus.CONFLICT);
                }
                Address address = addressRepository.findAddressById(id);
                address.setNumber(addressR.getNumber());
                address.setStreet(addressR.getStreet());
                address.setPostcode(addressR.getPostcode());
                address.setCity(addressR.getCity());
                if(!countryRepository.existsByName(addressR.getCountryName())){
                    Country country = new Country();
                    country.setName(addressR.getCountryName());
                    countryRepository.save(country);
                }
                Country country = countryRepository.findCountryByName(addressR.getCountryName());
                address.setCountry(country);
                addressRepository.save(address);
                AddressDTO addressDTO = AddressDTO.convert(address);
                createHateoasAddressLinks(addressDTO);
                log.info("USER " + user.getUsername().toUpperCase() + " UPDATED ADDRESS WITH ID " + id + ". " + addressDTO.toString());
                return new ResponseEntity<>(addressDTO, HttpStatus.OK);
            }
            log.info("USER " + user.getUsername().toUpperCase() + " REQUESTED TO UPDATE ADDRESS WITH ID " + id + " BUT THE REQUEST IS INVALID.");
            return new ResponseEntity<>(rules.getSecond(), rules.getFirst());
        }catch (Exception e){
            log.error("Error updating address with id " + id + ": " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /*--------------------------------------------- CREATE METHODS --------------------------------------------------*/
    /**
     * This function is used to create an address. It is called by the AddressController class.
     * First we check if the all the fields in the request are valid. If they are not we return a ResponseEntity with
     * the HttpStatus.BAD_REQUEST and a message with more information on the error. After that we check if the address
     * already exists in the database. If it does we return a ResponseEntity with the HttpStatus.CONFLICT. If it doesn't
     * we create the address and return a ResponseEntity with the HttpStatus.OK and the new address.
     * @param user - the user that made the request
     * @param addressR - the request that contains all the information made by the user
     * @return - a ResponseEntity with the HttpStatus
     */
    public ResponseEntity<?> create(User user, AddressRequest addressR){
        try {
            log.info("USER " + user.getUsername().toUpperCase() + " REQUESTED TO CREATE AN ADDRESS. ");
            Pair<HttpStatus, String> rules = rules(addressR);
            if (rules.getFirst().equals(HttpStatus.OK)){
                if (!countryRepository.existsByName(addressR.getCountryName())){
                    log.info("USER " + user.getUsername().toUpperCase() + " CREATED A COUNTRY.");
                    Country country = new Country(addressR.getCountryName());
                    countryRepository.save(country);
                }
                if(checkIfAddressExists(addressR)) {
                    log.info("USER " + user.getUsername().toUpperCase() + " REQUESTED TO CREATE AN ADDRESS PRESENT IN DATA BASE.");
                    Address address = new Address(addressR.getNumber(), addressR.getStreet(), addressR.getPostcode(), addressR.getCity(),
                            countryRepository.findCountryByName(addressR.getCountryName()));
                    AddressDTO addressDTO = AddressDTO.convert(address);
                    createHateoasAddressLinks(addressDTO);
                    return new ResponseEntity<>(addressDTO, HttpStatus.CONFLICT);
                }
                Country country = countryRepository.findCountryByName(addressR.getCountryName());
                Address address = new Address(addressR.getNumber(), addressR.getStreet(), addressR.getPostcode(), addressR.getCity(), country);
                addressRepository.save(address);
                AddressDTO addressDTO = AddressDTO.convert(address);
                createHateoasAddressLinks(addressDTO);
                return new ResponseEntity<>(addressDTO, HttpStatus.CREATED);
            }
            else {
                log.info("USER " + user.getUsername().toUpperCase() + " REQUESTED TO CREATE AN ADDRESS WITH THE FOLLOWING ERROR : " + rules.getSecond() + ".");

                return new ResponseEntity<>(rules.getSecond(), rules.getFirst());
            }

        }catch (Exception e){
            log.info("User " + user.getUsername() + " REQUESTED TO CREATE AN ACTIVITY. ERROR: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    /*--------------------------------------------- DELETE METHODS --------------------------------------------------*/
    /**
     * This function is used to delete an address. It is called by the AddressController class.
     * First we will update all the activities that have the address that we want to delete. We will set the address to null.
     * After that we will delete the address and return a ResponseEntity with the HttpStatus.OK.
     * @param user - the user that made the request
     * @param id - the id of the address
     * @return - a ResponseEntity with the HttpStatus
     */
    public ResponseEntity<?> deleteAddress(User user, Long id){
        try {
            Address address = addressRepository.findAddressById(id);
            log.info("USER " + user.getUsername().toUpperCase() + " REQUESTED TO DELETE ADDRESS WITH ID " + id + ". " + address.toString());
            if(!addressRepository.existsById(id)){
                log.info("USER " + user.getUsername().toUpperCase() + " REQUESTED TO DELETE ADDRESS WITH ID " + id + " BUT IT DOES NOT EXIST.");
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            if (activityService.removeAddressFromActivity(id).getFirst().equals(HttpStatus.OK)){
                addressRepository.deleteById(id);
                log.info("USER " + user.getUsername().toUpperCase() + " DELETED ADDRESS WITH ID " + id + ".");
                return new ResponseEntity<>("ADDRESS WAS SUCCESSFULLY DELETED",HttpStatus.OK);
            }
            return new ResponseEntity<>(activityService.removeAddressFromActivity(id).getSecond().toUpperCase(), activityService.removeAddressFromActivity(id).getFirst());
        }catch (Exception e){
            log.error("ERROR DELETING ADDRESS WITH ID " + id + ": " + e.getMessage().toUpperCase());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
