package com.issues.service.application.internal.commandservices;

import com.issues.service.application.events.EventsPublisher;
import com.issues.service.domain.model.aggregates.Issue;
import com.issues.service.domain.model.commands.CreateIssueCommand;
import com.issues.service.domain.model.commands.UpdateIssueCommand;
import com.issues.service.domain.model.events.IssueCreatedEvent;
import com.issues.service.domain.model.events.IssueUpdatedEvent;
import com.issues.service.domain.services.IssueCommandService;
import com.issues.service.infrastructure.persistence.jpa.repositories.IssueRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class IssueCommandServiceImpl implements IssueCommandService {
    private final IssueRepository issueRepository;
    private final EventsPublisher eventsPublisher;

    public IssueCommandServiceImpl(IssueRepository issueRepository, EventsPublisher eventsPublisher) {
        this.issueRepository = issueRepository;
        this.eventsPublisher = eventsPublisher;
    }

    @Override
    public Issue handle(CreateIssueCommand command) {
        var issue = new Issue(command);
        var savedIssue = issueRepository.save(issue);

        // Publicar evento de incidencia creada
        var event = new IssueCreatedEvent(
            savedIssue.getId(),
            savedIssue.getTitle(),
            savedIssue.getContent(),
            savedIssue.getType(),
            savedIssue.getReportDate(),
            savedIssue.getCarrierId(),
            savedIssue.getManagerId(),
            savedIssue.getVehicleId(),
            savedIssue.getShipmentId()
        );
        eventsPublisher.publishIssueCreatedEvent(event);

        return savedIssue;
    }

    @Override
    public Optional<Issue> handle(Long issueId, UpdateIssueCommand command) {
        return issueRepository.findById(issueId)
                .map(issue -> {
                    if (command.title() != null) { issue.updateTitle(command.title()); }
                    if (command.content() != null) { issue.updateContent(command.content()); }
                    if (command.type() != null) { issue.updateType(command.type()); }
                    var updatedIssue = issueRepository.save(issue);

                    // Publicar evento de incidencia actualizada
                    var event = new IssueUpdatedEvent(
                        updatedIssue.getId(),
                        command.title(),
                        command.content(),
                        command.type()
                    );
                    eventsPublisher.publishIssueUpdatedEvent(event);

                    return updatedIssue;
                });
    }

    @Override
    public boolean deleteIssue(Long issueId){
        if (!issueRepository.existsById(issueId)) { return false; }
        try {
            issueRepository.deleteById(issueId);
            return true;
        } catch (Exception e) {
            // Log the exception if necessary
            return false;
        }
    }
}
