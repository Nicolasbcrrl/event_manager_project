package fi.haagahelia.eventmanager.dto.opinion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OpinionRequest {
    private int rating;
    private String comment;
}
