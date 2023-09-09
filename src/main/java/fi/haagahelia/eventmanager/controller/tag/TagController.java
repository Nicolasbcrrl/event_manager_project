package fi.haagahelia.eventmanager.controller.tag;

import fi.haagahelia.eventmanager.dto.TagDTO;
import fi.haagahelia.eventmanager.domain.Tag;
import fi.haagahelia.eventmanager.domain.User;
import fi.haagahelia.eventmanager.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import fi.haagahelia.eventmanager.security.ErrorResponse;
import java.util.ArrayList;
import java.util.List;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tags")
public class TagController {

    private final TagService service;

    /**
     * This function is used to get all tags in the database.
     * All details of this function are described in the description of the function in the TagService class.
     * @param user - the user that is currently logged in
     * @return - a list of all tags in the database wrapped in a ResponseEntity
     *         .OK - if the request was successful
     *         .NO_CONTENT - if there are no tags in the database
     *         .INTERNAL_SERVER_ERROR - if there was an error in the server
     */
    @GetMapping(produces = "application/json")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<?> getTags(@AuthenticationPrincipal User user){
        return ResponseEntity.ok(service.getTags(user));
    }

    /**
     * This function is used to create a new tag in the database using the information giving in the request body and the user making the request.
     * All details of this function are described in the description of the function in the TagService class.
     * @param user - the user that is currently logged in
     * @param tagDTO - the request body containing the name of the tag to create
     * @return - the newly created tag wrapped in a ResponseEntity
     *         .CREATED - if the request was successful
     *         .BAD_REQUEST - if the request body is invalid
     *         .INTERNAL_SERVER_ERROR - if there was an error in the server
     */
    @PostMapping(path = "/create", produces = "application/json", consumes = "application/json")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<?> createTag(@AuthenticationPrincipal User user, @RequestBody TagDTO tagDTO){
       return ResponseEntity.ok(service.createTag(user, tagDTO));
    }

    /**
     * This function is used to delete a tag in the database. The tag to delete is specified by the id in the path.
     * All details of this function are described in the description of the function in the TagService class.
     * @param user - the user that is currently logged in
     * @param id - the id of the tag to delete
     * @return - the deleted tag wrapped in a ResponseEntity
     *        .OK - if the request was successful
     *        .NOT_FOUND - if the tag with the specified id was not found
     *        .INTERNAL_SERVER_ERROR - if there was an error in the server
     */

    @DeleteMapping(path = "/{id}/delete", produces = "application/json")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> deleteTag(@AuthenticationPrincipal User user, @PathVariable("id") Long id){
        return ResponseEntity.ok(service.deleteTag(user, id));
    }


}
