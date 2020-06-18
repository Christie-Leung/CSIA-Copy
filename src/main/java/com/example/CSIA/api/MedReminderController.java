package com.example.CSIA.api;

import com.example.CSIA.entity.MedicationReminder;
import com.example.CSIA.repository.MedicationReminderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("api/med")
public class MedReminderController {

    MedicationReminderRepository mrr;

    @Autowired
    public MedReminderController(MedicationReminderRepository medicationReminderRepository) {
        this.mrr = medicationReminderRepository;
    }

    @PostMapping("/{id}")
    public ResponseEntity<?> insertMedReminder(@PathVariable("id") UUID id, @RequestBody @NonNull @Valid MedicationReminder medicationReminder) {
        medicationReminder.setId(id);
        mrr.save(medicationReminder);
        return ResponseEntity.ok("Success!");
    }

    @GetMapping
    public Iterable<MedicationReminder> getAllMedReminder() {
        return mrr.findAll();
    }

    @GetMapping("/{id}")
    public Optional<MedicationReminder> getMedReminderByID(@PathVariable("id") UUID id) {
        return mrr.findById(id);
    }

    @DeleteMapping("/{id}/{med}")
    public int deleteMedReminderByMedName(@PathVariable("id") UUID id, @PathVariable("med") String medName) {
        mrr.findById(id).filter(MedicationReminder ->
                MedicationReminder.getMedication().equals(medName)).map(MedicationReminder -> {
            mrr.delete(MedicationReminder);
            return true;
        });
        return 1;
    }

    @DeleteMapping("/{id}")
    public int deleteMedReminderByUserID(@PathVariable("id") UUID id) {
        mrr.deleteById(id);
        return 1;
    }

    @PutMapping("/{id}/{med}")
    public MedicationReminder updateMedReminderByMedName(@PathVariable("id") UUID id, @PathVariable("med") String medName,
                                          @RequestBody @Valid @NonNull MedicationReminder mr) {
        Optional<MedicationReminder> tempMr = mrr.findById(id).filter(MedicationReminder ->
                MedicationReminder.getMedication().equals(mr.getMedication()));
        if (tempMr.isPresent()) {
            MedicationReminder newMr = tempMr.get();
            newMr.setMedStartTime(mr.getMedStartTime());
            newMr.setMedEndTime(mr.getMedEndTime());
            newMr.setMedication(mr.getMedication());
            newMr.setMedInterval(mr.getMedInterval());
            return mrr.save(newMr);
        }
        return null;
    }

}