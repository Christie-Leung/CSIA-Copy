package com.example.CSIA.api;

import com.example.CSIA.converter.RoleConverter;
import com.example.CSIA.entity.Activity;
import com.example.CSIA.exceptions.RoleException;
import com.example.CSIA.repository.CalendarRepository;
import com.example.CSIA.repository.UserRepository;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RequestMapping("/api/calendar")
@RestController
public class CalendarController {

    CalendarRepository calendarRepository;
    UserRepository userRepository;

    @Autowired
    public CalendarController(CalendarRepository calendarRepository, UserRepository userRepository) {
        this.calendarRepository = calendarRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<?> insertActivity(@Valid @NonNull @RequestBody Activity activity) {
        calendarRepository.save(activity);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/shift/{userId}")
    public ResponseEntity<?> addShiftHours(@PathVariable("userId") UUID id, @Valid @NonNull @RequestBody Activity activity) {
        if (userRepository.findById(id).isPresent()) {
            int role = RoleConverter.getUserRoleHashTable().get(userRepository.findById(id).get().getRole());
            if (role == 3) {
                calendarRepository.save(activity);
                return ResponseEntity.accepted().body("Success!");
            } else {
                throw new RoleException(userRepository.findById(id).get().getRole(), "DOCTOR");
            }
        }
        return ResponseEntity.badRequest().build();
    }


    @PostMapping("/book/{userId}")
    public ResponseEntity<?> bookAppointment(@PathVariable("userId") UUID id, @Valid @NonNull @RequestBody Activity activity) {
        if (userRepository.findById(id).isPresent() && activity.getRespondingUserId() != null) {
            int role = RoleConverter.getUserRoleHashTable().get(userRepository.findById(id).get().getRole());
            if (role == 1 && activity.getRespondingUserId() != null) {
                LocalDateTime[][] doctorHours = getDoctorAvailableHours(activity.getRespondingUserId(), activity.getStartTime().toLocalDate());
                if (doctorHours != null) {
                    for (LocalDateTime[] shift : doctorHours) {
                        if ((activity.getStartTime().isAfter(shift[0]) || activity.getStartTime().equals(shift[0]))
                                && (activity.getEndTime().isBefore(shift[1]) || activity.getEndTime().equals(shift[1]))) {
                            calendarRepository.save(activity);
                            return ResponseEntity.ok("Success!");
                        }
                        HttpHeaders headers = new HttpHeaders();
                        headers.add("Error! These are not within the doctor's available hours!", "foo");

                        return ResponseEntity.badRequest().headers(headers).body(getDoctorAvailableHours(activity.getRespondingUserId(), activity.getStartTime().toLocalDate()));
                    }
                }
            } else {
                throw new RoleException(userRepository.findById(id).get().getRole(), "PATIENT");
            }
        }
        return ResponseEntity.badRequest().build();
    }

    @GetMapping("/shift/{id}")
    public ResponseEntity<?> getShiftHours(@PathVariable("id") UUID id) {
        LocalDateTime[][] shifts = getShifts(id);
        return ResponseEntity.ok(shifts);
    }

    @GetMapping("/available/{doctorId}/{date}")
    public ResponseEntity<?> getAvailableHours(@PathVariable("doctorId") UUID id, @PathVariable("date") LocalDate date) {
        if (userRepository.findById(id).isPresent()) {
            if (RoleConverter.getUserRoleHashTable().get(userRepository.findById(id).get().getRole()) == 3) {
                return ResponseEntity.ok(Objects.requireNonNull(getDoctorAvailableHours(id, date)));
            } else {
                return ResponseEntity.badRequest().body("User is not a doctor!");
            }
        } else {
            return ResponseEntity.badRequest().body("Not a valid user!");
        }
    }



    @DeleteMapping("/{id}")
    public int deleteActivityByUserId(@PathVariable("id") UUID id) {
        calendarRepository.deleteById(id);
        return 1;
    }

    @DeleteMapping("/activity/{activityId}")
    public ResponseEntity<?> deleteActivityByActivityId(@PathVariable("activityId") UUID activityId) {
        if (calendarRepository.findById(activityId).isPresent()) {
            calendarRepository.deleteById(activityId);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/activity/{activityId}")
    public ResponseEntity<?> updateActivityById(@PathVariable("activityId") UUID activityId,
                                  @NonNull @Valid @RequestBody Activity activity) {

        if (calendarRepository.findById(activityId).isPresent()) {
            Activity newActivity = calendarRepository.findById(activityId).get();
            newActivity.setActivityName(activity.getActivityName());
            newActivity.setEndTime(activity.getEndTime());
            newActivity.setStartTime(activity.getStartTime());
            return ResponseEntity.ok(calendarRepository.save(newActivity));
        }
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/user/{id}")
    public ResponseEntity<?> getActivityByUserId(@PathVariable("id") UUID userId) {
        List<Activity> userActivities = getUserActivities(userId);
        if (userActivities != null) {
            if (userActivities.size() > 0) {
                return ResponseEntity.ok(getUserActivities(userId));
            } else {
                return ResponseEntity.badRequest().body("User has no activities!");
            }
        }
        return ResponseEntity.badRequest().body("Error!");
    }

    @GetMapping
    public Iterable<Activity> getAllActivity() {
        return calendarRepository.findAll();
    }

    @GetMapping("/activity/{activityId}")
    public Activity getCalendarByActivityId(@PathVariable("activityId") UUID userID, @PathVariable("activityId") UUID activityID) {
        if (calendarRepository.findById(activityID).isPresent()) {
            return calendarRepository.findById(activityID).get();
        }
        return null;
    }

    private LocalDateTime[][] getShifts(UUID userId) {
        List<Activity> userActivities = getUserActivities(userId);
        assert userActivities != null;
        LocalDateTime[][] shifts = new LocalDateTime[userActivities.size()][];
        for (int x = 0; x < userActivities.size(); x++) {
            shifts[x] = new LocalDateTime[]{userActivities.get(x).getStartTime(), userActivities.get(x).getEndTime()};
        }
        return shifts;
    }

    private List<Activity> getUserActivities(UUID userId) {
        List<Activity> userActivities = new ArrayList<>();
        for (Activity activity : calendarRepository.findAll()) {
            if (activity.getUserId().equals(userId)) {
                userActivities.add(activity);
            }
        }
        if (!userActivities.isEmpty()) {
            return userActivities;
        }
        return null;
    }

    private LocalDateTime[][] getDoctorAvailableHours(UUID id, LocalDate date) {
        LocalDateTime startTime = null;
        LocalDateTime endTime;
        List<LocalDateTime> appointments = new ArrayList<>();


        for (Activity activity : calendarRepository.findAll()) {
            if (activity.getUserId().equals(id) && activity.getStartTime().toLocalDate().equals(date)) {
                startTime = activity.getStartTime();
                endTime = activity.getEndTime();
                appointments.add(startTime);
                appointments.add(endTime);
            }
        }

        for (Activity activity : calendarRepository.findAll()) {
            if (activity.getRespondingUserId() != null) {
                if (activity.getRespondingUserId().equals(id) && activity.getStartTime().toLocalDate().equals(date)) {
                    appointments.add(activity.getStartTime());
                    appointments.add(activity.getEndTime());
                }
            }
        }
        Collections.sort(appointments);
        appointments = removeRepeatedElements(appointments);
        assert appointments != null;
        LocalDateTime[][] availableHours = new LocalDateTime[appointments.size()/2][];


        int count = 0;
        for (int x = 0; x < appointments.size(); x++) {
            if (x < appointments.size() - 2) {
                availableHours[count] = new LocalDateTime[]{appointments.get(x), appointments.get(x + 1)};
                count++;
            }
        }

        if (availableHours.length != 0) {
            return availableHours;
        }
        return null;
    }

    private List<LocalDateTime> removeRepeatedElements(List<LocalDateTime> list) {
        Collections.sort(list);
        List<LocalDateTime> finalList = new ArrayList<>();
        for (int x = 0; x < list.size(); x++) {
            if(x == 0) {
                if (!list.get(x).equals(list.get(x + 1))) {
                    finalList.add(list.get(x));
                }
            } else if (x < list.size() - 1){
                if (!list.get(x).equals(list.get(x - 1)) && !list.get(x).equals(list.get(x + 1))) {
                    finalList.add(list.get(x));
                } else if (!list.get(x).equals(list.get(x + 1))) {
                    finalList.add(list.get(x));
                }
            } else {
                if (!list.get(x).equals(list.get(x - 1))) {
                    finalList.add(list.get(x));
                }
            }
        }
        if (!finalList.isEmpty()) {
            return finalList;
        }
        return null;
    }

}
