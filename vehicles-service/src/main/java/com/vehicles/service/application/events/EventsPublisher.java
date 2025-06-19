package com.vehicles.service.application.events;

import com.vehicles.service.domain.model.events.VehicleCreatedEvent;
import com.vehicles.service.domain.model.events.VehicleUpdatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

/**
 * Publicador de eventos para el servicio de vehículos
 */
@Component
public class EventsPublisher {

    private static final Logger log = LoggerFactory.getLogger(EventsPublisher.class);
    private final StreamBridge streamBridge;

    public EventsPublisher(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    /**
     * Publica un evento cuando se crea un vehículo
     * @param event El VehicleCreatedEvent a publicar
     * @return true si el evento fue publicado exitosamente, false en caso contrario
     */
    public boolean publishVehicleCreatedEvent(VehicleCreatedEvent event) {
        try {
            log.info("Publicando VehicleCreatedEvent para vehicleId: {}", event.vehicleId());
            boolean result = streamBridge.send("vehicle-events", event);
            if (result) {
                log.info("VehicleCreatedEvent publicado exitosamente para vehicleId: {}", event.vehicleId());
            } else {
                log.warn("Falló la publicación de VehicleCreatedEvent para vehicleId: {}", event.vehicleId());
            }
            return result;
        } catch (Exception e) {
            log.error("Error al publicar VehicleCreatedEvent para vehicleId: {}", event.vehicleId(), e);
            return false;
        }
    }

    /**
     * Publica un evento cuando un vehículo es actualizado
     * @param event El VehicleUpdatedEvent a publicar
     * @return true si el evento fue publicado exitosamente, false en caso contrario
     */
    public boolean publishVehicleUpdatedEvent(VehicleUpdatedEvent event) {
        try {
            log.info("Publicando VehicleUpdatedEvent para vehicleId: {}", event.vehicleId());
            boolean result = streamBridge.send("vehicle-update-events", event);
            if (result) {
                log.info("VehicleUpdatedEvent publicado exitosamente para vehicleId: {}", event.vehicleId());
            } else {
                log.warn("Falló la publicación de VehicleUpdatedEvent para vehicleId: {}", event.vehicleId());
            }
            return result;
        } catch (Exception e) {
            log.error("Error al publicar VehicleUpdatedEvent para vehicleId: {}", event.vehicleId(), e);
            return false;
        }
    }
}
