package fi.haagahelia.eventmanager.dto.activity;

import fi.haagahelia.eventmanager.domain.Activity;
import fi.haagahelia.eventmanager.dto.address.AddressDTO;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDate;
import java.time.LocalTime;

@Builder
@Data
@EqualsAndHashCode(callSuper = false)
public class ActivityDTO extends RepresentationModel<ActivityDTO> {
    private Long id;
    private String name;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String description;
    private int numPlaces;
    private int ageLimit;
    private AddressDTO address;
    private String creator;

    public static ActivityDTO convert(Activity activity){
        ActivityDTOBuilder builder = builder()
                .id(activity.getId())
                .name(activity.getName())
                .date(activity.getDate())
                .startTime(activity.getStartTime())
                .endTime(activity.getEndTime())
                .description(activity.getDescription())
                .numPlaces(activity.getNumPlaces())
                .ageLimit(activity.getAgeLimit())
                .creator(activity.getCreator().getUsername());
        if (activity.getAddress() != null) {
            builder.address(AddressDTO.convert(activity.getAddress()));
        }else {
            builder.address(null);
        }
        return builder.build();
    }
}
