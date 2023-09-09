package fi.haagahelia.eventmanager.controller.authentication;

import fi.haagahelia.eventmanager.controller.authentication.authService.AuthenticationRequest;
import fi.haagahelia.eventmanager.controller.authentication.authService.AuthenticationService;
import fi.haagahelia.eventmanager.controller.authentication.authService.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping ("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService service;

    /**
     * This function is used to register a new user to the database, by calling the function register of the
     * AuthenticationService class. All details of this function are described in the description of the function
     * in the AuthenticationService class.
     * @param request - the request containing the details of the user to be registered
     * @return - the details of the registered user wrapped in a ResponseEntity
     */
    @PostMapping(path = "/register", consumes = "application/json")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request){
        return ResponseEntity.ok(service.register(request));
    }

    /**
     * This function is used to authenticate a user, by calling the function authenticate of the
     * AuthenticationService class. All details of this function are described in the description of the function
     * in the AuthenticationService class.
     * @param request - the request containing the details of the user to be authenticated
     * @return - the details of the authenticated user wrapped in a ResponseEntity
     */
    @PostMapping(path = "/login", consumes = "application/json")
    public ResponseEntity<?> register(@RequestBody AuthenticationRequest request){
        return ResponseEntity.ok(service.authenticate(request));
    }
}
