package fi.haagahelia.eventmanager.repository;


import fi.haagahelia.eventmanager.domain.Country;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CountryRepository extends JpaRepository<Country, Long> {
    Country findCountryById(Long id);
    Country findCountryByName(String name);
    Boolean existsByName(String name);
}
