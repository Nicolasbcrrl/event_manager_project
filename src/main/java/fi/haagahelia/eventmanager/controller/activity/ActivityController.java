package fi.haagahelia.eventmanager.controller.activity;


import fi.haagahelia.eventmanager.domain.User;

import fi.haagahelia.eventmanager.dto.activity.ActivityRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;


@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/events")
public class ActivityController {

    private final ActivityService service;

    /**
     * This function is used to get all activities in the database. It will call the allActivities function in the ActivityService class.
     * All details of this function are described in the description of the function in the ActivityService class.
     * @param user - the user that is currently logged in
     * @return  - a list of all activities in the database wrapped in a ResponseEntity
     *         .OK - if the request was successful
     *         .NO_CONTENT - if there are no activities in the database
     *         .INTERNAL_SERVER_ERROR - if there was an error in the server
     */
    @GetMapping(produces = "application/json")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<?> getAllActivities(@AuthenticationPrincipal User user){
        return ResponseEntity.ok(service.allActivities(user));
    }

    /**
     * This function is used to get all activities in the database that are available regarding the system date.
     * It will call the allAvailableActivities function in the ActivityService class.
     * All details of this function are described in the description of the function in the ActivityService class.
     * @param user - the user that is currently logged in
     * @return - a list of all activities in the database that are available regarding the system date wrapped in a ResponseEntity
     *        .OK - if the request was successful
     *        .NO_CONTENT - if there are no activities in the database
     *        .INTERNAL_SERVER_ERROR - if there was an error in the server
     */
    @GetMapping(path = "/availableEvents", produces = "application/json")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<?> getAllAvailableActivities(@AuthenticationPrincipal User user){
        return ResponseEntity.ok(service.allAvailableActivities(user));
    }

    /**
     * This function is used to get an activity in the database using the id in the path.
     * It will call the activityById function in the ActivityService class.
     * All details of this function are described in the description of the function in the ActivityService class.
     * @param user - the user that is currently logged in
     * @param id - the id of the activity to get
     * @return - the activity with the specified id wrapped in a ResponseEntity
     *       .OK - if the request was successful
     *       .NOT_FOUND - if there is no activity with the specified id
     *       .INTERNAL_SERVER_ERROR - if there was an error in the server
     */
    @GetMapping(path = "/{id}", produces = "application/json")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<?> getActivityById(@AuthenticationPrincipal User user, @PathVariable("id") Long id){
        return ResponseEntity.ok(service.activityById(user, id));
    }

    /**
     * This function is used to search for activities in the database depending on the parameters given in the request.
     * It will call the search function in the ActivityService class.
     * All details of this function are described in the description of the function in the ActivityService class.
     * @param user - the user that is currently logged in
     * @param name - the name of the activity to search for
     * @param date - the date of the activity to search for
     * @param city - the city of the activity to search for
     * @param country - the country of the activity to search for
     * @param tag - the tag of the activity to search for
     * @return - a list of all activities in the database that match the search parameters wrapped in a ResponseEntity
     *      .OK - if the request was successful
     *      .NOT_FOUND - if there are no activities that match the search parameters
     *      .BAD_REQUEST - if the request parameters are invalid or no proper parameters are given
     *      .INTERNAL_SERVER_ERROR - if there was an error in the server
     */
    @GetMapping(path = "/search", produces = "application/json")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<?> getAllActivityByName(@AuthenticationPrincipal User user,
                                                  @RequestParam(required = false) String name,
                                                  @RequestParam(required = false) LocalDate date,
                                                  @RequestParam(required = false) String city,
                                                  @RequestParam(required = false) String country,
                                                  @RequestParam(required = false) String tag
                                                  )
    {
        return ResponseEntity.ok(service.search(tag,city,country,name,date,user));
    }

