package com.example.CSIA.repository;

import com.example.CSIA.entity.MedicationReminder;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface MedicationReminderRepository extends CrudRepository<MedicationReminder, UUID> {
}
