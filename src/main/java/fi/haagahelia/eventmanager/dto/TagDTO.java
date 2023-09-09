package fi.haagahelia.eventmanager.dto;

import fi.haagahelia.eventmanager.domain.Tag;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.hateoas.RepresentationModel;

@Builder
@Data
@EqualsAndHashCode(callSuper = false)
public class TagDTO extends RepresentationModel<TagDTO>{
    private Long id;
    private String name;

    public static TagDTO convert(Tag tag) {
        return builder()
                .id(tag.getId())
                .name(tag.getName())
                .build();
    }
}
