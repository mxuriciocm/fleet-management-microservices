package com.issues.service.application.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Consumers para eventos de otros microservicios
 */
@Component
public class EventsConsumer {

    private static final Logger log = LoggerFactory.getLogger(EventsConsumer.class);

    // Mapas en memoria para mantener las relaciones necesarias
    private final Map<Long, Long> vehicleManagerMap = new ConcurrentHashMap<>(); // vehicleId -> managerId
    private final Map<Long, Long> carrierVehicleMap = new ConcurrentHashMap<>(); // carrierId -> vehicleId

    /**
     * Obtiene el managerId para un carrierId específico
     * @param carrierId El ID del carrier
     * @return El ID del manager que gestiona a este carrier, o null si no se encuentra
     */
    public Long getManagerForCarrier(Long carrierId) {
        if (carrierId == null) return null;

        Long vehicleId = carrierVehicleMap.get(carrierId);
        if (vehicleId == null) {
            log.debug("No se encontró vehículo asociado al carrier {}", carrierId);
            return null;
        }

        Long managerId = vehicleManagerMap.get(vehicleId);
        if (managerId == null) {
            log.debug("No se encontró manager asociado al vehículo {} del carrier {}", vehicleId, carrierId);
            return null;
        }

        return managerId;
    }

    /**
     * Obtiene el vehicleId para un carrierId específico
     * @param carrierId El ID del carrier
     * @return El ID del vehículo asignado a este carrier, o null si no se encuentra
     */
    public Long getVehicleIdForCarrier(Long carrierId) {
        if (carrierId == null) return null;

        Long vehicleId = carrierVehicleMap.get(carrierId);
        if (vehicleId == null) {
            log.debug("No se encontró vehículo asociado al carrier {}", carrierId);
            return null;
        }

        return vehicleId;
    }

    /**
     * Consume el evento UserCreatedEvent del servicio IAM
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
        };
    }

    /**
     * Consume el evento UserUpdatedEvent del servicio IAM
     *
     * @return Consumer que procesa UserUpdatedEvent
     */
    @Bean
    public Consumer<UserUpdatedEvent> userUpdatedEvent() {
        return event -> {
            if (event == null) {
                log.error("Recibido UserUpdatedEvent nulo");
                return;
            }

            log.info("Recibido evento UserUpdatedEvent para userId: {}", event.userId());
        };
    }

    /**
     * Consume el evento VehicleCreatedEvent del servicio Vehicles
     * Almacena la relación manager->vehículo en memoria
     *
     * @return Consumer que procesa VehicleCreatedEvent
     */
    @Bean
    public Consumer<VehicleCreatedEvent> vehicleCreatedEvent() {
        return event -> {
            if (event == null) {
                log.error("Recibido VehicleCreatedEvent nulo");
                return;
            }

            log.info("Recibido evento VehicleCreatedEvent para vehicleId: {}, managerId: {}",
                     event.vehicleId(), event.managerId());

            // Almacenar la relación vehículo -> manager en memoria
            vehicleManagerMap.put(event.vehicleId(), event.managerId());
            log.debug("Almacenada relación vehículo {} -> manager {}", event.vehicleId(), event.managerId());
        };
    }

    /**
     * Consume el evento VehicleUpdatedEvent del servicio Vehicles
     * Actualiza las relaciones carrier<->vehículo y vehículo->manager en memoria
     *
     * @return Consumer que procesa VehicleUpdatedEvent
     */
    @Bean
    public Consumer<VehicleUpdatedEvent> vehicleUpdatedEvent() {
        return event -> {
            if (event == null) {
                log.error("Recibido VehicleUpdatedEvent nulo");
                return;
            }

            log.info("Recibido evento VehicleUpdatedEvent para vehicleId: {}", event.vehicleId());

            Long vehicleId = event.vehicleId();
            Long carrierId = event.carrierId();

            if (carrierId == null) {
                // Si el carrier fue eliminado, buscar el carrier anterior y eliminar su relación
                for (Map.Entry<Long, Long> entry : carrierVehicleMap.entrySet()) {
                    if (entry.getValue().equals(vehicleId)) {
                        carrierVehicleMap.remove(entry.getKey());
                        log.info("Eliminada relación carrier {} -> vehículo {}", entry.getKey(), vehicleId);
                        break;
                    }
                }
            } else {
                // Actualizar la relación carrier -> vehículo
                carrierVehicleMap.put(carrierId, vehicleId);
                log.info("Actualizada relación carrier {} -> vehículo {}", carrierId, vehicleId);

                // Verificar si tenemos el managerId para este vehículo
                Long managerId = vehicleManagerMap.get(vehicleId);
                if (managerId != null) {
                    log.debug("Carrier {} está relacionado con manager {} a través del vehículo {}",
                            carrierId, managerId, vehicleId);
                } else {
                    log.warn("No se encontró manager para el vehículo {} al asignar el carrier {}",
                            vehicleId, carrierId);
                }
            }
        };
    }
}
