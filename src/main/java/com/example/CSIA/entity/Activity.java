package com.example.CSIA.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.lang.NonNull;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "calendar")
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(allowGetters = true, ignoreUnknown = true)
public class Activity {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @NonNull
    @JsonProperty(value = "userID")
    private UUID userId;

    @NonNull
    @JsonProperty(value = "respondingUserID")
    private UUID respondingUserId;

    @NonNull
    @JsonProperty(value = "startTime")
    private LocalDateTime startTime;

    @NonNull
    @JsonProperty(value = "endTime")
    private LocalDateTime endTime;

    private String activityName;

    public UUID getActivityId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setStartTime(@NonNull LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(@NonNull LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public UUID getRespondingUserId() {
        return respondingUserId;
    }

    public void setRespondingUserId(UUID respondingUserId) {
        this.respondingUserId = respondingUserId;
    }
}



