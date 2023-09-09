package fi.haagahelia.eventmanager.controller.opinion;

import fi.haagahelia.eventmanager.domain.Activity;
import fi.haagahelia.eventmanager.domain.Address;
import fi.haagahelia.eventmanager.domain.User;
import fi.haagahelia.eventmanager.domain.opinion.Opinion;
import fi.haagahelia.eventmanager.domain.opinion.OpinionId;
import fi.haagahelia.eventmanager.dto.opinion.OpinionDTO;
import fi.haagahelia.eventmanager.dto.opinion.OpinionRequest;
import fi.haagahelia.eventmanager.repository.*;
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
public class OpinionService {
    private final ActivityRepository activityRepository;
    private final OpinionRepository opinionRepository;
    private final AddressRepository addressRepository;

    /*----------------------------------------------- GLOBAL TOOLS ---------------------------------------------------*/

    /**
     * This methode is used to create and add the HATEOAS links to the opinionDTO.
     * @param opinionDTO - the opinionDTO to which the links will be added
     * @return - the opinionDTO to wich the links will be added
     */
    public OpinionDTO createHateoasLinks(OpinionDTO opinionDTO) {
        Link selfLink = linkTo(OpinionController.class).slash(String.valueOf(opinionDTO.getOpinionID())).withSelfRel();
        Link collectionLink = linkTo(OpinionController.class).slash("/").withRel("opinions");
        opinionDTO.add(selfLink, collectionLink);
        return opinionDTO;
    }

    /**
     * This methode will be used to convert a list of opinion into a list of opinionDTO
     * @param opinions - List that contains opinions to be converted into DTO objects
     * @return -  a list of opinionDTO
     */
    private List<OpinionDTO> listConvertor(List<Opinion> opinions){
        List<OpinionDTO> opinionDTOS = new ArrayList<>();
        for(Opinion opinion : opinions){
            OpinionDTO opinionDTO = OpinionDTO.convert(opinion);
            createHateoasLinks(opinionDTO);
            opinionDTOS.add(opinionDTO);
        }
        return opinionDTOS;
    }

    /*------------------------------------------------ DELETE METHODS ------------------------------------------------*/

    /**
     * This function is used to delete all opinions related to an activity. It is called by the deleteActivity function
     * in the ActivityService class.
     * First, we check if there are opinions related to the activity. If there are no opinions, we return false. If there
     * are opinions, we delete them and return true.
     * @param actId - the id of the activity
     * @return - true if the opinions were deleted, false if there were no opinions to delete
     */
    public  boolean deleteOpinion(Long actId){
        List<Opinion> opinions = opinionRepository.findOpinionsByActivity_Id(actId);
        if(!opinions.isEmpty()){
            return false;
        }
        opinionRepository.deleteAll(opinions);
        return true;
    }

