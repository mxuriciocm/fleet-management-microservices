package com.issues.service.application.internal.queryservices;

import com.issues.service.domain.model.aggregates.Issue;
import com.issues.service.domain.model.queries.*;
import com.issues.service.domain.services.IssueQueryService;
import com.issues.service.infrastructure.persistence.jpa.repositories.IssueRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class IssueQueryServiceImpl implements IssueQueryService {
    private final IssueRepository issueRepository;

    public IssueQueryServiceImpl(IssueRepository issueRepository) {
        this.issueRepository = issueRepository;
    }

    @Override
    public Optional<Issue> handle(GetIssueByIdQuery query) {
        return issueRepository.findById(query.reportId());
    }

    @Override
    public List<Issue> handle(GetIssuesByCarrierIdQuery query) {
        return issueRepository.findByCarrierId(query.carrierId());
    }

    @Override
    public List<Issue> handle(GetIssuesByManagerIdQuery query) {
        return issueRepository.findByManagerId(query.managerId());
    }

    @Override
    public List<Issue> handle(GetIssuesByTypeQuery query) {
        return issueRepository.findByType(query.type());
    }

    @Override
    public List<Issue> handle(GetIssuesByVehicleIdQuery query) {
        return issueRepository.findByVehicleId(query.vehicleId());
    }

    @Override
    public List<Issue> handle(GetIssuesByShipmentIdQuery query) {
        return issueRepository.findByShipmentId(query.shipmentId());
    }
}
