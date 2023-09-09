package fi.haagahelia.eventmanager.controller.tag;

import fi.haagahelia.eventmanager.controller.activity.ActivityService;
import fi.haagahelia.eventmanager.domain.Activity;
import fi.haagahelia.eventmanager.domain.Tag;
import fi.haagahelia.eventmanager.domain.User;
import fi.haagahelia.eventmanager.dto.TagDTO;
import fi.haagahelia.eventmanager.repository.ActivityRepository;
import fi.haagahelia.eventmanager.repository.TagRepository;
import fi.haagahelia.eventmanager.security.ErrorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Log4j2
@Service
@RequiredArgsConstructor
public class TagService {
    private final TagRepository tagRepository;
    private final ActivityRepository activityRepository;
    private final ActivityService activityService;

    /**
     * This methode is used to create and add the HATEOAS links to the tagDTO
     * @param tagDTO - the tagDTO to which the links will be added
     * @return - the tagDTO with the links added
     */
    private TagDTO createHateoasLinks(TagDTO tagDTO) {
        Link selfLink = linkTo(TagController.class).slash(String.valueOf(tagDTO.getId())).withSelfRel();
        Link collectionLink = linkTo(TagController.class).slash("/").withRel("tags");
        tagDTO.add(selfLink, collectionLink);
        return tagDTO;
    }

    /**
     * This function is use to return all tags in the database. It is called by the getTags function in the TagController class.
     * First it gets all tags from the database. If there are no tags in the database it returns a ResponseEntity with the status NO_CONTENT.
     * If there are tags in the database it converts them to TagDTOs and adds the HATEOAS links to them.
     * It then returns a ResponseEntity with the status OK and the list of TagDTOs.
     * If there is an error it returns a ResponseEntity with the status INTERNAL_SERVER_ERROR.
     * @param user - the user that requested the tags
     * @return - a ResponseEntity with the status OK and the list of TagDTOs or a ResponseEntity with the status NO_CONTENT or INTERNAL_SERVER_ERROR
     */
    public ResponseEntity<?> getTags(User user){
        log.info("USER " + user.getUsername().toUpperCase() + " REQUESTED ALL TAGS");
        try{
            List<Tag> tags = tagRepository.findAll();
            if(tags.isEmpty()){
                log.info("USER " + user.getUsername().toUpperCase() + " REQUESTED ALL TAGS. NO DATA FOUND");
                //ErrorResponse error = new ErrorResponse(HttpStatus.NO_CONTENT.getReasonPhrase(), "No tags found") ;
                return new ResponseEntity<>("NO DATA FOUND",HttpStatus.NO_CONTENT);
            }
            List<TagDTO> tagDTOS = new ArrayList<>();
            for (Tag tag : tags) {
                TagDTO tagDTO = TagDTO.convert(tag);
                tagDTOS.add(createHateoasLinks(tagDTO));
            }
            return new ResponseEntity<>(tagDTOS, HttpStatus.OK);
        }
        catch (Exception e){
            log.error("ERROR GETTING TAGS: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * This function is used to create a Tag in the database. It is called by the createTag function in the TagController class.
     * First it checks if the name of the tag is empty. If it is it returns a ResponseEntity with the status BAD_REQUEST.
     * Then it checks if the tag already exists in the database. If it does it returns a ResponseEntity with the status BAD_REQUEST.
     * If the tag does not exist in the database it creates a new tag and saves it to the database.
     * It then converts the tag to a TagDTO and adds the HATEOAS links to it.
     * It then returns a ResponseEntity with the status CREATED and the TagDTO of the created tag.
     * If there is an error it returns a ResponseEntity with the status INTERNAL_SERVER_ERROR.
     * @param user - the user that requested the creation of the tag
     * @param tagDTO -  the tagDTO of the tag to be created
     * @return - a ResponseEntity with the status CREATED and the TagDTO of the created tag or
     *           a ResponseEntity with the status BAD_REQUEST or INTERNAL_SERVER_ERROR
     */
    public ResponseEntity<?> createTag(User user, TagDTO tagDTO){
        log.info("USER " + user.getUsername().toUpperCase() + " REQUESTED TO CREATE A TAG");
        try{
            if (tagDTO.getName().isEmpty()){
                log.info("USER " + user.getUsername().toUpperCase() + " REQUESTED TO CREATE A TAG BAD REQUEST");
                ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.getReasonPhrase(), "TAG NAME CAN NOT BE EMPTY");
                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
            }
            if (tagRepository.existsByName(tagDTO.getName())){
                log.info("USER " + user.getUsername().toUpperCase() + " REQUESTED TO CREATE A TAG. TAG ALREADY EXISTS");
                ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.getReasonPhrase(), "TAG ALREADY EXISTS");
                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
            }
            Tag tag = new Tag(tagDTO.getName());
            tagRepository.save(tag);
            log.info("USER " + user.getUsername().toUpperCase() + " REQUESTED TO CREATE A TAG. CREATED");
            TagDTO confirmedTag = TagDTO.convert(tag);
            createHateoasLinks(confirmedTag);
            return new ResponseEntity<>(confirmedTag, HttpStatus.CREATED);
        }
        catch (Exception e){
            log.error("ERROR DURING CREATION OF THE TAG: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * This function is used to delete a tag from the database. It is called by the deleteTag function in the TagController class.
     * First it checks if the tag exists in the database. If it does not it returns a ResponseEntity with the status NOT_FOUND.
     * Then it will remove the tag from all activities that have it.
     * If the tag is not used by any activity it will be deleted from the database. If it is used by an activity it will
     * be deleted from the database after it has been removed from all activities.
     * @param user - the user that requested the deletion of the tag
     * @param tagId - the id of the tag to be deleted
     * @return - a ResponseEntity with the status OK or NOT_FOUND or INTERNAL_SERVER_ERROR
     *
     */
    public ResponseEntity<?> deleteTag(User user, Long tagId){
        try {
            Tag tag = tagRepository.findTagById(tagId);
            if (tag == null) {
                log.info("USER " + user.getUsername().toUpperCase() + " REQUESTED TO DELETE A TAG. TAG NOT FOUND");
                return new ResponseEntity<>("TAG NOT FOUND", HttpStatus.NOT_FOUND);
            }
            log.info("USER " + user.getUsername().toUpperCase() + " REQUESTED TO DELETE THE TAG " + tag.getName().toUpperCase());
            if (activityService.removeTagFromActivity(tagId).getFirst().equals(HttpStatus.OK)) {
                tagRepository.delete(tag);
                return new ResponseEntity<>("TAG " + tag.getName().toUpperCase() + " WAS SUCCESSFULLY DELETED",HttpStatus.OK);
            }
            tagRepository.delete(tag);
            return new ResponseEntity<>("TAG " + tag.getName().toUpperCase() + " WAS SUCCESSFULLY DELETED",HttpStatus.OK);

        }catch (Exception e){
            log.error("ERROR DURING DELETION OF THE TAG: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
