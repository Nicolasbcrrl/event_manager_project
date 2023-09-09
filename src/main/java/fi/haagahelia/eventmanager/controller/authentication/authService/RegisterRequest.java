package fi.haagahelia.eventmanager.controller.authentication.authService;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    private String firstName;
    private String lastName;
    private int yearOfBirth;
    private int monthOfBirth;
    private int dayOfBirth;
    private String email;
    private String role;
    private String username;
    private String password;
}
