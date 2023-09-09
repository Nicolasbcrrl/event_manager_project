package fi.haagahelia.eventmanager.controller.opinion;

import fi.haagahelia.eventmanager.domain.User;
import fi.haagahelia.eventmanager.dto.opinion.OpinionRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/events/opinions")
public class OpinionController {
    private final OpinionService service;

    /**
     * This function is used to get all the opinions in the database. All details of this function are described in the
     * description of the function in the OpinionService class.
     * @param user - the user that is currently logged in
     * @return - a list of all the opinions wrapped in a ResponseEntity
     *         .OK - if the request was successful
     *         .INTERNAL_SERVER_ERROR - if there was an error in the server
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<?> getAllOpinions(@AuthenticationPrincipal User user){
        return ResponseEntity.ok(service.getAllOpinions(user));
    }

    /**
     * This function is used to get all the opinions in the database for a specific activity. All details of this
     * function are described in the description of the function in the OpinionService class.
     * @param user - the user that is currently logged in
     * @param id - the id of the activity concerned by the opinions
     * @return - a list of all the opinions for the activity wrapped in a ResponseEntity
     *        .OK - if the request was successful
     *        .NOT_FOUND - if the activity doesn't exist
     *        .NO_CONTENT - if there are no opinions for the activity
     *        .INTERNAL_SERVER_ERROR - if there was an error in the server
     */
    @GetMapping(path = "/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<?> getOpinionById(@AuthenticationPrincipal User user, @PathVariable("id") Long id){
        return ResponseEntity.ok(service.getAllOpinionsByActivity(user, id));
    }

    /**
     * This function is used to create a new opinion in the database using the information in the request body, the id of
     * an activity and the information of the user making the request.
     * All details of this function are described in the description of the function in the OpinionService class.
     * @param user - the user that is currently logged in
     * @param actId - the id of the activity concerned by the opinion
     * @param request - the request body to containing the rating and the comment to create the new opinion
     * @return - the newly created opinion wrapped in a ResponseEntity
     *          .CREATED - if the request was successful
     *          .BAD_REQUEST - if the request body is invalid
     *          .NOT_FOUND - if the activity doesn't exist
     *          .INTERNAL_SERVER_ERROR - if there was an error in the server     *
     */
    @PostMapping(path = "/create/{id}", consumes = "application/json", produces = "application/json")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<?> createOpinion(@AuthenticationPrincipal User user, @PathVariable("id") Long actId, @RequestBody OpinionRequest request){
        return ResponseEntity.ok(service.addOpinion(user,actId,request));
    }

    /**
     * This function is used to delete an opinion from the database using the id of the activity and the id of the user.
     * All details of this function are described in the description of the function in the OpinionService class.
     * @param user - the user that is currently logged in
     * @param id - the id of the activity concerned by the opinion
     * @return - the deleted opinion wrapped in a ResponseEntity
     *        .OK - if the request was successful
     *        .NOT_FOUND - if the opinion doesn't exist
     *        .INTERNAL_SERVER_ERROR - if there was an error in the server
     */
    @DeleteMapping(path = "/delete/{actId}", produces = "application/json")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<?> deleteOpinion(@AuthenticationPrincipal User user,@PathVariable("actId") Long id){
        return ResponseEntity.ok(service.deleteOneOpinion(id, user));
    }
}
