package com.shipments.service.domain.model.queries;

import com.shipments.service.domain.model.valueobjects.ShipmentStatus;

public record GetShipmentsByStatusQuery(ShipmentStatus status) {}
