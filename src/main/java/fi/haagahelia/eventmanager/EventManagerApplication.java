package fi.haagahelia.eventmanager;

import fi.haagahelia.eventmanager.domain.*;
import fi.haagahelia.eventmanager.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalTime;

@SpringBootApplication
@RestController
public class EventManagerApplication {
    private static final Logger log = LoggerFactory.getLogger(EventManagerApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(EventManagerApplication.class, args);
    }

    @Bean
    public CommandLineRunner eventTranscation(RoleRepository roleRepository)
    {
        return (args) -> {

            //create all statuses
            Role status1 = new Role("ROLE_ADMIN");
            Role status2 = new Role("ROLE_USER");

            //save statuses in the database
            roleRepository.save(status1);
            roleRepository.save(status2);
            log.info("---------------------------------------------------------Status saved---------------------------------------------------------");
        };
    }

}
