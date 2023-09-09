package fi.haagahelia.eventmanager.domain;

import jakarta.persistence.*;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

@Entity
@Table(name = "eve_activity")
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "act_no")
    private Long id;

    @Column(name = "act_name", nullable = false)
    private String name;

    @Column(name = "act_date", nullable = false)
    private LocalDate date;

    @Column(name = "act_description", nullable = false)
    private String description;

    @Column(name = "act_num_palces", nullable = false)
    private int numPlaces;

    @Column(name = "act_age_limit", nullable = true)
    private int ageLimit;

    @Column(name = "act_start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "act_end_time", nullable = false)
    private LocalTime endTime;

    private Random random = new Random();
    /*------------------------------------------------ RELATIONS -----------------------------------------------------*/
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "act_add_no")
    private Address address;

    @ManyToOne
    @JoinColumn(name = "act_use_no")
    private User creator;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.REFRESH)
    @JoinTable(name = "eve_act_tag", joinColumns = @JoinColumn(name = "act_no"), inverseJoinColumns = @JoinColumn(name = "tag_no"))
    private List<Tag> tags;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.REFRESH)
    @JoinTable(name = "eve_participate", joinColumns = @JoinColumn(name = "act_no"), inverseJoinColumns = @JoinColumn(name = "use_no"))
    private List<User> participants;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.REFRESH)
    @JoinTable(name = "eve_waiting_list", joinColumns = @JoinColumn(name = "act_no"), inverseJoinColumns = @JoinColumn(name = "use_no"))
    private List<User> waitingList;

    //Empty constructor for JPA needs
    public Activity() {
    }

    //Constructor for creating activity with age limit
    public Activity(String name, LocalDate date,LocalTime startTime, LocalTime endTime, String description, int numPlaces, int ageLimit, Address address, User creator) {
        this.name = name;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.description = description;
        this.numPlaces = numPlaces;
        this.ageLimit = ageLimit;
        this.address = address;
        this.creator = creator;
        this.tags = new ArrayList<>();
        this.participants = new ArrayList<>();
        this.waitingList = new ArrayList<>();
    }

    //Constructor for creating activity without age limit
    public Activity(String name, LocalDate date, LocalTime startTime, LocalTime endTime, String description, int numPlaces, Address address, User creator) {
        this.name = name;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.description = description;
        this.numPlaces = numPlaces;
        this.address = address;
        this.creator = creator;
        this.tags = new ArrayList<>();
        this.participants = new ArrayList<>();
        this.waitingList = new ArrayList<>();
    }

    //Constructor for creating activity with age limit to add an address later
    public Activity(String name, LocalDate date, LocalTime startTime, LocalTime endTime, String description, int numPlaces, int ageLimit, User creator) {
        this.name = name;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.description = description;
        this.numPlaces = numPlaces;
        this.ageLimit = ageLimit;
        this.creator = creator;
        this.tags = new ArrayList<>();
        this.participants = new ArrayList<>();
        this.waitingList = new ArrayList<>();
    }

    //Constructor for creating activity without age limit to add an address later
    public Activity(String name, LocalDate date,LocalTime startTime, LocalTime endTime, String description, int numPlaces, User creator) {
        this.name = name;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.description = description;
        this.numPlaces = numPlaces;
        this.creator = creator;
        this.tags = new ArrayList<>();
        this.participants = new ArrayList<>();
        this.waitingList = new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Activity activity = (Activity) o;
        return numPlaces == activity.numPlaces && ageLimit == activity.ageLimit && Objects.equals(id, activity.id) &&
                Objects.equals(name, activity.name) && Objects.equals(date, activity.date) && Objects.equals(description,
                activity.description) && Objects.equals(startTime, activity.startTime) && Objects.equals(endTime, activity.endTime)
                && Objects.equals(address, activity.address) && Objects.equals(creator, activity.creator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, date, description, numPlaces, ageLimit, startTime, endTime, address, creator);
    }

    @Override
    public String toString() {
        return "Activity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", date=" + date +
                ", description='" + description + '\'' +
                ", numPlaces=" + numPlaces +
                ", address=" + address +
                '}';
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getNumPlaces() {
        return numPlaces;
    }

    public void setNumPlaces(int numPlaces) {
        this.numPlaces = numPlaces;
    }

    public int getAgeLimit() {
        return ageLimit;
    }

    public void setAgeLimit(int ageLimit) {
        this.ageLimit = ageLimit;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public void addTag(Tag tag) {
        this.tags.add(tag);
    }

    public void removeTag(Tag tag) {
        this.tags.remove(tag);
    }


    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public List<User> getParticipants() {
        return participants;
    }

    public void setParticipants(List<User> participants) {
        this.participants = participants;
    }

    public void deleteParticipant(User participant) {
        this.participants.remove(participant);
    }


    public Pair<HttpStatus,String> addParticipant(User participant) {
        if(this.participants.size() < this.numPlaces) {
            this.participants.add(participant);
        }
        else {
            this.waitingList.add(participant);
            return Pair.of(HttpStatus.OK,"PARTICIPANT " + participant.getUsername().toUpperCase() + " ADDED TO WAITING LIST");
        }
        return Pair.of(HttpStatus.OK,"NEW PARTICIPANT " + participant.getUsername().toUpperCase() + " ADDED");
    }

    public Pair<HttpStatus,String> freePlace(){
        if(this.participants.size() < this.numPlaces) {
            if (!this.waitingList.isEmpty()) {
                User randomParticipant = this.waitingList.remove(random.nextInt(this.waitingList.size()));
                this.participants.add(randomParticipant);
            }
        }else {
            return Pair.of(HttpStatus.CONFLICT,"NO AVAILABLE PLACES");
        }
        return Pair.of(HttpStatus.OK,"NEW PARTICIPANTS ADDED");
    }

    public List<User> getWaitingList() {
        return this.waitingList;
    }

    public void removeParticipant(User participant) {
        this.participants.remove(participant);
    }

}