    /**
     * This function is used to delete one opinion related to an activity. It is called by the deleteOpinion function in
     * the OpinionController class.
     * First, we check if there is an opinion related to the activity and the user who requested the deletion. If there is
     * no opinion, we return a NOT_FOUND response. If there is an opinion, we delete it and return an OK response. In case
     * of internal server error, we return an INTERNAL_SERVER_ERROR response.
     * @param actId - the id of the activity
     * @param user  - the user who requested the deletion
     * @return - a response entity with a message and a status code
     *        - NOT_FOUND if there is no opinion related to the activity and the user
     *        - OK if the opinion was deleted
     *        - INTERNAL_SERVER_ERROR if there was an internal server error
     */
    public ResponseEntity<?> deleteOneOpinion(Long actId, User user){
        try{
            log.info("USER " + user.getUsername().toUpperCase() + " REQUESTED TO DELETE OPINION RELATED TO ACTIVITY " + actId);
            Opinion opinion = opinionRepository.findOpinionByActivity_IdAndUser_Id(actId, user.getId());
            if(opinion == null){
                log.info("USER " + user.getUsername().toUpperCase() + " REQUESTED TO DELETE OPINION RELATED TO ACTIVITY " + actId + ". NO OPINION FOUND");
                return new ResponseEntity<>("NO OPINION WAS FOUND",HttpStatus.NOT_FOUND);
            }
            opinionRepository.delete(opinion);
            log.info("USER " + user.getUsername().toUpperCase() + " REQUESTED TO DELETE OPINION RELATED TO ACTIVITY " + actId + ". OPINION DELETED");
            return new ResponseEntity<>("OPINION WAS SUCCESSFULLY DELETED",HttpStatus.OK);
        }catch(Exception e){
            log.error("USER " + user.getUsername().toUpperCase() + " REQUESTED TO DELETE OPINION RELATED TO ACTIVITY " + actId + ". ERROR: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    /*------------------------------------------------- DISPLAY METHODS ----------------------------------------------*/

    /**
     * This function is used to return all opinions of the database. It is called by the getAllOpinions function in the OpinionController class.
     * First, we check if there are opinions in the database. If there are no opinions, we return a NOT_FOUND response. If there
     * are opinions, we convert them into opinionDTO and return an OK response with the list of opinionDTO. In case of internal server
     * error, we return an INTERNAL_SERVER_ERROR response.
     * @param user - the user who requested the opinions
     * @return - a response entity with a message and a status code
     *     - NOT_FOUND if there are no opinions in the database
     *     - OK if the opinions were found and a list of opinionDTO was returned
     *     - INTERNAL_SERVER_ERROR if there was an internal server error
     */
    public ResponseEntity<?> getAllOpinions(User user){
        try{
            log.info("USER " + user.getUsername().toUpperCase() + " REQUESTED TO GET ALL OPINIONS");
            List<Opinion> opinions = opinionRepository.findAll();
            if (opinions.isEmpty()){
                log.info("USER " + user.getUsername().toUpperCase() + " REQUESTED TO GET ALL OPINIONS. NO OPINIONS FOUND");
                return new ResponseEntity<>("NO OPINION WAS FOUND",HttpStatus.NOT_FOUND);
            }
            List<OpinionDTO> opinionDTOS = listConvertor(opinions);
            return new ResponseEntity<>(opinionDTOS, HttpStatus.OK);

        }catch (Exception e){
            log.error("USER " + user.getUsername().toUpperCase() + " REQUESTED TO GET ALL OPINIONS. ERROR: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * This function is used to return all opinions related to an activity. It is called by the function getOpinionById in
     * the OpinionController class. First, we check if the activity exists. If the activity does not exist, we return a
     * NOT_FOUND response. If the activity exists, we check if there are opinions related to the activity. If there are no
     * opinions, we return a NO_CONTENT response. If there are opinions, we convert them into opinionDTO and return an OK
     * response with the list of opinionDTO. In case of internal server error, we return an INTERNAL_SERVER_ERROR response.
     * @param user - the user who requested the opinions
     * @param actId - the id of the activity
     * @return - a response entity with a message and a status code
     *    - NOT_FOUND if the activity doesn't exist
     *    - OK if the opinions were found and a list of opinionDTO was returned
     *    - INTERNAL_SERVER_ERROR if there was an internal server error
     *    - NO_CONTENT if there are no opinions related to the activity
     */
    public ResponseEntity<?> getAllOpinionsByActivity(User user, Long actId){
        try{
            log.info("USER " + user.getUsername().toUpperCase() + " REQUESTED TO GET ALL OPINIONS RELATED TO ACTIVITY " + actId);
            Activity activity = activityRepository.findActivityById(actId);
            if (activity == null){
                log.info("USER " + user.getUsername().toUpperCase() + " REQUESTED TO GET ALL OPINIONS RELATED TO ACTIVITY " + actId + ". NO ACTIVITY FOUND");
                return new ResponseEntity<>("NO ACTIVITY FOUND WITH THIS ID",HttpStatus.NOT_FOUND);
            }
            List<Opinion> opinions = opinionRepository.findOpinionsByActivity_Id(actId);
            if (opinions.isEmpty()){
                log.info("USER " + user.getUsername().toUpperCase() + " REQUESTED TO GET ALL OPINIONS RELATED TO ACTIVITY " + actId + ". NO OPINIONS FOUND");
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            List<OpinionDTO> opinionDTOS = listConvertor(opinions);
            return new ResponseEntity<>(opinionDTOS, HttpStatus.OK);

        }catch (Exception e){
            log.error("USER " + user.getUsername().toUpperCase() + " REQUESTED TO GET ALL OPINIONS RELATED TO ACTIVITY " + actId + ". ERROR: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    /*--------------------------------------------- ADD OPINION TO AN EVENT ------------------------------------------*/

    /**
     * This function is used to create a new opinion in the database using the information in the request body, the id of
     * an activity and the information of the user making the request. It is called by the createOpinion function in the
     * OpinionController class.
     * First, it checks if the activity exists and if the user is a participant of the activity. If the activity doesn't
     * exist we return a ResponseEntity with a NOT_FOUND status and if the user is not a participant we return a ResponseEntity
     * with a CONFLICT status. If the activity exists and the user is a participant, we can start the creation of the opinion.
     * We first check if the rating is between 0 and 10. If it is not, we return a ResponseEntity
     * with a BAD_REQUEST status. If the rating is between 0 and 10, we create the opinion and save it in the database. We then
     * convert the opinion into an opinionDTO and add the HATEOAS links to it. Finally, we return a ResponseEntity with a CREATED
     * status and the opinionDTO in the body.
     * @param user - the user making the request
     * @param actId - the id of the activity to which the opinion will be added
     * @param opinionRequest - the information of the opinion to be created
     * @return - a ResponseEntity with a CREATED status and the opinionDTO in the body
     */
    public ResponseEntity<?> addOpinion(User user, Long actId, OpinionRequest opinionRequest){
        try{
            Activity activity = activityRepository.findActivityById(actId);
            if (activity == null){
                log.error("USER " + user.getUsername().toUpperCase() + " REQUESTED TO ADD OPINION TO ACTIVITY " + actId + ". ERROR: ACTIVITY DOESN'T EXIST");
                return new ResponseEntity<>("ACTIVITY DOESN'T EXIST", HttpStatus.NOT_FOUND);
            }
            if (!activity.getParticipants().contains(user)){
                log.error("USER " + user.getUsername().toUpperCase() + " REQUESTED TO ADD OPINION TO ACTIVITY " + actId + ". ERROR: USER IS NOT A PARTICIPANT");
                return new ResponseEntity<>("USER " + user.getUsername().toUpperCase() + " IS NOT A PARTICIPANT", HttpStatus.CONFLICT);
            }
            log.info("USER " + user.getUsername().toUpperCase() + " REQUESTED TO ADD OPINION TO ACTIVITY " + activity.getName().toUpperCase());
            if(opinionRequest.getRating()<0){
                log.error("USER " + user.getUsername().toUpperCase() + " REQUESTED TO ADD OPINION TO ACTIVITY " + actId + ". ERROR: RATING HAS TO BE BETWEEN 0 AND 10");
                return new ResponseEntity<>("RATING HAS TO BE BETWEEN 0 AND 10",HttpStatus.BAD_REQUEST);
            }
            if (opinionRequest.getRating()>10){
                log.error("USER " + user.getUsername().toUpperCase() + " REQUESTED TO ADD OPINION TO ACTIVITY " + actId + ". ERROR: RATING HAS TO BE BETWEEN 0 AND 10");
                return new ResponseEntity<>("RATING HAS TO BE BETWEEN 0 AND 10",HttpStatus.BAD_REQUEST);
            }
            if (opinionRepository.findOpinionByActivity_IdAndUser_Id(actId,user.getId())!=null){
                log.error("USER " + user.getUsername().toUpperCase() + " REQUESTED TO ADD OPINION TO ACTIVITY " + actId + ". ERROR: USER ALREADY ADDED AN OPINION TO THIS ACTIVITY");
                return new ResponseEntity<>("USER " + user.getUsername().toUpperCase() + " ALREADY ADDED AN OPINION TO THIS ACTIVITY",HttpStatus.CONFLICT);
            }
            OpinionId opinionId = new OpinionId(actId,user.getId());
            Opinion opinion = new Opinion(opinionId,opinionRequest.getComment(),opinionRequest.getRating(),activity,user);
            opinionRepository.save(opinion);
            OpinionDTO opinionDTO = OpinionDTO.convert(opinion);
            createHateoasLinks(opinionDTO);
            log.info("USER " + user.getUsername().toUpperCase() + " ADDED OPINION TO ACTIVITY " + activity.getName().toUpperCase());
            return new ResponseEntity<>(opinionDTO,HttpStatus.CREATED);
        }catch (Exception e){
            log.error("USER " + user.getUsername().toUpperCase() + " REQUESTED TO ADD OPINION TO ACTIVITY " + actId + ". ERROR: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
