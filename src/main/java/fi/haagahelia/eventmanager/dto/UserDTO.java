package fi.haagahelia.eventmanager.dto;

import fi.haagahelia.eventmanager.domain.User;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDate;

@Builder
@Data
@EqualsAndHashCode(callSuper = false)
public class UserDTO extends RepresentationModel<TagDTO> {

    private Long id;
    private String username;
    private String email;
    private LocalDate birthDate;

    public static UserDTO convert(User user) {
        return builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .birthDate(user.getBirthDate())
                .build();
    }

}
