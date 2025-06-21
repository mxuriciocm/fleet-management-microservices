package com.issues.service.domain.model.aggregates;

import com.issues.service.domain.model.commands.CreateIssueCommand;
import com.issues.service.domain.model.valueobjects.IssueType;
import com.issues.service.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class Issue extends AuditableAbstractAggregateRoot<Issue> {

    @NotBlank
    @Size(max = 255)
    private String title;

    @NotBlank
    @Size(max = 2000)
    @Column(length=2000)
    private String content;

    @NotNull
    @Enumerated(EnumType.STRING)
    private IssueType type;

    @NotNull
    private LocalDateTime reportDate;

    @NotNull
    private Long carrierId;

    private Long managerId;

    private Long vehicleId;

    private Long shipmentId;

    public Issue(String title, String content, IssueType type, LocalDateTime reportDate, Long carrierId, Long managerId, Long vehicleId, Long shipmentId) {
        this.title = title;
        this.content = content;
        this.type = type;
        this.reportDate = reportDate;
        this.carrierId = carrierId;
        this.managerId = managerId;
        this.vehicleId = vehicleId;
        this.shipmentId = shipmentId;
    }

    public Issue() {}

    public Issue(CreateIssueCommand command) {
        this.title = command.title();
        this.content = command.content();
        this.type = command.type();
        this.reportDate = command.reportDate() != null ? command.reportDate() : LocalDateTime.now();
        this.carrierId = command.carrierId();
        this.managerId = command.managerId();
        this.vehicleId = command.vehicleId();
        this.shipmentId = command.shipmentId();
    }

    public Issue updateContent(String newContent) {
        this.content = newContent;
        return this;
    }

    public Issue updateTitle(String newTitle) {
        this.title = newTitle;
        return this;
    }

    public Issue updateType(IssueType newType) {
        this.type = newType;
        return this;
    }
}
