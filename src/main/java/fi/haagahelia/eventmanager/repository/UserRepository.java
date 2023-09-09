package fi.haagahelia.eventmanager.repository;

import fi.haagahelia.eventmanager.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
    Optional<User> findByUsername(String username);
    User findByEmail(String email);

    List<User> findAll();
    User findUserById(Long id);
}
