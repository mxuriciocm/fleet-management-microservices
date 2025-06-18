package com.profiles.service.application.events;

import com.profiles.service.domain.model.commands.CreateProfileCommand;
import com.profiles.service.domain.services.ProfileCommandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

/**
 * Consumer for events from IAM service
 */
@Component
public class UserEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(UserEventConsumer.class);
    private final ProfileCommandService profileCommandService;

    public UserEventConsumer(ProfileCommandService profileCommandService) {
        this.profileCommandService = profileCommandService;
    }

    /**
     * Consumes UserCreatedEvent from IAM service and creates a profile
     * @return Consumer function that processes UserCreatedEvent
     */
    @Bean
    public Consumer<UserCreatedEvent> userCreated() {
        return event -> {
            if (event == null) {
                log.error("Received null UserCreatedEvent");
                return;
            }

            log.info("Received UserCreatedEvent for userId: {}", event.userId());
            try {
                // Validación básica de datos
                if (event.userId() == null) {
                    log.error("Invalid UserCreatedEvent: userId is null");
                    return;
                }

                var command = new CreateProfileCommand(
                        event.userId(),
                        null,
                        null,
                        null
                );
                var profile = profileCommandService.handle(command);
                if (profile.isPresent()) {
                    log.info("Profile created successfully for userId: {}", event.userId());
                } else {
                    log.warn("Profile creation returned empty result for userId: {}", event.userId());
                }
            } catch (Exception e) {
                log.error("Failed to create profile for userId: {}", event.userId(), e);
            }
        };
    }
}
