package br.com.viniciusmarlin.internetping.model;

import jakarta.persistence.*;
import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "network_incidents")
public class NetworkIncident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private Long durationSeconds;

    @Enumerated(EnumType.STRING)
    private IncidentStatus status;

    private String salesforceCaseId;

    public NetworkIncident() {}

    public NetworkIncident(LocalDateTime startedAt) {
        this.startedAt = startedAt;
        this.status = IncidentStatus.ONGOING;
    }

    public void closeIncident(LocalDateTime endedAt) {
        this.endedAt = endedAt;
        this.durationSeconds = Duration.between(this.startedAt, endedAt).getSeconds();
        this.status = IncidentStatus.PENDING_SYNC;
    }

    public void markAsSynced(String salesforceCaseId) {
        this.salesforceCaseId = salesforceCaseId;
        this.status = IncidentStatus.SYNCED;
    }

    // Getters
    public Long getId() { return id; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public LocalDateTime getEndedAt() { return endedAt; }
    public Long getDurationSeconds() { return durationSeconds; }
    public IncidentStatus getStatus() { return status; }
    public String getSalesforceCaseId() { return salesforceCaseId; }
} 
