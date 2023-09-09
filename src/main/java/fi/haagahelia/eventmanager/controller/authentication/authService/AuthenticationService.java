package fi.haagahelia.eventmanager.controller.authentication.authService;

import fi.haagahelia.eventmanager.dto.UserDTO;
import fi.haagahelia.eventmanager.security.ErrorResponse;
import fi.haagahelia.eventmanager.domain.Role;
import fi.haagahelia.eventmanager.domain.User;
import fi.haagahelia.eventmanager.repository.RoleRepository;
import fi.haagahelia.eventmanager.repository.UserRepository;
import fi.haagahelia.eventmanager.security.configuration.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDate;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Log4j2
@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwrtService;
    private final AuthenticationManager authenticationManager;

    /**
     * This method is used to create and add the HATEOAS links to the userDTO
     * @param userDTO - the userDTO to which the links will be added
     * @return - the userDTO with the links added
     */
    private UserDTO createHateoasLinks(UserDTO userDTO){
        Link self = linkTo(AuthenticationService.class).slash(String.valueOf(userDTO.getId())).withSelfRel();
        userDTO.add(self);
        Link collectionLink = linkTo(AuthenticationService.class).slash("").withRel("users");
        userDTO.add(collectionLink);
        return userDTO;
    }

    /**
     * This function is used to register a new user to the database. It is called by the register function in the AuthenticationController.
     * First it checks if the username and email are already in use. If they are, it returns a ResponseEntity with the status BAD_REQUEST.
     * If the username and email are not in use, it creates a new user and saves it to the database. It also adds the role to the user.
     * If the role is not specified, it adds the role ROLE_USER. If the role is admin, it adds the role ROLE_ADMIN.
     * It then save the user in the database. It then creates a new UserDTO from the user and adds the HATEOAS links to it.
     * After the user is converted to a UserDTO, the function will create a new JWT token by using the function generateToken from the jwrtService class.
     * It the return the userDTO in a ResponseEntity with the status OK.
     * @param request - the request containing the information about the user to be registered
     * @return - a ResponseEntity containing the userDTO and the status OK
     */
    public ResponseEntity<?> register(RegisterRequest request) {
        log.info("start registration " + request.getUsername());
        try {
            if (userRepository.existsByUsername(request.getUsername())) {
                log.error("ERROR REGISTERING USER: USERNAME ALREADY EXISTS");
                ErrorResponse response =  new ErrorResponse(HttpStatus.BAD_REQUEST.getReasonPhrase(), "USERNAME ALREADY EXISTS");
                return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
            }
            if (request.getUsername().equals("")) {
                log.error("ERROR REGISTERING USER: USERNAME IS EMPTY");
                ErrorResponse response =  new ErrorResponse(HttpStatus.BAD_REQUEST.getReasonPhrase(), "USERNAME IS EMPTY");
                return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
            }
            if (userRepository.existsByEmail(request.getEmail())) {
                log.error("ERROR REGISTERING USER: EMAIL ALREADY EXISTS");
                ErrorResponse response =  new ErrorResponse(HttpStatus.BAD_REQUEST.getReasonPhrase(), "EMAIL ALREADY EXISTS");
                return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
            }
            User user = new User(request.getFirstName(), request.getLastName(), LocalDate.of(request.getYearOfBirth(), request.getMonthOfBirth(), request.getDayOfBirth()), request.getEmail(), request.getUsername(), passwordEncoder.encode(request.getPassword()));
            if (request.getRole().equals("")) {
                Role role = roleRepository.findByName("ROLE_USER");
                user.addRole(role);
            } else if (request.getRole().equals("admin")) {
                Role role = roleRepository.findByName("ROLE_ADMIN");
                user.addRole(role);
            } else {
                log.error("ERROR REGISTERING USER: ROLE NOT FOUND");
                ErrorResponse response =  new ErrorResponse(HttpStatus.BAD_REQUEST.getReasonPhrase(), "ROLE NOT FOUND");
                return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
            }
            userRepository.save(user);
            UserDTO userDTO = UserDTO.convert(user);
            createHateoasLinks(userDTO);
            var jwtToken = jwrtService.generateToken(user);
            log.info("USER REGISTERED SUCCESSFULLY");
            AuthenticationResponse response = AuthenticationResponse.builder().token(jwtToken).build();
            return new ResponseEntity<>(userDTO , HttpStatus.OK);
        } catch (Exception e) {
            log.error("ERROR REGISTERING USER: " + e.getMessage());
            ErrorResponse response =  new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), "INTERNAL ERROR HAPPENED");
            return new ResponseEntity<>( response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * This function is used to authenticate a user. It is called by the authenticate function in the AuthenticationController.
     * First it checks if the username is in the database. If it is not, it returns a ResponseEntity with the status UNAUTHORIZED.
     * If the username is in the database, it will try to authenticate the user. If the authentication fails, it will return a ResponseEntity with the status UNAUTHORIZED.
     * If the authentication succeeds, it will create a new JWT token by using the function generateToken from the jwrtService class.
     * It then return the userDTO in a ResponseEntity with the status OK.
     * @param request - the request containing the information about the user to be authenticated
     * @return - a ResponseEntity containing :
     *           - the userDTO and the status OK, if the authentication succeeds
     *           - the status UNAUTHORIZED, if the user is not registered in the database
     *           - the status FORBIDDEN, if the user is disabled
     *           - the status INTERNAL_SERVER_ERROR, if an error happens during the authentication
     *
     */

    public ResponseEntity<?> authenticate(AuthenticationRequest request) {
        log.info("USER " + request.getUsername().toUpperCase() + " REQUESTED AUTHENTICATION");
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
        } catch (DisabledException e) {
            // manage disabled exception
            log.info("AUTHENTICATION FAILED FOR USER: " + request.getUsername().toUpperCase() + ". USER DISABLED");
            ErrorResponse response =  new ErrorResponse(HttpStatus.FORBIDDEN.getReasonPhrase(), "USER DISABLED");
            return new ResponseEntity<>(response,HttpStatus.FORBIDDEN);
        } catch (BadCredentialsException e) {
            // manage bad credentials exception (wrong password and username)
            log.info("AUTHENTICATION FAILED FOR USER: " + request.getUsername().toUpperCase() + ". BAD CREDENTIALS");
            ErrorResponse response =  new ErrorResponse(HttpStatus.UNAUTHORIZED.getReasonPhrase(), "BAD CREDENTIALS");
            return new ResponseEntity<>(response,HttpStatus.UNAUTHORIZED);
        } catch (LockedException e) {
            // manage account locked exception
            log.info("AUTHENTICATION FAILED FOR USER: " + request.getUsername().toUpperCase() + ". ACCOUNT LOCKED");
            ErrorResponse response =  new ErrorResponse(HttpStatus.FORBIDDEN.getReasonPhrase(), "ACCOUNT LOCKED");
            return new ResponseEntity<>(response,HttpStatus.FORBIDDEN);
        } catch (AuthenticationException e) {
            // manage generic authentication exception
            log.info("AUTHENTICATION FAILED FOR USER: " + request.getUsername().toUpperCase() + ". " + e.getMessage());
            ErrorResponse response =  new ErrorResponse(HttpStatus.UNAUTHORIZED.getReasonPhrase(), "INTERNAL ERROR HAPPENED DURING AUTHENTICATION");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        catch (Exception e) {
            log.error("ERROR HAPPENED DURING AUTHENTICATION FOR " + request.getUsername().toUpperCase() + ": " + e.getMessage());
            ErrorResponse response =  new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), "INTERNAL ERROR HAPPENED");
            return new ResponseEntity<>( response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        var user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new RuntimeException("USER NOT FOUND"));
        var jwtToken = jwrtService.generateToken(user);
        AuthenticationResponse response = AuthenticationResponse.builder().token(jwtToken).build();
        log.info("USER " + request.getUsername().toUpperCase() +" AUTHENTICATED SUCCESSFULLY. TOKEN: " + response.getToken());
        return new ResponseEntity<>(jwtToken,HttpStatus.OK);
    }

}
