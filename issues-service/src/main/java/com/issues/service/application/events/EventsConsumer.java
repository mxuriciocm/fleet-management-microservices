package com.issues.service.application.events;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Component
public class EventsConsumer {

    private final Map<Long, Long> vehicleManagerMap = new ConcurrentHashMap<>();
    private final Map<Long, Long> carrierVehicleMap = new ConcurrentHashMap<>();
    private final Map<Long, Long> carrierManagerMap = new ConcurrentHashMap<>();

    public Long getManagerForCarrier(Long carrierId) {
        if (carrierId == null) return null;

        Long vehicleId = carrierVehicleMap.get(carrierId);
        if (vehicleId != null) {
            Long managerId = vehicleManagerMap.get(vehicleId);
            if (managerId != null) {
                return managerId;
            }
        }

        return carrierManagerMap.get(carrierId);
    }

    public Long getVehicleIdForCarrier(Long carrierId) {
        if (carrierId == null) return null;
        return carrierVehicleMap.get(carrierId);
    }

    public boolean hasVehicleAssigned(Long carrierId) {
        return carrierVehicleMap.containsKey(carrierId);
    }

    @Bean
    public Consumer<UserCreatedEvent> userCreatedEvent() {
        return event -> {
            // Event processing logic can be added here if needed
        };
    }

    @Bean
    public Consumer<UserUpdatedEvent> userUpdatedEvent() {
        return event -> {
            // Event processing logic can be added here if needed
        };
    }

    @Bean
    public Consumer<VehicleCreatedEvent> vehicleCreatedEvent() {
        return event -> {
            if (event != null) {
                vehicleManagerMap.put(event.vehicleId(), event.managerId());
            }
        };
    }

    @Bean
    public Consumer<VehicleUpdatedEvent> vehicleUpdatedEvent() {
        return event -> {
            if (event == null) return;

            Long vehicleId = event.vehicleId();
            Long carrierId = event.carrierId();

            if (carrierId == null) {
                for (Map.Entry<Long, Long> entry : carrierVehicleMap.entrySet()) {
                    if (entry.getValue().equals(vehicleId)) {
                        Long previousCarrierId = entry.getKey();
                        carrierVehicleMap.remove(previousCarrierId);

                        Long managerId = vehicleManagerMap.get(vehicleId);
                        if (managerId != null) {
                            carrierManagerMap.put(previousCarrierId, managerId);
                        }
                        break;
                    }
                }
            } else {
                carrierVehicleMap.put(carrierId, vehicleId);

                Long managerId = vehicleManagerMap.get(vehicleId);
                if (managerId != null) {
                    carrierManagerMap.put(carrierId, managerId);
                }
            }
        };
    }
}