    /**
     * This function is used to create a new activity in the database using the information giving in the request body
     * and the user making the request, as the creator of the activity.
     * It will call the createActivity function in the ActivityService class.
     * All details of this function are described in the description of the function in the ActivityService class.
     * @param user - the user that is currently logged in
     * @param request - the request body containing the information of the activity to create
     * @return - the newly created activity wrapped in a ResponseEntity
     *      .CREATED - if the request was successful
     *      .BAD_REQUEST - if the request body is invalid
     *      .INTERNAL_SERVER_ERROR - if there was an error in the server
     */
    @PostMapping(path = "/create", consumes = "application/json", produces = "application/json")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<?> createActivity(@AuthenticationPrincipal User user, @RequestBody ActivityRequest request){
        return ResponseEntity.ok(service.createActivity(user, request));
    }

    /**
     * This function is used to add an address to an activity in the database. The address is added using his id to the
     * activity spécified by the id in the parameter.
     * It will call the addAddress function in the ActivityService class.
     * All details of this function are described in the description of the function in the ActivityService class.
     * @param user - the user that is currently logged in and the creator of the event
     * @param activityId - the id of the activity to add the address to
     * @param addressId - the id of the address to add to the activity
     * @return - the activity with the added address wrapped in a ResponseEntity
     *     .OK - if the request was successful
     *     .NOT_FOUND - if there is no activity with the specified id or no address with the specified id
     *     .NOT_ACCEPTABLE - if the address does not exist in the database
     *     .INTERNAL_SERVER_ERROR - if there was an error in the server
     */
    @PutMapping(path = "/{eventId}/addAddress/{addressId}", produces = "application/json")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<?> addAdressToActivity(@AuthenticationPrincipal User user, @PathVariable("eventId") Long activityId,  @PathVariable("addressId") Long addressId){
        return ResponseEntity.ok(service.addAdress(user, activityId, addressId));
    }

    /**
     * This function is used to add a tag to an activity in the database. The tag is added using his id of the activity.
     * It will call the addTag function in the ActivityService class.
     * All details of this function are described in the description of the function in the ActivityService class.
     * @param user - the user that is currently logged in and the creator of the event
     * @param activityId - the id of the activity to add the tag to
     * @param tagNames - the name of the tag to add to the activity
     * @return - the activity with the added tag wrapped in a ResponseEntity
     *    .OK - if the request was successful
     *    .UNAUTHORIZED - if the user is not the creator of the activity
     *    .BAD_REQUEST - if the tag does not exist in the database
     *    .INTERNAL_SERVER_ERROR - if there was an error in the server
     */
    @PutMapping(path = "/{eventId}/addTags", produces = "application/json")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<?> addTagToActivity(@AuthenticationPrincipal User user, @PathVariable("eventId") Long activityId,  @RequestBody List<String> tagNames){
        return ResponseEntity.ok(service.addTags(user, activityId, tagNames));
    }

    /**
     * This function is used to add a participant to the list of participants or in the waiting list of the activity
     * specified by the id in the parameters.
     * It will call the addParticipant function in the ActivityService class.
     * All details of this function are described in the description of the function in the ActivityService class.
     * @param user - the user that is currently logged in and the creator of the event
     * @param activityId - the id of the activity to add the participant to
     * @return - the activity with the added participant wrapped in a ResponseEntity
     *  .OK - if the request was successful
     *  .NOT_FOUND - if there is no activity with the specified id
     *  .UNAUTHORIZED - if the user is to young to participate in the activity
     *  .INTERNAL_SERVER_ERROR - if there was an error in the server
     */
    @PutMapping(path = "/{eventId}/addParticipant", produces = "application/json")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<?> addUserToActivity(@AuthenticationPrincipal User user, @PathVariable("eventId") Long activityId){
        return ResponseEntity.ok(service.addParticipant(user, activityId));
    }

