package fi.haagahelia.eventmanager.repository;

import fi.haagahelia.eventmanager.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
    Activity findActivityById(Long id);
    Activity findActivityByName(String name);
    List<Activity> findAllByName(String name);
    List<Activity> findAll();
    List<Activity> findAllByAddress(Address address);
    List<Activity> findByDate(LocalDate date);
    List<Activity> findAllByCreator(Optional<User> creator);
    Boolean existsActivityByNameAndDateAndStartTimeAndEndTime(String name, LocalDate date, LocalTime startTime, LocalTime endTime);
    @Query(value = "SELECT a FROM Activity a WHERE a.name LIKE %:name%")
    List<Activity> searchActivitiesByName(@Param("name") String name);
    @Query(value = "SELECT a FROM Activity a WHERE a.date = :date")
    List<Activity> searchActivitiesByDate(@Param("date") LocalDate date);
    @Query(value = "SELECT a FROM Activity a WHERE a.address.city = :city and a.address.country = :country")
    List<Activity> searchActivitiesByCity(@Param("city") String city, @Param("country") Country country);
    @Query(value = "SELECT a FROM Activity a JOIN a.tags t WHERE t.name LIKE %:tag%")
    List<Activity> searchActivitiesByTag(@Param("tag") String tag);

    @Query(value = "SELECT a FROM Activity a JOIN a.tags t WHERE t.id = :tagId")
    List<Activity> findAllByTag(Long tagId);

}
