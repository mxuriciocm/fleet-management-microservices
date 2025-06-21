package com.issues.service.application.events;

import com.issues.service.domain.model.events.IssueCreatedEvent;
import com.issues.service.domain.model.events.IssueUpdatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

/**
 * Publicador de eventos para el servicio de incidencias
 */
@Component
public class EventsPublisher {

    private static final Logger log = LoggerFactory.getLogger(EventsPublisher.class);
    private final StreamBridge streamBridge;

    public EventsPublisher(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    /**
     * Publica un evento cuando se crea una incidencia
     * @param event El IssueCreatedEvent a publicar
     * @return true si el evento fue publicado exitosamente, false en caso contrario
     */
    public boolean publishIssueCreatedEvent(IssueCreatedEvent event) {
        try {
            log.info("Publicando IssueCreatedEvent para issueId: {}", event.issueId());
            boolean result = streamBridge.send("issueCreatedEvent-out-0", event);
            if (result) {
                log.info("IssueCreatedEvent publicado exitosamente para issueId: {}", event.issueId());
            } else {
                log.warn("Falló la publicación de IssueCreatedEvent para issueId: {}", event.issueId());
            }
            return result;
        } catch (Exception e) {
            log.error("Error al publicar IssueCreatedEvent para issueId: {}", event.issueId(), e);
            return false;
        }
    }

    /**
     * Publica un evento cuando una incidencia es actualizada
     * @param event El IssueUpdatedEvent a publicar
     * @return true si el evento fue publicado exitosamente, false en caso contrario
     */
    public boolean publishIssueUpdatedEvent(IssueUpdatedEvent event) {
        try {
            log.info("Publicando IssueUpdatedEvent para issueId: {}", event.issueId());
            boolean result = streamBridge.send("issueUpdatedEvent-out-0", event);
            if (result) {
                log.info("IssueUpdatedEvent publicado exitosamente para issueId: {}", event.issueId());
            } else {
                log.warn("Falló la publicación de IssueUpdatedEvent para issueId: {}", event.issueId());
            }
            return result;
        } catch (Exception e) {
            log.error("Error al publicar IssueUpdatedEvent para issueId: {}", event.issueId(), e);
            return false;
        }
    }
}
