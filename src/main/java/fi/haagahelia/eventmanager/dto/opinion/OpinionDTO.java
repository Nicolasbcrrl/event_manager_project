package fi.haagahelia.eventmanager.dto.opinion;

import fi.haagahelia.eventmanager.domain.opinion.Opinion;
import fi.haagahelia.eventmanager.domain.opinion.OpinionId;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.hateoas.RepresentationModel;

@Builder
@Data
@EqualsAndHashCode(callSuper = false)
public class OpinionDTO extends RepresentationModel<OpinionDTO> {
    private OpinionId opinionID;
    private String username;
    private String activityName;
    private int rating;
    private String comment;

    public static OpinionDTO convert(Opinion opinion) {
        return builder()
                .opinionID(opinion.getId())
                .username(opinion.getUser().getUsername())
                .activityName(opinion.getActivity().getName())
                .rating(opinion.getRating())
                .comment(opinion.getComment())
                .build();
    }
}
