package fi.haagahelia.eventmanager.controller.activity;

import fi.haagahelia.eventmanager.controller.opinion.OpinionService;
import fi.haagahelia.eventmanager.domain.*;
import fi.haagahelia.eventmanager.domain.opinion.Opinion;
import fi.haagahelia.eventmanager.dto.activity.ActivityDTO;
import fi.haagahelia.eventmanager.dto.activity.ActivityRequest;
import fi.haagahelia.eventmanager.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.util.Pair;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Log4j2
@Service
@RequiredArgsConstructor
public class ActivityService {
    private final ActivityRepository activityRepository;
    private final CountryRepository countryRepository;
    private final AddressRepository addressRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final OpinionService opinionService;
    /*----------------------------------------------- GLOBAL TOOLS ---------------------------------------------------*/

    /**
     * This methode is used to create and add the HATEOAS links to the activityDTO
     * @param activityDTO - the activityDTO to which the links will be added
     * @return - the activityDTO with the links added
     */
    public ActivityDTO createHateoasLinks(ActivityDTO activityDTO) {
        Link selfLink = linkTo(ActivityController.class).slash(String.valueOf(activityDTO.getId())).withSelfRel();
        Link collectionLink = linkTo(ActivityController.class).slash("/").withRel("activities");
        activityDTO.add(selfLink, collectionLink);
        return activityDTO;
    }

    /**
     * This methode is used to convert a list of activities to a list of activities DTO and add the HATEOAS links to the activityDTO
     * @param activities - the list of activities to which have to be converted to activityDTO
     * @return - a list of activityDTO with the links added
     */
    private List<ActivityDTO> listConvertor(List<Activity> activities){
        List<ActivityDTO> activityDTOS = new ArrayList<>();
        for(Activity activity : activities){
            ActivityDTO activityDTO = ActivityDTO.convert(activity);
            createHateoasLinks(activityDTO);
            activityDTOS.add(activityDTO);
        }
        return activityDTOS;
    }

    /**
     * This methode is used to check if the user is enough old to participate in the activity. this methode will be called
     * by the createActivity methode in this class. First it takes the system date. Then it calculates the difference between
     * the system date and the birthdate of the user. Then it checks if the age limit of the activity is 0. If it is 0 it
     * returns false. If it is not 0 it checks if the difference between the system date and the birthdate of the user is
     * smaller than the age limit of the activity. If it is smaller it returns true. If it is not smaller it returns false.
     * @param activity - the activity to which the user wants to participate
     * @param user - the user who wants to participate in the activity
     * @return - true if the user is old enough to participate in the activity
     *         - false if the user is not old enough to participate in the activity
     *         - false if the age limit of the activity is 0
     */
    public boolean checkAge(Activity activity, User user){
        LocalDate dateSystem = LocalDate.now(Clock.systemUTC());
        Period diff = Period.between(user.getBirthDate(), dateSystem);
        if (activity.getAgeLimit() == 0) {
            return false;
        }
        return diff.getYears() < activity.getAgeLimit();
    }


    /*--------------------------------------------- DISPLAY METHODS --------------------------------------------------*/

