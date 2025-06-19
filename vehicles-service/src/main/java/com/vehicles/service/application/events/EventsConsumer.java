package com.vehicles.service.application.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

/**
 * Consumers para eventos de otros microservicios
 * (Como usamos el gateway para la autenticación/autorización,
 * sólo necesitamos escuchar eventos específicos para lógica de negocio)
 */
@Component
public class EventsConsumer {

    private static final Logger log = LoggerFactory.getLogger(EventsConsumer.class);

    /**
     * Consume el evento UserCreatedEvent del servicio IAM
     * Esto podría usarse para lógica de negocio específica, como enviar notificaciones
     * o realizar acciones cuando se crea un nuevo usuario.
     *
     * @return Consumer que procesa UserCreatedEvent
     */
    @Bean
    public Consumer<UserCreatedEvent> userCreatedEvent() {
        return event -> {
            if (event == null) {
                log.error("Recibido UserCreatedEvent nulo");
                return;
            }

            log.info("Recibido evento UserCreatedEvent para userId: {}, email: {}",
                     event.userId(), event.email());

            // Aquí puedes implementar lógica de negocio específica si es necesaria
            // Por ejemplo, notificar a administradores, generar estadísticas, etc.
        };
    }

    /**
     * Consume el evento ProfileCreatedEvent del servicio de Profiles
     *
     * @return Consumer que procesa ProfileCreatedEvent
     */
    @Bean
    public Consumer<ProfileCreatedEvent> profileCreatedEvent() {
        return event -> {
            if (event == null) {
                log.error("Recibido ProfileCreatedEvent nulo");
                return;
            }

            log.info("Recibido evento ProfileCreatedEvent para userId: {}", event.userId());

            // Aquí puedes implementar lógica de negocio específica si es necesaria
        };
    }
}
