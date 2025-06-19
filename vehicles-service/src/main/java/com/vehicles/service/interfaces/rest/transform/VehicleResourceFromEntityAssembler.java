package com.vehicles.service.interfaces.rest.transform;

import com.vehicles.service.domain.model.aggregates.Vehicle;
import com.vehicles.service.interfaces.rest.resources.VehicleResource;

import java.util.List;
import java.util.stream.Collectors;

public class VehicleResourceFromEntityAssembler {
    public static VehicleResource toResourceFromEntity(Vehicle entity){
        return new VehicleResource(entity.getId(), entity.getLicensePlate(), entity.getBrand(), entity.getModel(), entity.getStatus(), entity.getCarrierId(), entity.getManagerId());
    }

    public static List<VehicleResource> toResourceFromEntities(List<Vehicle> entities) {
        return entities.stream()
                .map(VehicleResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());
    }
}
