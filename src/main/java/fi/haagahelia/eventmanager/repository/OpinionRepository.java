package fi.haagahelia.eventmanager.repository;

import fi.haagahelia.eventmanager.domain.opinion.Opinion;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

public interface OpinionRepository extends JpaRepository<Opinion, Long>
{
   Opinion findOpinionByActivity_IdAndUser_Id(Long activityId, Long userId);
   List<Opinion> findOpinionsByActivity_Id(Long activityId);
   List<Opinion> findAll();

   
}
