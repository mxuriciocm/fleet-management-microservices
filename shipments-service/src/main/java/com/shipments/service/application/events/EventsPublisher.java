package com.shipments.service.application.events;

import com.shipments.service.domain.model.events.ShipmentCreatedEvent;
import com.shipments.service.domain.model.events.ShipmentUpdatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

/**
 * Publicador de eventos para el servicio de envíos
 */
@Component
public class EventsPublisher {

    private static final Logger log = LoggerFactory.getLogger(EventsPublisher.class);
    private final StreamBridge streamBridge;

    public EventsPublisher(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    /**
     * Publica un evento cuando se crea un envío
     * @param event El ShipmentCreatedEvent a publicar
     * @return true si el evento fue publicado exitosamente, false en caso contrario
     */
    public boolean publishShipmentCreatedEvent(ShipmentCreatedEvent event) {
        try {
            log.info("Publicando ShipmentCreatedEvent para shipmentId: {}", event.shipmentId());
            boolean result = streamBridge.send("shipment-events", event);
            if (result) {
                log.info("ShipmentCreatedEvent publicado exitosamente para shipmentId: {}", event.shipmentId());
            } else {
                log.warn("Falló la publicación de ShipmentCreatedEvent para shipmentId: {}", event.shipmentId());
            }
            return result;
        } catch (Exception e) {
            log.error("Error al publicar ShipmentCreatedEvent para shipmentId: {}", event.shipmentId(), e);
            return false;
        }
    }

    /**
     * Publica un evento cuando un envío es actualizado
     * @param event El ShipmentUpdatedEvent a publicar
     * @return true si el evento fue publicado exitosamente, false en caso contrario
     */
    public boolean publishShipmentUpdatedEvent(ShipmentUpdatedEvent event) {
        try {
            log.info("Publicando ShipmentUpdatedEvent para shipmentId: {}", event.shipmentId());
            boolean result = streamBridge.send("shipment-update-events", event);
            if (result) {
                log.info("ShipmentUpdatedEvent publicado exitosamente para shipmentId: {}", event.shipmentId());
            } else {
                log.warn("Falló la publicación de ShipmentUpdatedEvent para shipmentId: {}", event.shipmentId());
            }
            return result;
        } catch (Exception e) {
            log.error("Error al publicar ShipmentUpdatedEvent para shipmentId: {}", event.shipmentId(), e);
            return false;
        }
    }
}