    /**
     * This function is used to take randomly in the waiting list a participant for the event specified by the id in the parameters.
     * It will call the addWaitingParticipant function in the ActivityService class.
     * All details of this function are described in the description of the function in the ActivityService class.
     * @param user - the user that is currently logged in and the creator of the event
     * @param activityId - the id of the activity to add the participant to
     * @return - the activity with the added participant wrapped in a ResponseEntity
     *    .OK - if the request was successful
     *    .CONFLICT - if there is no participant in the waiting list
     *    .INTERNAL_SERVER_ERROR - if there was an error in the server
     *    .UNAUTHORIZED - if the user is not the creator of the event
     */
    @PutMapping(path = "/{eventId}/addParticipant/waitingParticipant", produces = "application/json")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<?> addWaitingParticipantToActivity(@AuthenticationPrincipal User user, @PathVariable("eventId") Long activityId){
        return ResponseEntity.ok(service.addWaitingParticipant(user, activityId));
    }

    /**
     * This function is used to update an activity in the database using the information giving in the request body
     * and the user making the request, as the creator of the activity.
     * It will call the modifyActivity function in the ActivityService class.
     * All details of this function are described in the description of the function in the ActivityService class.
     * @param user - the user that is currently logged in and the creator of the event
     * @param activityId - the id of the activity to update
     * @param request - the request body containing the information of the activity to update
     * @return - the updated activity wrapped in a ResponseEntity
     *   .OK - if the request was successful
     *   .NOT_FOUND - if there is no activity with the specified id
     *   .UNAUTHORIZED - if the user is not the creator of the activity
     *   .INTERNAL_SERVER_ERROR - if there was an error in the server
     */
    @PutMapping(path = "/{id}/updateEvent", produces = "application/json")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<?> updateActivity(@AuthenticationPrincipal User user, @PathVariable("id") Long activityId, @RequestBody ActivityRequest request){
        return ResponseEntity.ok(service.modifyActivity(user,activityId,request));
    }

    /**
     * This function is used to remove a participant from the list of participants of the activity specified by the id in the parameters.
     * It will call the deleteParticipant function in the ActivityService class.
     * All details of this function are described in the description of the function in the ActivityService class.
     * @param user - the user that is currently logged in and the creator of the event
     * @param activityId - the id of the activity to remove the participant from
     * @param userId - the id of the participant to remove from the activity
     * @return - the activity with the removed participant wrapped in a ResponseEntity
     *  .OK - if the request was successful
     *  .NOT_FOUND - if there is no activity with the specified id or no participant with the specified id
     *  .UNAUTHORIZED - if the user is not the creator of the activity
     *  .INTERNAL_SERVER_ERROR - if there was an error in the server
     */
    @DeleteMapping(path = "/{eventId}/removeParticipant/{userId}", produces = "application/json")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<?> removeUserFromActivity(@AuthenticationPrincipal User user, @PathVariable("eventId") Long activityId, @PathVariable("userId") Long userId){
        return ResponseEntity.ok(service.deleteParticipant(user,userId,activityId));
    }

    /**
     * This function is used to remove an activity from the database. The activity to delete is specified by the id in the path.
     * It will call the deleteActivity function in the ActivityService class.
     * All details of this function are described in the description of the function in the ActivityService class.
     * @param user - the user that is currently logged in and the creator of the event
     * @param activityId - the id of the activity to delete
     * @return - a ResponseEntity with the status OK or NOT_FOUND or INTERNAL_SERVER_ERROR
     *   .OK - if the request was successful
     *   .NOT_FOUND - if there is no activity with the specified id
     *   .UNAUTHORIZED - if the user is not the creator of the activity
     *   .INTERNAL_SERVER_ERROR - if there was an error in the server
     */
    @DeleteMapping(path = "/{id}/removeEvent", produces = "application/json")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<?> removeActivity(@AuthenticationPrincipal User user, @PathVariable("id") Long activityId){
        return ResponseEntity.ok(service.deleteActivity(user,activityId));
    }
}