package fi.haagahelia.eventmanager.security;

import lombok.Data;
import lombok.NonNull;

@Data
public class ErrorResponse {
    @NonNull
    private String status;
    @NonNull
    private String message;

}
