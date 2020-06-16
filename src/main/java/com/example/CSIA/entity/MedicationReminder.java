package com.example.CSIA.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.lang.NonNull;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "MedicationReminder")
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(allowGetters = true, ignoreUnknown = true)
public class MedicationReminder {

    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @NonNull
    private LocalDateTime medStartTime;

    @NonNull
    private LocalDateTime medEndTime;

    @NonNull
    private LocalTime medInterval;

    @NotBlank
    private String medication;


    @NonNull
    public UUID getUuid() {
        return id;
    }

    @NonNull
    public LocalDateTime getMedStartTime() {
        return medStartTime;
    }

    @NonNull
    public LocalDateTime getMedEndTime() {
        return medEndTime;
    }

    @NonNull
    public LocalTime getMedInterval() {
        return medInterval;
    }

    public String getMedication() {
        return medication;
    }

    public void setMedStartTime(@NonNull LocalDateTime medStartTime) {
        this.medStartTime = medStartTime;
    }

    public void setMedEndTime(@NonNull LocalDateTime medEndTime) {
        this.medEndTime = medEndTime;
    }

    public void setMedInterval(@NonNull LocalTime medInterval) {
        this.medInterval = medInterval;
    }

    public void setMedication(String medication) {
        this.medication = medication;
    }
}