    /**
     * This function is used to display all the activities in the database. It is called by the getAllActivities function
     * in the ActivityController class. First it takes all the activities from the database. Then it checks if the list is empty.
     * If it is empty it returns an error message with the status NOT_FOUND. If the list is not empty it converts the list
     * of activities to a list of activityDTO and returns it with the status OK. If an error occurs it returns an error
     * message with the status INTERNAL_SERVER_ERROR.
     * @param user - the user who requested the activities
     * @return - a ResponseEntity with the list of activities and the status
     *              - NOT_FOUND if the list is empty
     *              - INTERNAL_SERVER_ERROR if an error occurs
     *              - OK if the list is not empty and send the list of activities converted to activityDTO
     */
    public ResponseEntity<?> allActivities(User user){
        log.info("USER " + user.getUsername().toUpperCase() + " REQUESTED ALL ACTIVITIES.");
        try {
            List<Activity> activities = activityRepository.findAll();
            if(activities.isEmpty()) {
                log.info("USER " + user.getUsername().toUpperCase() + " REQUESTED ALL ACTIVITIES. ACTIVITIES NOT FOUND.");
                return new ResponseEntity<>("NO DATA FOUND",HttpStatus.NOT_FOUND);
            }
            List<ActivityDTO> activityDTOS = listConvertor(activities);
            return new ResponseEntity<>(activityDTOS, HttpStatus.OK);
        }catch (Exception e){
            log.error("USER " + user.getUsername().toUpperCase() + " REQUESTED ALL ACTIVITIES. ERROR: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * This function is used to display all the available activities in the database, regarding the system date.
     * It is called by the getAllAvailableActivities function in the ActivityController class.
     * First it sets the system date. Then it takes all the activities from the database. Then it checks if the date of the event
     * is after the system date. If it is after the system date it converts the activity to activityDTO and adds it to the
     * list of activityDTO. Then it returns the list of activityDTO with the status OK. If the list is empty it returns an
     * error message with the status NOT_FOUND. If an error occurs it returns an error message with the status
     * INTERNAL_SERVER_ERROR.
     * @param user - the user who requested the activities
     * @return - a ResponseEntity with the list of activities and the status
     *             - NOT_FOUND if the list is empty
     *             - INTERNAL_SERVER_ERROR if an error occurs
     *             - OK if the list is not empty and send the list of activities converted to activityDTO
     */
    public ResponseEntity<?> allAvailableActivities(User user){
        try {
            log.info("USER " + user.getUsername().toUpperCase() + " REQUESTED ALL AVAILABLE ACTIVITIES.");
            LocalDate localDate = LocalDate.now(Clock.systemDefaultZone());
            List<Activity> activities = activityRepository.findAll();
            List<ActivityDTO> activityDTOS = new ArrayList<>();
            for (Activity activity : activities) {
                if (activity.getDate().isAfter(localDate)) {
                    ActivityDTO activityDTO = ActivityDTO.convert(activity);
                    createHateoasLinks(activityDTO);
                    activityDTOS.add(activityDTO);
                }
            }
            if (activityDTOS.isEmpty()) {
                log.info("USER " + user.getUsername().toUpperCase() + " REQUESTED ALL AVAILABLE ACTIVITIES. ACTIVITIES NOT FOUND.");
                return new ResponseEntity<>("NO DATA FOUND", HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(activityDTOS, HttpStatus.OK);
        } catch (Exception e){
            log.error("USER " + user.getUsername().toUpperCase() + " REQUESTED ALL AVAILABLE ACTIVITIES. ERROR: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * This function is used to display an activity by its id. It is called by the getActivityById function
     * in the ActivityController class. First it checks if the activity exists in the database. If it does not exist it
     * returns an error message with the status NOT_FOUND. If it exists it converts the activity to activityDTO and returns
     * it with the status OK. If an error occurs it returns an error message with the status INTERNAL_SERVER_ERROR.
     * @param user - the user who requested the activity
     * @param id - the id of the activity
     * @return  - a ResponseEntity with the activity and the status
     *             - NOT_FOUND if the activity does not exist
     *             - INTERNAL_SERVER_ERROR if an error occurs
     *             - OK if the activity exists and send the activity converted to activityDTO
     */
    public ResponseEntity<?> activityById(User user, Long id){
        log.info("USER " + user.getUsername().toUpperCase() + " REQUESTED ACTIVITY WITH ID " + id);
        try {
            Activity activity = activityRepository.findActivityById(id);
            if(activity == null){
                log.info("USER " + user.getUsername().toUpperCase() + " REQUESTED ACTIVITY WITH ID " + id + ". ACTIVITY NOT FOUND.");
                return new ResponseEntity<>("ACTIVITY NOT FOUND", HttpStatus.NOT_FOUND);
            }
            ActivityDTO activityDTO = ActivityDTO.convert(activity);
            createHateoasLinks(activityDTO);
            return new ResponseEntity<>(activityDTO, HttpStatus.OK);
        }catch (Exception e){
            log.error("USER " + user.getUsername().toUpperCase() + " REQUESTED ACTIVITY WITH ID " + id + ". ERROR: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /*--------------------------------------------- SEARCH METHODS ---------------------------------------------------*/

    /**
     * This function is used to search activities by name. It is called by the search function in the
     * ActivityService class. First it checks if the list of activities is empty. If it is empty it returns an error
     * message with the status NOT_FOUND. If it is not empty it converts the list of activities to a list of activityDTO
     * and returns it with the status OK. If an error occurs it returns an error message with the status
     * INTERNAL_SERVER_ERROR.
     * @param name - the name of the activity
     * @param user - the user who start the research of the activity by name
     * @return - a ResponseEntity with the following content
     *             - NOT_FOUND if the list is empty
     *             - INTERNAL_SERVER_ERROR if an error occurs
     *             - OK if the list is not empty and send the list of activities converted to activityDTO
     */
    private ResponseEntity<?> searchByName(String name, User user){
        log.info("USER " + user.getUsername().toUpperCase() + " REQUESTED ACTIVITIES WITH NAME " + name);
        try {
            List<Activity> activities = activityRepository.searchActivitiesByName(name);
            if(activities.isEmpty()){
                log.info("USER " + user.getUsername().toUpperCase() + " REQUESTED ACTIVITIES WITH NAME " + name + ". ACTIVITIES NOT FOUND.");
                return new ResponseEntity<>("ACTIVITIES NOT FOUND", HttpStatus.NOT_FOUND);
            }
            List<ActivityDTO> activityDTOS = listConvertor(activities);
            return new ResponseEntity<>(activityDTOS, HttpStatus.OK);
        }catch (Exception e){
            log.error("USER " + user.getUsername().toUpperCase() + " REQUESTED ACTIVITIES WITH NAME " + name + ". ERROR: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * This function is used to search activities by date. It is called by the search function in the
     * ActivityService class. First it checks if the list of activities is empty. If it is empty it returns an error
     * message with the status NOT_FOUND. If it is not empty it converts the list of activities to a list of activityDTO
     * and returns it with the status OK. If an error occurs it returns an error message with the status
     * INTERNAL_SERVER_ERROR.
     * @param date  - the date of the activity
     * @param user  - the user who requested the activities
     * @return - a ResponseEntity with the folowing content
     *              - NOT_FOUND if the list is empty
     *              - INTERNAL_SERVER_ERROR if an error occurs
     *              - OK if the list is not empty and send the list of activities converted to activityDTO
     */
    private ResponseEntity<?> searchByDate(LocalDate date, User user){
        log.info("USER " + user.getUsername().toUpperCase() + " REQUESTED ACTIVITIES WITH DATE " + date);
        try {
            List<Activity> activities = activityRepository.searchActivitiesByDate(date);
            if(activities.isEmpty()){
                log.info("USER " + user.getUsername().toUpperCase() + " REQUESTED ACTIVITIES WITH DATE " + date + ". ACTIVITIES NOT FOUND.");
                return new ResponseEntity<>("ACTIVITIES NOT FOUND", HttpStatus.NOT_FOUND);
            }
            List<ActivityDTO> activityDTOS = listConvertor(activities);
            return new ResponseEntity<>(activityDTOS, HttpStatus.OK);
        }catch (Exception e){
            log.error("USER " + user.getUsername().toUpperCase() + " REQUESTED ACTIVITIES WITH DATE " + date + ". ERROR: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * This function is used to search activities by city. It is called by the search function in the
     * ActivityService class. First it checks if the list of activities is empty. If it is empty it returns an error
     * message with the status NOT_FOUND. If it is not empty it converts the list of activities to a list of activityDTO
     * and returns it with the status OK. If an error occurs it returns an error message with the status
     * INTERNAL_SERVER_ERROR.
     * @param city - the city of the activity
     * @param country - the country of the activity
     * @param user  - the user who requested the activities
     * @return  - a ResponseEntity with the folowing content
     *              - NOT_FOUND if the list is empty
     *              - INTERNAL_SERVER_ERROR if an error occurs
     *              - OK if the list is not empty and send the list of activities converted to activityDTO
     */
    private ResponseEntity<?> searchByCity(String city, String country, User user){
        log.info("USER " + user.getUsername().toUpperCase() + " REQUESTED ACTIVITIES WITH CITY " + city);
        try {
            Country contryfind = countryRepository.findCountryByName(country);
            List<Activity> activities = activityRepository.searchActivitiesByCity(city, contryfind);
            if(activities.isEmpty()){
                log.info("USER " + user.getUsername().toUpperCase() + " REQUESTED ACTIVITIES WITH CITY " + city + ". ACTIVITIES NOT FOUND.");
                return new ResponseEntity<>("ACTIVITIES NOT FOUND", HttpStatus.NOT_FOUND);
            }
            List<ActivityDTO> activityDTOS = listConvertor(activities);
            return new ResponseEntity<>(activityDTOS, HttpStatus.OK);
        }catch (Exception e){
            log.error("USER " + user.getUsername().toUpperCase() + " REQUESTED ACTIVITIES WITH CITY " + city + ". ERROR: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * This function is used to search activities by tag. It is called by the search function in the
     * ActivityService class. First it checks if the list of activities is empty. If it is empty it returns an error
     * message with the status NOT_FOUND. If it is not empty it converts the list of activities to a list of activityDTO
     * and returns it with the status OK. If an error occurs it returns an error message with the status
     * INTERNAL_SERVER_ERROR.
     * @param tag - the tag of the activity
     * @param user - the user who requested the activities
     * @return - a ResponseEntity with the following content
     *              - NOT_FOUND if the list is empty
     *              - INTERNAL_SERVER_ERROR if an error occurs
     *              - OK if the list is not empty and send the list of activities converted to activityDTO
     */
    private ResponseEntity<?> searchByTag(String tag, User user){
        log.info("USER " + user.getUsername().toUpperCase() + " REQUESTED ACTIVITIES WITH TAG " + tag);
        try {
            List<Activity> activities = activityRepository.searchActivitiesByTag(tag);
            if(activities.isEmpty()){
                log.info("USER " + user.getUsername().toUpperCase() + " REQUESTED ACTIVITIES WITH TAG " + tag + ". ACTIVITIES NOT FOUND.");
                return new ResponseEntity<>("ACTIVITIES NOT FOUND", HttpStatus.NOT_FOUND);
            }
            List<ActivityDTO> activityDTOS = listConvertor(activities);
            return new ResponseEntity<>(activityDTOS, HttpStatus.OK);
        }catch (Exception e){
            log.error("USER " + user.getUsername().toUpperCase() + " REQUESTED ACTIVITIES WITH TAG " + tag + ". ERROR: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * This function is used to search an activity depending on the parameters. It is called by the searchActivities
     * function in the ActivityController class. First it checks if the tag parameter is not null. If it is not null
     * it calls the searchByTag function. Then it checks if the city and country parameters are not null. If they are
     * not null it calls the searchByCity function. Then it checks if the name parameter is not null. If it is not null
     * it calls the searchByName function. Then it checks if the date parameter is not null. If it is not null it calls
     * the searchByDate function. If an error occurs it returns an error message with the status INTERNAL_SERVER_ERROR.
     * If all the parameters are null it returns an error message with the status BAD_REQUEST.
     *
     * @param tag - the name of the activity
     * @param city - the city of the activity
     * @param country - the country of the activity
     * @param name - the name of the activity
     * @param date - the date of the activity
     * @param user - the user who requested the activities
     * @return - a ResponseEntity with the folowing content
     *              - BAD_REQUEST if all the parameters are null
     *              - INTERNAL_SERVER_ERROR if an error occurs
     *              - OK if the list is not empty and send the list of activities converted to activityDTO
     */
    public ResponseEntity<?> search(String tag, String city,String country,String name,LocalDate date, User user){
        try {
            if (tag != null){
                return searchByTag(tag, user);
            }
            if (city != null && country != null){
                return searchByCity(city, country, user);
            }
            if (name != null){
                return searchByName(name, user);
            }
            if (date != null){
                return searchByDate(date, user);
            }
            return new ResponseEntity<>("NO PARAMETERS",HttpStatus.BAD_REQUEST);
        }catch (Exception e){
            log.error("USER " + user.getUsername().toUpperCase() + " REQUESTED ACTIVITIES WITH TAG. ERROR: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /*---------------------------------------- TOOLS FOR ACTIVITY CREATION -------------------------------------------*/

    /**
     * This function is used to check if the current year il a leap year or not. It is called by the isFerbruaryCorrect function in the
     * ActivityService class. It returns true if the year is a leap year and false if it is not.
     * @param year - the year to check
     * @return - true if the year is a leap year
     *         - false if the year is not a leap year
     */
    private boolean isLeapYear(int year){
        if(year % 4 == 0){
            // if year is divisible by 4 and not divisible by 100 or divisible by 400
            return year % 100 != 0 || year % 400 == 0;
        }
        return false;
    }

    /**
     * This function is used to check if the February date is correct. It is called by the rules function in the
     * ActivityService class. It returns true if the number of days for the month of February is correct and false if it is not.
     * It checks if the year is a leap year or not. If it is a leap year it checks if the number of days is less or equal to 29.
     * If it is not a leap year it checks if the number of days is less or equal to 28.
     * If the month is not February it returns true.
     *
     * @param year  - the year of the date
     * @param month - the month of the date
     * @param day   - the day of the date
     * @return  - true if the number of days for the month of February is correct or if the month is not February
     *          - false if the number of days for the month of February is not correct
     *          - false if the month is February and the year is not a leap year and the number of days is greater than 28
     *          - false if the month is February and the year is a leap year and the number of days is greater than 29
     */
    private boolean isFebruaryCorrect(int year, int month, int day){
        if(month == 2){
            if(isLeapYear(year)){
                return day <= 29;
            }
            return day <= 28;
        }
        return true;
    }

    /**
     * This methode is used to control all the information the request made by the user.
     * In this method we check the following constraints:
     *  - request : not null
     *  - description: not null, not empty
     *  - number of places : can't be less than 1
     *  - date day: can't to be less than 1 and more than 31
     *  - date month: can't to be less than 1 and more than 12
     *  - February: number of days have to be between 1 and 28 or 29 in case of a leap year
     *  - date year: can't be less than the actual system date
     *  - date of the activity: can't be before the system date
     *  - start hour: can't be less than 1 and more than 24
     *  - start minute: can't be less than 1 and more than 60
     *  - End hour: can't be less than 1 and more than 24
     *  - End minute: can't be less than 1 and more than 60
     *  - age limitation: can't be less tan 1
     *
     * @param request - the request made by the user
     * @returna  - pair that contains the HttpStatus and a String that contains the error message
     */

    private Pair<HttpStatus, String> rules(ActivityRequest request){
        Year currentDate = Year.now(Clock.systemDefaultZone());
        LocalDate date = LocalDate.of(request.getYear(), request.getMonth(), request.getDay());
        if(request == null){
            return Pair.of(HttpStatus.BAD_REQUEST, "REQUEST IS NULL");
        }
        if (request.getName().isEmpty()){
            return Pair.of(HttpStatus.BAD_REQUEST, "NAME IS EMPTY");
        }
        if (request.getDescription().isEmpty()){
            return Pair.of(HttpStatus.BAD_REQUEST, "DESCRIPTION IS EMPTY");
        }
        if (request.getDescription() == null){
            return Pair.of(HttpStatus.BAD_REQUEST, "DESCRIPTION IS NULL");
        }
        if (request.getNumPlaces() < 1){
            return Pair.of(HttpStatus.BAD_REQUEST, "NUMBER OF PLACES HAVE TO BE GREATER THAN 0");
        }
        if (request.getDay() < 1 || request.getDay() > 31){
            return Pair.of(HttpStatus.BAD_REQUEST, "DAY HAVE TO BE BETWEEN 1 AND 31");
        }
        if (request.getMonth() < 1 || request.getMonth() > 12){
            return Pair.of(HttpStatus.BAD_REQUEST, "MONTH HAVE TO BE BETWEEN 1 AND 12");
        }
        if (request.getAgeLimit() < 0){
            return Pair.of(HttpStatus.BAD_REQUEST, "AGE CAN NOT BE LESS THAN 0");
        }
        if (!isFebruaryCorrect(request.getYear(), request.getMonth(), request.getDay())){
            return Pair.of(HttpStatus.BAD_REQUEST, "FEBRUARY HAVE TO BE BETWEEN 1 AND 28 OR 29");
        }
        if (request.getYear() < currentDate.getValue()){
            return Pair.of(HttpStatus.BAD_REQUEST, "YEAR HAVE TO BE " + currentDate.getValue() + " OR LATER");
        }
        if (date.isBefore(LocalDate.now())){
            return Pair.of(HttpStatus.BAD_REQUEST, "DATE HAVE TO BE " + LocalDate.now() + " OR LATER");
        }
        if (request.getStartHour() < 0 || request.getStartHour() > 23){
            return Pair.of(HttpStatus.BAD_REQUEST, "START HOUR HAVE TO BE BETWEEN 0 AND 23");
        }
        if (request.getStartMinute() < 0 || request.getStartMinute() > 59){
            return Pair.of(HttpStatus.BAD_REQUEST, "START MINUTE HAVE TO BE BETWEEN 0 AND 59");
        }
        if (request.getEndHour() < 0 || request.getEndHour() > 23){
            return Pair.of(HttpStatus.BAD_REQUEST, "END HOUR HAVE TO BE BETWEEN 0 AND 23");
        }
        if (request.getEndMinute() < 0 || request.getEndMinute() > 59){
            return Pair.of(HttpStatus.BAD_REQUEST, "END MINUTE HAVE TO BE BETWEEN 0 AND 59");
        }
        return Pair.of(HttpStatus.OK, "REQUEST IS VALID");
    }

    /**
     * This function is used to check if the user is the creator of the activity. It is called by the createActivity function in the
     * ActivityService class. It returns true if the user is the creator of the activity and false if it is not.
     * @param user - the user to check
     * @param activity   - the activity to check
     * @return - true if the user is the creator of the activity
     */
    private boolean creatorCheck(User user, Activity activity){
        return user.getUsername().equals(activity.getCreator().getUsername());
    }

    /**
     * This function is used to check if there is an activity that use an address at the same date and time. It is called by the createActivity function in the
     * ActivityService class. It returns a pair of HttpStatus and String message.
     * First it takes all the activities that use the address. Then it checks if the name, date, start time and end time of the activity are the same
     * as the name, date, start time and end time of the activity in the database. If they are the same it will delete the activity that request for the address and
     * returns a pair of HttpStatus and String message. In cas that an activity take place at the same time and date it will return a pair of HttpStatus and String message.
     * If they are not the same it returns a pair of HttpStatus and String message.
     * @param act - the activity to check
     * @param address - the address of the activity
     * @return - a pair of HttpStatus and String message
     *         - HttpStatus.CONFLICT and "ACTIVITY DELETED BECAUSE ALREADY EXISTENT"
     *         - HttpStatus.CONFLICT and "THE ADDRESS IS ALREADY BOOK FOR AN OTHER ACTIVITY"
     *         - HttpStatus.OK and "ACTIVITY DOES NOT EXIST"
     *         - HttpStatus.INTERNAL_SERVER_ERROR and the error message
     */
    private Pair<HttpStatus, String> activityAlreadyExists(Activity act, Address address){
        List<Activity> activities = activityRepository.findAllByAddress(address);
        for (Activity activity : activities) {
            if (activity.getName().equals(act.getName()) && activity.getDate().equals(act.getDate())
                    && activity.getStartTime().equals(act.getStartTime()) && activity.getEndTime().equals(act.getEndTime())) {
                activityRepository.delete(act);
                return Pair.of(HttpStatus.CONFLICT, "ACTIVITY DELETED BECAUSE ALREADY EXISTENT");
            }
            if ( activity.getDate().equals(act.getDate())
                    && activity.getStartTime().equals(act.getStartTime()) && activity.getEndTime().equals(act.getEndTime())) {
                return Pair.of(HttpStatus.CONFLICT, "THE ADDRESS IS ALREADY BOOK FOR AN OTHER ACTIVITY");
            }
        }
        return Pair.of(HttpStatus.OK, "ACTIVITY DOES NOT EXIST");
    }


    /*--------------------------------------------- CREATE ACTIVITY --------------------------------------------------*/

    /**
     * This function is used to create an activity. It is called by the createActivity function in the ActivityCotroller class.
     * First it checks if the request is valid by calling the rules function. If the request is valid it creates the activity, and
     * It will check if the age int the request is greater than 0. If it is greater than 0 it will set the age limit of the activity
     * to the age in the request. If the age is not greater than 0 it will not set the age limit of the activity. Then it will save the
     * activity in the database and return a ResponseEntity with the activity, link HATEOAS and HttpStatus.CREATED. If the request is not valid it will
     * return a ResponseEntity with the HttpStatus and the String returned by the rules function.
     *
     * @param user - the user that requested the activity creation
     * @param request - the request to create the activity
     * @return - the response will be wrapped into a ResponseEntity object with the following content:
     *       - a ResponseEntity with the HttpStatus.BAD_REQUEST and the String returned by the rules function if the request is not valid
     *       - a ResponseEntity with the HttpStatus.INTERNAL_SERVER_ERROR if there is an error
     *       - a ResponseEntity with the activity, link HATEOAS and HttpStatus.CREATED if the request is valid
     */
    public ResponseEntity<?> createActivity(User user, ActivityRequest request){
        try {
            log.info("USER " + user.getUsername().toUpperCase() + " REQUESTED ACTIVITY CREATION.");
            Pair<HttpStatus, String> rules = rules(request);
            if(rules.getFirst().equals(HttpStatus.OK)){
                LocalDate date = LocalDate.of(request.getYear(), request.getMonth(), request.getDay());
                LocalTime startTime = LocalTime.of(request.getStartHour(), request.getStartMinute());
                LocalTime endTime = LocalTime.of(request.getEndHour(), request.getEndMinute());
                Activity activity = new Activity(request.getName(), date,startTime,endTime,request.getDescription(),
                        request.getNumPlaces(),user);
                if(request.getAgeLimit() > 0){
                    activity.setAgeLimit(request.getAgeLimit());
                }
                activityRepository.save(activity);
                ActivityDTO activityDTO = ActivityDTO.convert(activity);
                log.info("USER " + user.getUsername().toUpperCase() + " CREATED AN ACTIVITY.");
                createHateoasLinks(activityDTO);
                return new ResponseEntity<>(activityDTO, HttpStatus.CREATED);
            }
            log.error("USER " + user.getUsername().toUpperCase() + " REQUESTED ACTIVITY CREATION. ERROR: " + rules.getSecond());
            return new ResponseEntity<>(rules.getSecond(), rules.getFirst());
        }catch (Exception e){
            log.error("USER " + user.getUsername().toUpperCase() + " REQUESTED ACTIVITY CREATION. ERROR: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    //--------------------------------------------- ADD ADDRESS

    /**
     * This function is used to add an address to an activity. It is called by the addAddressToActivity function in the ActivityController class.
     * First it checks if the activity exists. If the activity does not exist it will return a ResponseEntity with the HttpStatus.NOT_FOUND and the
     * String "ACTIVITY NOT FOUND". If the activity exists, it checks if the activity exists by calling the activityRepository.
     * If the activity does not exist it will return a ResponseEntity with the HttpStatus.NOT_FOUND and the String "ACTIVITY NOT FOUND".
     * If the activity exists it will check if the user is the creator of the activity. it checks if the user is the creator of the activity by calling the creatorCheck function. If the user is the creator of the activity
     * it will check if the activity already exists by calling the activityAlreadyExists function. If the activity already exists it will return a
     * ResponseEntity with the HttpStatus and the String returned by the activityAlreadyExists function. If the activity does not exist it will
     * check if the address already exists by calling the addressAlreadyExists function. If the address already exists it will return a ResponseEntity
     * with the HttpStatus and the String returned by the addressAlreadyExists function. If the address does not exist it will add the address to the
     * activity and save the activity in the database. Then it will return a ResponseEntity with the activity, link HATEOAS and HttpStatus.OK.
     * If the user is not the creator of the activity it will return a ResponseEntity with the HttpStatus.UNAUTHORIZED.
     * @param user - the user that requested the address addition
     * @param actId - the id of the activity
     * @param addId - the id of the address
     * @return - the response will be wrapped into a ResponseEntity object with the following content:
     *        - a ResponseEntity with the HttpStatus.NOT_FOUND and the String "ACTIVITY NOT FOUND" if the activity does not exist
     *        - a ResponseEntity with the HttpStatus.NOT_FOUND and the String "ADDRESS NOT FOUND" if the address does not exist
     *        - a ResponseEntity with the HttpStatus.UNAUTHORIZED if the user is not the creator of the activity
     *        - a ResponseEntity with the activity, link HATEOAS and HttpStatus.OK if the user is the creator of the activity and the address does not exist
     *        - a ResponseEntity with the HttpStatus.INTERNAL_SERVER_ERROR if there is an error
     *
     */
    public ResponseEntity<?> addAdress(User user, Long actId, Long addId){
        try {
            Activity activity = activityRepository.findActivityById(actId);
            Address address = addressRepository.findAddressById(addId);
            if(activity == null){
                log.error("USER " + user.getUsername().toUpperCase() + " REQUESTED ADDRESS ADDITION TO ACTIVITY " + actId + ". ERROR: ACTIVITY NOT FOUND");
                return new ResponseEntity<>("ACTIVITY NOT FOUND", HttpStatus.NOT_FOUND);
            }
            if(address == null){
                log.error("USER " + user.getUsername().toUpperCase() + " REQUESTED ADDRESS ADDITION TO ACTIVITY " + actId + ". ERROR: ADDRESS NOT FOUND");
                return new ResponseEntity<>("ADDRESS NOT FOUND", HttpStatus.NOT_FOUND);
            }
            if(!creatorCheck(user, activity)) {
                log.error("USER " + user.getUsername().toUpperCase() + " REQUESTED ADDRESS ADDITION TO ACTIVITY " + actId + ". ERROR: USER IS NOT THE CREATOR");
                return new ResponseEntity<>("USER " + user.getUsername().toUpperCase() + " IS NOT THE CREATOR", HttpStatus.UNAUTHORIZED);
            }
            log.info("USER " + user.getUsername().toUpperCase() + " REQUESTED ADDRESS ADDITION TO ACTIVITY " + activity.getName().toUpperCase());
            Pair<HttpStatus, String> exists = activityAlreadyExists(activity,address);
            if (exists.getFirst().equals(HttpStatus.OK)){
                activity.setAddress(address);
                activityRepository.save(activity);
                log.info("USER " + user.getUsername().toUpperCase() + " ADDED ADDRESS TO ACTIVITY " + activity.getName().toUpperCase());
                return new ResponseEntity<>("ADDRESS SUCCESSFULLY ADDED",HttpStatus.OK);
            }
            log.info("USER " + user.getUsername().toUpperCase() + " REQUESTED ADDRESS ADDITION TO ACTIVITY " + actId + ". ERROR: " + exists.getSecond());
            return new ResponseEntity<>(exists.getSecond(), exists.getFirst());
        }catch (Exception e){
            log.error("USER " + user.getUsername().toUpperCase() + " REQUESTED ADDRESS ADDITION TO ACTIVITY " + actId + ". ERROR: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    //--------------------------------------------- REMOVE ADDRESS FROM ACTIVITY BY ADDRESS ID

    /**
     * This function is used to remove an address from all activities that are related to this address.
     * It is called by the removeAddressFromActivity function in the ActivityController class.
     * First it checks if the address is present in the database. If the address is not present in the database it will return a ResponseEntity with the
     * HttpStatus.NOT_FOUND and the String "ADDRESS NOT FOUND". If the address is present in the database it will get all the activities that are related
     * to this address and set the address of each activity to null. Then it will save each activity in the database. Finally, it will return a ResponseEntity
     * with the HttpStatus.OK and the String "ADDRESS SUCCESSFULLY REMOVED FROM ACTIVITIES".
     * In case of other error it will return a ResponseEntity with the HttpStatus.INTERNAL_SERVER_ERROR
     *
     * @param addId - the id of the address
     * @return - the response will be wrapped into a ResponseEntity object with the following content:
     *           - a ResponseEntity with the HttpStatus.NOT_FOUND and the String "ADDRESS NOT FOUND" if the address is not present in the database
     *           - a ResponseEntity with the HttpStatus.OK and a confimation message if the address is present in the database
     *           - a ResponseEntity with the HttpStatus.INTERNAL_SERVER_ERROR in case of other error
     */
    public Pair<HttpStatus, String> removeAddressFromActivity(Long addId){
        try {
            Address address = addressRepository.findAddressById(addId);
            if(address == null){
                return Pair.of(HttpStatus.NOT_FOUND, "ADDRESS NOT FOUND");
            }
            List<Activity> activities = activityRepository.findAllByAddress(address);
            for (Activity activity : activities) {
                activity.setAddress(null);
                activityRepository.save(activity);
            }
            return Pair.of(HttpStatus.OK, "ADDRESS SUCCESSFULLY REMOVED FROM ACTIVITIES");
        }catch (Exception e){
            return Pair.of(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    //--------------------------------------------- ADD TAG

    /**
     * This function is used to add tags to an activity. It is called by the addTagsToActivity function in the ActivityController class.
     * First it checks if the user is the creator of the activity by calling the creatorCheck function. If the user is the creator of the activity
     * it will check if the activity have already the tags. If not it will add the tags to the activity and save the activity.
     * Then it will check if the list of tags of the activity is empty. If it is empty it will return a ResponseEntity with the HttpStatus.BAD_REQUEST.
     * If the list of tags of the activity is not empty it will return a ResponseEntity with the message TAGS SUCCESSFULLY ADDED and HttpStatus.OK.
     * In case of other error it will return a ResponseEntity with the HttpStatus.INTERNAL_SERVER_ERROR
     * @param user - the user that requested the tags addition
     * @param actId - the id of the activity
     * @param tags - the list of tags
     * @return - the response will be wrapped into a ResponseEntity object with the following content:
     *           - a ResponseEntity with the message TAGS SUCCESSFULLY ADDED and HttpStatus.OK if the user is the creator of the activity
     *           - a ResponseEntity with the HttpStatus.UNAUTHORIZED if the user is not the creator of the activity
     *           - a ResponseEntity with the HttpStatus.BAD_REQUEST if the list of tags of the activity is empty
     *           - a ResponseEntity with the HttpStatus.INTERNAL_SERVER_ERROR in case of other error
     */
    public ResponseEntity<?> addTags(User user, Long actId, List<String> tags){
        try{
            Activity activity = activityRepository.findActivityById(actId);
            if(!creatorCheck(user, activity)) {
                log.error("USER " + user.getUsername().toUpperCase() + " REQUESTED TAGS ADDITION TO ACTIVITY " + actId + ". ERROR: USER IS NOT THE CREATOR");
                return new ResponseEntity<>("USER " + user.getUsername().toUpperCase() + " IS NOT THE CREATOR", HttpStatus.UNAUTHORIZED);
            }
            log.info("USER " + user.getUsername().toUpperCase() + " REQUESTED TAGS ADDITION TO ACTIVITY " + activity.getName().toUpperCase());
            List<Tag> tagList = activity.getTags();
            for (String tag : tags){
                if(tagRepository.existsByName(tag)){
                    if (!tagList.contains(tagRepository.findTagByName(tag))){
                        activity.addTag(tagRepository.findTagByName(tag));
                    }
                }

            }
            if (activity.getTags().isEmpty()){
                log.error("list is empty");
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }else {
                activityRepository.save(activity);
                log.info("USER " + user.getUsername().toUpperCase() + " ADDED TAGS TO ACTIVITY " + activity.getName().toUpperCase());
                return new ResponseEntity<>("TAGS SUCCESSFULLY ADDED",HttpStatus.OK);
            }
        }catch (Exception e){
            log.error("USER " + user.getUsername().toUpperCase() + " REQUESTED TAGS ADDITION TO ACTIVITY " + actId + ". ERROR: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    //--------------------------------------------- REMOVE TAG

    /**
     * This function is used to remove a tag from all activities that are related to this tag. It is called by the removeTag function in the TagService class.
     * First it checks if the tag is present in the database. If the tag is not present in the database it will return a ResponseEntity with the
     * HttpStatus.BAD_REQUEST and the String "TAG DOES NOT EXIST". If the tag is present in the database it will get all the activities that are related
     * to this tag and remove the tag from each activity. Then it will save each activity in the database. Finally, it will return a ResponseEntity
     * with the HttpStatus.OK and a confirmation message.
     * In case of other error it will return a ResponseEntity with the HttpStatus.INTERNAL_SERVER_ERROR and the error message.
     *
     * @param tagId - the id of the tag
     * @return - the response will be wrapped into a ResponseEntity object with the following content:
     *          - a ResponseEntity with the HttpStatus.BAD_REQUEST and the String "TAG DOES NOT EXIST" if the tag is not present in the database
     *          - a ResponseEntity with the HttpStatus.OK and a message to confirme the removal of the tag from the activities if the tag is present in the database
     *          - a ResponseEntity with the HttpStatus.INTERNAL_SERVER_ERROR and the error message in case of other error
     */
    public Pair<HttpStatus, String> removeTagFromActivity(Long tagId){
        try{
            Tag tag = tagRepository.findTagById(tagId);
            if (tag == null){
                return Pair.of(HttpStatus.NOT_FOUND, "TAG DOES NOT EXIST");
            }
            List<Activity> activities = activityRepository.findAllByTag(tag.getId());
            for (Activity activity : activities) {
                activity.removeTag(tag);
                activityRepository.save(activity);
            }
            return Pair.of(HttpStatus.OK, "TAG: " + tag.getName() + ", SUCCESSFULLY REMOVED FROM ACTIVITIES");

        }catch (Exception e){
            return Pair.of(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
    //--------------------------------------------- ADD PARTICIPANT STANDARD

    /**
     * This function is used to add a participant to an activity. It is called by the addParticipant function in the ActivityController class.
     * First it checks if the activity exists. If the activity does not exist it will return a ResponseEntity with the HttpStatus.NOT_FOUND and the
     * String "ACTIVITY NOT FOUND". If the activity exists,     it checks if the user is the creator of the activity by calling the creatorCheck function. If the user is the creator of the activity
     * it will check if the activity have already the user as a participant. If not it will check if the user is enought old to participate in the activity,
     * by calling the function checkAge. If the user is enough old to participate in the activity the methdoe will call the methode addParticipant in the Activity call.
     * it will check if the activity is full. If the activity is full it will add the user to the waiting list of the activity. If the activity is not full
     * it will add the user to the list of participants of the activity. Then it will save the activity in the database.
     * Then it will return a ResponseEntity with the HttpStatus.OK and a confirmation message.
     * In case of other error it will return a ResponseEntity with the HttpStatus.INTERNAL_SERVER_ERROR
     * @param user - the user that requested to participate in the activity
     * @param actId - the id of the activity
     * @return - the response will be wrapped into a ResponseEntity object with the following content:
     *            - a ResponseEntity with the HttpStatus.OK and the confirmation message
     *            - a ResponseEntity with the HttpStatus.CONFLICT and the conflict message
     *            - a ResponseEntity with the HttpStatus.UNAUTHORIZED and the unauthorized message
     *            - a ResponseEntity with the HttpStatus.INTERNAL_SERVER_ERROR in case of other error
     */
    public ResponseEntity<?> addParticipant(User user, Long actId){
        try{
            Activity activity = activityRepository.findActivityById(actId);
            if (activity == null){
                log.error("USER " + user.getUsername().toUpperCase() + " REQUESTED TO PARTICIPATE  TO ACTIVITY " + actId + ". ERROR: ACTIVITY NOT FOUND");
                return new ResponseEntity<>("ACTIVITY NOT FOUND", HttpStatus.NOT_FOUND);
            }
            log.info("USER " + user.getUsername().toUpperCase() + " REQUESTED TO PARTICIPATE  TO ACTIVITY " + activity.getName().toUpperCase());
            if (activity.getParticipants().contains(user)){
                log.error("USER " + user.getUsername().toUpperCase() + " REQUESTED TO PARTICIPATE  TO ACTIVITY " + actId + ". ERROR: USER IS ALREADY A PARTICIPANT");
                return new ResponseEntity<>("USER " + user.getUsername().toUpperCase() + " IS ALREADY A PARTICIPANT", HttpStatus.CONFLICT);
            }
            if (activity.getWaitingList().contains(user)){
                log.error("USER " + user.getUsername().toUpperCase() + " REQUESTED TO PARTICIPATE  TO ACTIVITY " + actId + ". ERROR: USER IS ALREADY IN THE WAITING LIST");
                return new ResponseEntity<>("USER " + user.getUsername().toUpperCase() + " IS ALREADY IN THE WAITING LIST", HttpStatus.CONFLICT);
            }
            if(checkAge(activity,user)){
                log.error("USER " + user.getUsername().toUpperCase() + " REQUESTED TO PARTICIPATE  TO ACTIVITY " + actId + ". ERROR: USER IS TOO YOUNG");
                return new ResponseEntity<>("USER " + user.getUsername().toUpperCase() + " IS TOO YOUNG", HttpStatus.FORBIDDEN);
            }
            Pair<HttpStatus,String> addResult = activity.addParticipant(user);
            activityRepository.save(activity);
            log.info("USER " + user.getUsername().toUpperCase() + " PARTICIPATED  TO ACTIVITY " + activity.getName().toUpperCase());
            return new ResponseEntity<>(addResult.getSecond(),addResult.getFirst());
        }catch (Exception e){
            log.error("USER " + user.getUsername().toUpperCase() + " REQUESTED TO PARTICIPATE  TO ACTIVITY " + actId + ". ERROR: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    //--------------------------------------------- DELETE PARTICIPANT

    /**
     * This function is used to delete a participant from an activity. It is called by the removeParticipant function in the ActivityController class.
     * First it checks if the user is the creator of the activity by calling the creatorCheck function. If the user is the creator of the activity
     * it will check if the activity have the user as a participant. If not it will return a ResponseEntity with the HttpStatus.CONFLICT.
     * If the user is a participant of the activity, it will be removed from the main list of participants. Then it will return a ResponseEntity with the
     * HttpStatus.Ok and a confirmation message.
     * In case of other error it will return a ResponseEntity with the HttpStatus.INTERNAL_SERVER_ERROR
     *
     * @param creator - the user that requested the deletion of the participant
     * @param userId - the id of the user that will be deleted from the activity
     * @param actId - the id of the activity
     * @return - the response will be wrapped into a ResponseEntity object with the following content:
     *             - a ResponseEntity with the HttpStatus.OK and the confirmation message
     *             - a ResponseEntity with the HttpStatus.CONFLICT and the conflict message
     *             - a ResponseEntity with the HttpStatus.UNAUTHORIZED and the unauthorized message
     *             - a ResponseEntity with the HttpStatus.INTERNAL_SERVER_ERROR in case of other error
     */
    public ResponseEntity<?> deleteParticipant(User creator, Long userId, Long actId){
        try{
            Activity activity = activityRepository.findActivityById(actId);
            User user = userRepository.findUserById(userId);
            if(!creatorCheck(creator, activity)) {
                log.error("USER " + user.getUsername().toUpperCase() + " REQUESTED TAGS ADDITION TO ACTIVITY " + actId + ". ERROR: USER IS NOT THE CREATOR");
                return new ResponseEntity<>("USER " + creator.getUsername().toUpperCase() + " IS NOT THE CREATOR", HttpStatus.UNAUTHORIZED);
            }
            log.info("USER " + user.getUsername().toUpperCase() + " REQUESTED TO DELETE PARTICIPATION  TO ACTIVITY " + activity.getName().toUpperCase());
            if (!activity.getParticipants().contains(user)){
                log.error("USER " + user.getUsername().toUpperCase() + " REQUESTED TO DELETE PARTICIPATION  TO ACTIVITY " + actId + ". ERROR: USER IS NOT A PARTICIPANT");
                return new ResponseEntity<>("USER " + user.getUsername().toUpperCase() + " IS NOT A PARTICIPANT", HttpStatus.NOT_FOUND);
            }
            activity.deleteParticipant(user);
            activityRepository.save(activity);
            log.info("USER " + user.getUsername().toUpperCase() + " DELETED PARTICIPATION  TO ACTIVITY " + activity.getName().toUpperCase());
            return new ResponseEntity<>("USER " + user.getUsername().toUpperCase() + " SUCCESSFULLY DELETED PARTICIPATION",HttpStatus.OK);
        }catch (Exception e){
            log.error("USER " + creator.getUsername().toUpperCase() + " REQUESTED TO DELETE PARTICIPATION  TO ACTIVITY " + actId + ". ERROR: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    //--------------------------------------------- ADD PARTICIPANT USING WAITING LIST

    /**
     * This function is used to add a participant to an activity using the waiting list. It is called by the addWaitingParticipantToActivity function in the ActivityController class.
     * First it checks if the activity exists. If the activity does not exist it will return a ResponseEntity with the HttpStatus.NOT_FOUND and the
     * String "ACTIVITY NOT FOUND". If the activity exist, it will check if the user is the creator of the activity by calling the creatorCheck function. If the user is the creator of the activity
     * It will call the freePlace function in the Activity class. If the function returns a HttpStatus.OK it will return a ResponseEntity with the
     * HttpStatus.OK and a confirmation message. If the function returns a HttpStatus.CONFLICT it will return a ResponseEntity with the
     * HttpStatus.CONFLICT and a conflict message. If the user is not the creator of the activity it will return a ResponseEntity with the
     * HttpStatus.UNAUTHORIZED and an unauthorized message.
     * In case of other error it will return a ResponseEntity with the HttpStatus.INTERNAL_SERVER_ERROR
     * @param user - the user that requested the addition of the participant
     * @param actId - the id of the activity
     * @return - the response will be wrapped into a ResponseEntity object with the following content:
     *             - a ResponseEntity with the HttpStatus.OK and the confirmation message
     *             - a ResponseEntity with the HttpStatus.NOT_FOUND and the not found message
     *             - a ResponseEntity with the HttpStatus.CONFLICT and the conflict message
     *             - a ResponseEntity with the HttpStatus.UNAUTHORIZED and the unauthorized message
     *             - a ResponseEntity with the HttpStatus.INTERNAL_SERVER_ERROR
     */
    public ResponseEntity<?> addWaitingParticipant(User user, Long actId){
        try{
            Activity activity = activityRepository.findActivityById(actId);
            if (activity == null){
                log.error("USER " + user.getUsername().toUpperCase() + " REQUESTED TO ADD WAITING PARTICIPANT TO ACTIVITY " + actId + ". ERROR: ACTIVITY DOESN'T EXIST");
                return new ResponseEntity<>("ACTIVITY DOESN'T EXIST", HttpStatus.NOT_FOUND);
            }
            if (!creatorCheck(user, activity)){
                log.error("USER " + user.getUsername().toUpperCase() + " REQUESTED TO ADD WAITING PARTICIPANT TO ACTIVITY " + actId + ". ERROR: USER IS NOT THE CREATOR");
                return new ResponseEntity<>("USER " + user.getUsername().toUpperCase() + " IS NOT THE CREATOR", HttpStatus.UNAUTHORIZED);
            }
            log.info("USER " + user.getUsername().toUpperCase() + " REQUESTED TO ADD WAITING PARTICIPANT TO ACTIVITY " + activity.getName().toUpperCase());
            Pair<HttpStatus,String> addResult = activity.freePlace();
            activityRepository.save(activity);
            return new ResponseEntity<>(addResult.getSecond(),addResult.getFirst());
        }catch (Exception e){
            log.error("USER " + user.getUsername().toUpperCase() + " REQUESTED TO ADD WAITING PARTICIPANTS. ERROR: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    //--------------------------------------------- DELETE WAITING PARTICIPANT

    /**
     * This function is used to delete an activity. It is called by the removeActivity function in the ActivityController class.
     * First it checks if the activity exists. If not it will return a ResponseEntity with the HttpStatus.NOT_FOUND and a message.
     * If the activity exists it will check if the user is the creator of the activity by calling the creatorCheck function.
     * If the user is the creator of the activity it will check if the activity have opinions, by calling the deleteOpinion function
     * in the OpinionService class. If the function returns a false it will return a ResponseEntity with the HttpStatus.INTERNAL_SERVER_ERROR.
     * If the function returns a true it will delete the activity and return a ResponseEntity with the
     * HttpStatus.OK and a confirmation message. If the user is not the creator of the activity it will return a ResponseEntity with the
     * HttpStatus.UNAUTHORIZED and an unauthorized message.
     *
     * @param user - the user that requested the deletion of the activity
     * @param actId - the id of the activity
     * @return - the response will be wrapped into a ResponseEntity object with the following content:
     *            - a ResponseEntity with the HttpStatus.OK and the confirmation message
     *            - a ResponseEntity with the HttpStatus.NOT_FOUND and the not found message
     *            - a ResponseEntity with the HttpStatus.UNAUTHORIZED and the unauthorized message
     *            - a ResponseEntity with the HttpStatus.INTERNAL_SERVER_ERROR
     */
    public ResponseEntity<?> deleteActivity(User user, Long actId){
        try{
            Activity activity = activityRepository.findActivityById(actId);
            if (activity == null){
                log.error("USER " + user.getUsername().toUpperCase() + " REQUESTED TO DELETE ACTIVITY " + actId + ". ERROR: ACTIVITY DOESN'T EXIST");
                return new ResponseEntity<>("ACTIVITY DOESN'T EXIST", HttpStatus.NOT_FOUND);
            }
            if (!creatorCheck(user, activity)){
                log.error("USER " + user.getUsername().toUpperCase() + " REQUESTED TO DELETE WAITING PARTICIPANT TO ACTIVITY " + activity.getName() + ". ERROR: USER DOESN'T HAVE THE AUTHORISATION TO DELETE THIS EVENT");
                return new ResponseEntity<>("USER " + user.getUsername().toUpperCase() + " DOESN'T HAVE THE AUTHORISATION TO DELETE THIS EVENT", HttpStatus.UNAUTHORIZED);
            }
            if (!opinionService.deleteOpinion(actId)){
                log.error("USER " + user.getUsername().toUpperCase() + " REQUESTED TO DELETE ACTIVITY " + activity.getName() + ". ERROR: OPINIONS COULDN'T BE DELETED");
                return new ResponseEntity<>("OPINIONS COULDN'T BE DELETED", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            log.info("USER " + user.getUsername().toUpperCase() + " REQUESTED TO DELETE ACTIVITY " + activity.getName().toUpperCase());
            activityRepository.delete(activity);
            return new ResponseEntity<>("ACTIVITY " + activity.getName().toUpperCase() + " SUCCESSFULLY DELETED",HttpStatus.OK);
        }catch (Exception e){
            log.error("USER " + user.getUsername().toUpperCase() + " REQUESTED TO DELETE AN ACTIVITY. ERROR: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    //--------------------------------------------- MODIFY ACTIVITY

    /**
     *This function will modify an activity. It is called by the updateActivity function in the ActivityController class.
     * First it checks if the activity exists. If not it will return a ResponseEntity with the HttpStatus.NOT_FOUND and a message.
     * Then it will check if the user is the creator of the activity by calling the creatorCheck function.
     * If the user is the creator of the activity it will check if the activity already exists by calling the updateControle function.
     * If the user is not the creator, the function will return a ResponseEntity with the HttpStatus.UNAUTHORIZED and an unauthorized message.
     * If the activity already exists it will return a ResponseEntity with the HttpStatus.CONFLICT and the String returned.
     * It there is no conflict it will modify the activity and return a ResponseEntity with the HttpStatus.OK and a confirmation message.
     * In case of other error it will return a ResponseEntity with the HttpStatus.INTERNAL_SERVER_ERROR.
     * @param user - the user that requested the modification of the activity
     * @param actId - the id of the activity
     * @param activityRequest - the request with the new information
     * @return - the response will be wrapped into a ResponseEntity object with the following content:
     *             - a ResponseEntity with the HttpStatus.OK and the confirmation message
     *             - a ResponseEntity with the HttpStatus.NOT_FOUND and the not found message
     *             - a ResponseEntity with the HttpStatus.CONFLICT and the conflict message
     *             - a ResponseEntity with the HttpStatus.UNAUTHORIZED and the unauthorized message
     *             - a ResponseEntity with the HttpStatus.INTERNAL_SERVER_ERROR
     */
    public ResponseEntity<?> modifyActivity(User user, Long actId, ActivityRequest activityRequest){
        try{
            Activity activity = activityRepository.findActivityById(actId);
            if (activity == null){
                log.error("USER " + user.getUsername().toUpperCase() + " REQUESTED TO MODIFY ACTIVITY " + actId + ". " +
                        "ERROR: ACTIVITY DOESN'T EXIST");
                return new ResponseEntity<>("ACTIVITY DOESN'T EXIST", HttpStatus.NOT_FOUND);
            }
            if(!creatorCheck(user, activity)) {
                log.error("USER " + user.getUsername().toUpperCase() + " REQUESTED TO MODIFY ACTIVITY " + actId + ". " +
                        "ERROR: USER IS NOT THE CREATOR");
                return new ResponseEntity<>("USER " + user.getUsername().toUpperCase() + " IS NOT THE CREATOR",
                        HttpStatus.UNAUTHORIZED);
            }
            log.info("USER " + user.getUsername().toUpperCase() + " REQUESTED TO MODIFY ACTIVITY " +
                    activity.getName().toUpperCase());
            LocalDate date = LocalDate.of(activityRequest.getYear(),activityRequest.getMonth(),activityRequest.getDay());
            LocalTime start = LocalTime.of(activityRequest.getStartHour(),activityRequest.getStartMinute());
            LocalTime end = LocalTime.of(activityRequest.getEndHour(),activityRequest.getEndMinute());
            activity.setName(activityRequest.getName());
            activity.setDescription(activityRequest.getDescription());
            activity.setDate(date);
            activity.setStartTime(start);
            activity.setEndTime(end);
            activity.setNumPlaces(activityRequest.getNumPlaces());
            activity.setAgeLimit(activityRequest.getAgeLimit());
            if(activityRepository.existsActivityByNameAndDateAndStartTimeAndEndTime(activity.getName(), activity.getDate(),
                    activity.getStartTime(), activity.getEndTime())){
                log.error("USER " + user.getUsername().toUpperCase() + " REQUESTED TO MODIFY ACTIVITY " + actId + ". " +
                        "ERROR: ACTIVITY ALREADY EXISTS");
                return new ResponseEntity<>("ACTIVITY ALREADY EXISTS", HttpStatus.CONFLICT);
            }
            activityRepository.save(activity);
            ActivityDTO activityDTO = ActivityDTO.convert(activity);
            log.info("USER " + user.getUsername().toUpperCase() + " MODIFIED ACTIVITY " + activity.getName().toUpperCase());
            return new ResponseEntity<>(activityDTO,HttpStatus.OK);
        }catch (Exception e){
            log.error("USER " + user.getUsername().toUpperCase() + " REQUESTED TO MODIFY AN ACTIVITY. ERROR: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
