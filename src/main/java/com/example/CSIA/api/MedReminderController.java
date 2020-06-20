package com.example.CSIA.api;

import com.example.CSIA.entity.MedicationReminder;
import com.example.CSIA.repository.MedicationReminderRepository;
import com.example.CSIA.repository.UserRepository;
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
    UserRepository userRepository;

    @Autowired
    public MedReminderController(MedicationReminderRepository medicationReminderRepository, UserRepository userRepository) {
        this.mrr = medicationReminderRepository;
        this.userRepository = userRepository;
    }

    /**
     * This method adds a medication reminder by user ID into the med database
     * @param id Valid user ID from the user database
     * @param medicationReminder Valid MedicationReminder entity parsed through a JSON file
     * @return Success message
     */
    @PostMapping("/{id}")
    public ResponseEntity<?> insertMedReminder(@PathVariable("id") UUID id, @RequestBody @NonNull @Valid MedicationReminder medicationReminder) {
        if (userRepository.findById(id).isPresent()) {
            medicationReminder.setId(id);
            mrr.save(medicationReminder);
            return ResponseEntity.ok("Success!");
        }
        return ResponseEntity.badRequest().body("Error! User ID is invalid!");
    }

    /**
     * This method gets all medication reminders from the med database
     * @return Iterable list of all medication reminders
     */
    @GetMapping
    public ResponseEntity<?> getAllMedReminder() {
        return ResponseEntity.ok(mrr.findAll());
    }

    /**
     * This method gets a specified user's medication reminder
     * @param id Valid user ID from the user database
     * @return Medication reminders for specified user
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getMedReminderByID(@PathVariable("id") UUID id) {
        if (mrr.findById(id).isPresent()) {
            return ResponseEntity.ok(mrr.findById(id));
        }
        return ResponseEntity.badRequest().body("Error! There are no medication reminders for this user!");
    }

    /**
     * This method deletes a medication reminder by user ID and medication name
     * @param id Valid user ID from user database
     * @param medName Medication name
     * @return Success or error message
     */
    @DeleteMapping("/{id}/{med}")
    public ResponseEntity<?> deleteMedReminderByMedName(@PathVariable("id") UUID id, @PathVariable("med") String medName) {
        mrr.findById(id).filter(MedicationReminder ->
                MedicationReminder.getMedication().equals(medName)).map(MedicationReminder -> {
            mrr.delete(MedicationReminder);
            return ResponseEntity.ok("Success");
        });
        return ResponseEntity.badRequest().body("Error!");
    }

    /**
     * This method deletes all of specified user's medication reminders
     * @param id Valid user ID from user database
     * @return Success or error message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMedReminderByUserID(@PathVariable("id") UUID id) {
        if (userRepository.findById(id).isPresent()) {
            mrr.deleteById(id);
            return ResponseEntity.ok("Success!");
        }
        return ResponseEntity.badRequest().body("Error! User ID is invalid!");
    }

    /**
     * This method updates a medication reminder by user ID and medication name
     * @param id Valid user ID from user database
     * @param medName Medication Name
     * @param mr Updated version of the medication reminder
     * @return Updated medication reminder
     */
    @PutMapping("/{id}/{med}")
    public ResponseEntity<?> updateMedReminderByMedName(@PathVariable("id") UUID id, @PathVariable("med") String medName,
                                          @RequestBody @Valid @NonNull MedicationReminder mr) {
        Optional<MedicationReminder> tempMr = mrr.findById(id).filter(MedicationReminder ->
                MedicationReminder.getMedication().equals(mr.getMedication()));
        if (tempMr.isPresent()) {
            MedicationReminder newMr = tempMr.get();
            newMr.setMedStartTime(mr.getMedStartTime());
            newMr.setMedEndTime(mr.getMedEndTime());
            newMr.setMedication(mr.getMedication());
            newMr.setMedInterval(mr.getMedInterval());
            return ResponseEntity.ok(mrr.save(newMr));
        }
        return ResponseEntity.badRequest().body("Error!");
    }
}