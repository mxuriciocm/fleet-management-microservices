package com.shipments.service.application.internal.queryservices;

import com.shipments.service.domain.model.aggregates.Shipment;
import com.shipments.service.domain.model.queries.GetShipmentByIdQuery;
import com.shipments.service.domain.model.queries.GetShipmentsByCarrierIdQuery;
import com.shipments.service.domain.model.queries.GetShipmentsByManagerIdQuery;
import com.shipments.service.domain.model.queries.GetShipmentsByStatusQuery;
import com.shipments.service.domain.services.ShipmentQueryService;
import com.shipments.service.infrastructure.persistence.jpa.repositories.ShipmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ShipmentQueryServiceImpl implements ShipmentQueryService {
    private final ShipmentRepository shipmentRepository;

    public ShipmentQueryServiceImpl(ShipmentRepository shipmentRepository) {
        this.shipmentRepository = shipmentRepository;
    }

    /**
     * Handle Get Shipment By id Query
     * @param query The {@link GetShipmentByIdQuery} Query
     * @return An {@link Optional<Shipment>} instance if the shipment was found, otherwise empty
     */
    @Override
    public Optional<Shipment> handle(GetShipmentByIdQuery query) {
        return shipmentRepository.findById(query.shipmentId());
    }

    /**
     * Handle Get Shipments By Status Query
     * @param query The {@link GetShipmentsByStatusQuery} Query
     * @return A list of shipments with the specified status
     */
    @Override
    public List<Shipment> handle(GetShipmentsByStatusQuery query) {
        return shipmentRepository.findByStatus(query.status());
    }

    /**
     * Handle Get Shipments By Manager id Query
     * @param query The {@link GetShipmentsByManagerIdQuery} Query
     * @return A list of shipments managed by the specified manager
     */
    @Override
    public List<Shipment> handle(GetShipmentsByManagerIdQuery query) {
        return shipmentRepository.findByManagerId(query.managerId());
    }

    /**
     * Handle Get Shipments By Carrier id Query
     * @param query The {@link GetShipmentsByCarrierIdQuery} Query
     * @return A list of shipments assigned to the specified carrier
     */
    @Override
    public List<Shipment> handle(GetShipmentsByCarrierIdQuery query) {
        return shipmentRepository.findByCarrierId(query.carrierId());
    }

}
