package fi.haagahelia.eventmanager.controller.address;

import fi.haagahelia.eventmanager.domain.Address;
import fi.haagahelia.eventmanager.domain.Country;
import fi.haagahelia.eventmanager.dto.address.AddressDTO;
import fi.haagahelia.eventmanager.dto.address.AddressRequest;
import fi.haagahelia.eventmanager.domain.User;
import fi.haagahelia.eventmanager.repository.AddressRepository;
import fi.haagahelia.eventmanager.repository.CountryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.util.Pair;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/addresses")
public class AddressController {

    private final AddressService service;

    /**
     * This function is used to get all addresses from the database by calling
     * the function getAllAddresses of the AddressService class.
     * All details of this function are described in the description of the function in the AddressService class.
     * @param user - the user that is currently logged in
     * @return - a list of all addresses in the database wrapped in a ResponseEntity
     *         .OK - if the request was successful
     *         .NO_CONTENT - if there are no addresses in the database
     *         .INTERNAL_SERVER_ERROR - if there was an error in the server
     */
    @GetMapping(produces = "application/json")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<?> getAllAddresses(@AuthenticationPrincipal User user){
        return ResponseEntity.ok(service.getAllAddresses(user));
    }

    /**
     * This function is used to get an address specified by its id from the database by calling
     * the function getAddressById of the AddressService class.
     * All details of this function are described in the description of the function in the AddressService class.
     * @param user - the user that is currently logged in
     * @param id - the id of the address to be retrieved
     * @return - the address with the specified id wrapped in a ResponseEntity
     *        .OK - if the request was successful
     *        .NOT_FOUND - if there is no address with the specified id in the database
     *        .INTERNAL_SERVER_ERROR - if there was an error in the server     *
     */
    @GetMapping(path = "/{id}", produces = "application/json")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<?> getAddressById(@AuthenticationPrincipal User user, @PathVariable Long id){
        return ResponseEntity.ok(service.getAddressById(user, id));
    }

    /**
     * This function is used to update an address specified by its id in the database using the information in
     * the request body by calling the function updateAddress of the AddressService class.
     * All details of this function are described in the description of the function in the AddressService class.
     * @param user - the user that is currently logged in
     * @param id - the id of the address to be updated
     * @param addressR - the request body containing the information to be updated
     * @return - the updated address wrapped in a ResponseEntity
     *       .OK - if the request was successful
     *       .NOT_FOUND - if there is no address with the specified id in the database
     *       .CONFLICT - if the updated address already exists in the database
     *       .INTERNAL_SERVER_ERROR - if there was an error in the server
     */
    @PutMapping(path = "/{id}/update", produces = "application/json", consumes = "application/json")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<?> updateAddress(@AuthenticationPrincipal User user, @PathVariable Long id, @RequestBody AddressRequest addressR){
        return ResponseEntity.ok(service.updateAddress(user, id, addressR));
    }

    /**
     * This function is used to create a new address in the database using the information in the request body
     * by calling the function create of the AddressService class. This function constitutes the main functionality of
     * the AddressController class.
     * All details of this function are described in the description of the function in the AddressService class.
     * @param user - the user that is currently logged in
     * @param addressR - the request body containing the information to be used to create the new address
     * @return - the newly created address wrapped in a ResponseEntity
     *      .CREATED - if the request was successful
     *      .OK - if the address already exists in the database
     *      .BAD_REQUEST - if the request body is invalid
     *      .INTERNAL_SERVER_ERROR - if there was an error in the server
     */
    @PostMapping(path = "/create", produces = "application/json", consumes = "application/json")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<?> create(@AuthenticationPrincipal User user, @RequestBody AddressRequest addressR){
        return ResponseEntity.ok(service.create(user, addressR));
    }

    /**
     * This function is used to delete an address specified by its id from the database by calling the function
     * deleteAddress of the AddressService class.
     * All details of this function are described in the description of the function in the AddressService class.
     * @param user - the user that is currently logged in
     * @param id - the id of the address to be deleted
     * @return - the id of the deleted address wrapped in a ResponseEntity
     *      .OK - if the request was successful
     *      .NOT_FOUND - if there is no address with the specified id in the database
     *      .INTERNAL_SERVER_ERROR - if there was an error in the server
     */
    @DeleteMapping(path = "/{id}/delete", produces = "application/json")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<?> deleteAddress(@AuthenticationPrincipal User user, @PathVariable Long id){
        return ResponseEntity.ok(service.deleteAddress(user, id));
    }
}
