package fi.haagahelia.eventmanager.repository;

import fi.haagahelia.eventmanager.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Boolean existsByName(String name);
    List<Tag> findAll();
    Tag findTagById(Long id);
    Tag findTagByName(String name);

}
