package com.example.CSIA.api;

import com.example.CSIA.converter.RoleConverter;
import com.example.CSIA.entity.Activity;
import com.example.CSIA.entity.User;
import com.example.CSIA.exceptions.RoleException;
import com.example.CSIA.repository.CalendarRepository;
import com.example.CSIA.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
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

    /*
    @PostMapping
    public ResponseEntity<?> insertActivity(@Valid @NonNull @RequestBody Activity activity) {
        calendarRepository.save(activity);
        return ResponseEntity.accepted().build();
    }
     */

    /**
     * This method is used for inserting a doctor's shift hours. The doctor id must be present in the user database. The activity must be valid.
     * @param id Doctor ID that must be in the user database
     * @param activity A valid activity entity parsed through a JSON file
     * @return whether the method was successful or not
     */
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
        return ResponseEntity.badRequest().body("Error! User ID is invalid!");
    }


    /**
     * This method is used to book appointments. The id must be in the user database and is a patient. The activity must be valid.
     * @param id Patient ID that must be in the user database
     * @param activity A Valid activity entity parsed through a JSON file
     * @return whether the method was successful or the error that occured
     */
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

                        return ResponseEntity.badRequest().headers(headers).body(getDoctorAvailableHours(activity.getRespondingUserId(),
                                activity.getStartTime().toLocalDate()));
                    }
                }
            } else {
                throw new RoleException(userRepository.findById(id).get().getRole(), "PATIENT");
            }
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * This method gets a specified doctor's shifts in a specific day range.
     * @param id Doctor ID that must be in the user database.
     * @param startDate The first day in the specified day range.
     * @param endDate The last day in the specified day range.
     * @return An array of the specific doctor's shifts in the specified day range.
     */
    @GetMapping("/shift/{startDate}/{endDate}/{id}")
    public ResponseEntity<?> getShiftHours(@PathVariable("id") UUID id, @PathVariable("startDate") LocalDate startDate,
                                           @PathVariable("endDate") LocalDate endDate) {
        LocalDateTime[][] shifts = getShifts(id, startDate, endDate);
        return ResponseEntity.ok(shifts);
    }

    /**
     * This method gets a specified doctor's available hours during a requested day
     * @param id Doctor ID that must be in the user database.
     * @param date Specified date for doctor's available hours during shift
     * @return An array of the doctor's available hours during specified day
     */
    @GetMapping("/available/{date}/{doctorId}")
    public ResponseEntity<?> getAvailableHours(@PathVariable("doctorId") UUID id, @PathVariable("date") LocalDate date) {
        if (userRepository.findById(id).isPresent()) {
            if (RoleConverter.getUserRoleHashTable().get(userRepository.findById(id).get().getRole()) == 3) {
                return ResponseEntity.ok(Objects.requireNonNull(getDoctorAvailableHours(id, date)));
            } else {
                return ResponseEntity.badRequest().body("User is not a doctor!");
            }
        } else {
            return ResponseEntity.badRequest().body("Error! User ID is invalid!");
        }
    }

    /**
     * This method gets all doctor's available hours during a specified date.
     * @param date Specified day for getting doctors' available hours.
     * @return A map that is ordered by the doctor's name in alphabetical order with their available hours during shift on the specified day.
     */
    @GetMapping("/available/{date}")
    public ResponseEntity<?> getAvailableHours(@PathVariable("date") LocalDate date) {
        Map<String, LocalDateTime[][]> availableHours = new HashMap<>();
        for (User user : userRepository.findAll()) {
            if (RoleConverter.getUserRoleHashTable().get(user.getRole()) == 3) {
                availableHours.put(user.getName(), getDoctorAvailableHours(user.getID(), date));
            }
        }
        Map<String, LocalDateTime[][]> sortedMap = new TreeMap<>(availableHours);
        if (!sortedMap.isEmpty()) {
            return ResponseEntity.ok(sortedMap);
        }
        return ResponseEntity.badRequest().body("Error!");
    }

    /**
     * This method gets all activities by a user that is in the user database.
     * @param userId Valid ID that is in the user database.
     * @return A list of all the user's activities
     */
    @GetMapping("/user/{id}")
    public ResponseEntity<?> getActivityByUserId(@PathVariable("id") UUID userId) {
        if (userRepository.findById(userId).isPresent()) {
            List<Activity> userActivities = getUserActivities(userId);
            if (userActivities != null) {
                if (userActivities.size() > 0) {
                    return ResponseEntity.ok(userActivities);
                }
            }
            return ResponseEntity.badRequest().body("Error! User has no activities!");
        }
        return ResponseEntity.badRequest().body("Error! User ID is invalid!");
    }

    /**
     * This method gets all appointments in a specified day range.
     * @param startDate The first day in the specified day range.
     * @param endDate The last day in the specified day range.
     * @return A list of user's activities in the specified day range.
     */
    @GetMapping("/user/{startDate}/{endDate}")
    public ResponseEntity<?> getAppointmentsInRange(@PathVariable("startDate") LocalDate startDate, @PathVariable("endDate") LocalDate endDate) {
        return ResponseEntity.ok(Objects.requireNonNull(getUserActivities(1, startDate, endDate)));
    }

    /**
     * This method gets all activities in one day.
     * @param date Specified day to get all activities
     * @return A list of activities in the specified date.
     */
    @GetMapping("/day/{day}")
    public Iterable<Activity> getAllActivityByDay(@PathVariable("day") LocalDate date) {
        return getActivitiesInRange(date, date);
    }

    /**
     * This method gets all activities within the next 7 days
     * @return A list of activities occurring in the next 7 days from current date.
     */
    @GetMapping("/7day")
    public ResponseEntity<?> getAllActivitiesIn7Days() {
        return ResponseEntity.ok(Objects.requireNonNull(getActivitiesInRange(LocalDate.now(), LocalDate.now().plusDays(7))));
    }

    /**
     * This method gets all doctors' shift in a specified day range.
     * @param startDate The first day in the specified day range.
     * @param endDate The last day in the specified day range.
     * @return An array of the doctors' shifts in the specified day range sorted in alphabetical order.
     */
    @GetMapping("/shift/{startDate}/{endDate}")
    public ResponseEntity<?> getAllDoctorShifts(@PathVariable("startDate") LocalDate startDate, @PathVariable("endDate") LocalDate endDate) {
        Map<String, LocalDateTime[][]> doctorShifts = new HashMap<>();
        for (User user : userRepository.findAll()) {
            if (RoleConverter.getUserRoleHashTable().get(user.getRole()) == 3) {
                doctorShifts.put(user.getName(), getShifts(user.getID(), startDate, endDate));
            }
        }
        Map<String, LocalDateTime[][]> sortedMap = new TreeMap<>(doctorShifts);
        if (!sortedMap.isEmpty()) {
            return ResponseEntity.ok(sortedMap);
        }
        return ResponseEntity.badRequest().body("Error!");
    }

    /**
     * This method deletes all activities by user ID.
     * @param id Valid user ID in the user database
     * @return Success or error message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteActivityByUserId(@PathVariable("id") UUID id) {
        if (calendarRepository.findById(id).isPresent()) {
            calendarRepository.deleteById(id);
            return ResponseEntity.ok("Success!");
        }
        return ResponseEntity.badRequest().body("Error! User ID is invalid!");
    }

    /**
     * This method deletes an activity by an activity ID.
     * @param activityId Valid activity ID from calendar database.
     * @return Success or error message
     */
    @DeleteMapping("/activity/{activityId}")
    public ResponseEntity<?> deleteActivityByActivityId(@PathVariable("activityId") UUID activityId) {
        if (calendarRepository.findById(activityId).isPresent()) {
            calendarRepository.deleteById(activityId);
            return ResponseEntity.ok("Success!");
        }
        return ResponseEntity.badRequest().body("Error! User ID is invalid!");
    }

    /**
     * This method updates an activity by activity ID.
     * @param activityId Valid activity ID from calendar database.
     * @param activity Valid updated activity entity to be changed in the database.
     * @return Updated activity.
     */
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

    /**
     * This method gets an activity by activity ID.
     * @param activityID Valid activity ID in calendar database.
     * @return the activity specified
     */
    @GetMapping("/activity/{activityId}")
    public ResponseEntity<?> getActivityById(@PathVariable("activityId") UUID activityID) {
        if (calendarRepository.findById(activityID).isPresent()) {
            return ResponseEntity.ok(calendarRepository.findById(activityID).get());
        }
        return ResponseEntity.badRequest().body("Error! The activity ID is invalid");
    }

    /**
     * This method gets a doctor's shift by ID in a specified day range.
     * @param userId Valid doctor ID from the user database.
     * @param startDate The first date in the specified day range.
     * @param endDate The late date in the specified day range.
     * @return An array of the specified doctor's shift in the day range.
     */
    private LocalDateTime[][] getShifts(UUID userId, LocalDate startDate, LocalDate endDate) {
        List<Activity> userActivities = new ArrayList<>();
        for (Activity a : Objects.requireNonNull(getActivitiesInRange(startDate, endDate))) {
            if (a.getUserId().equals(userId)) {
                userActivities.add(a);
            }
        }
        LocalDateTime[][] shifts = new LocalDateTime[userActivities.size()][];
        for (int x = 0; x < userActivities.size(); x++) {
            shifts[x] = new LocalDateTime[]{userActivities.get(x).getStartTime(), userActivities.get(x).getEndTime()};
        }
        return shifts;
    }

    /**
     * This method gets all of user's activities from user ID.
     * @param userId Valid user ID from the user database
     * @return List of user's activities
     */
    private List<Activity> getUserActivities(UUID userId) {
        List<Activity> userActivities = new ArrayList<>();
        for (Activity activity : calendarRepository.findAll()) {
            if (!activity.getUserId().equals(userId)) {
                userActivities.add(activity);
            }
        }
        if (!userActivities.isEmpty()) {
            return userActivities;
        }
        return null;
    }

    /**
     * This method gets a specific role's activities in a specified day range
     * @param role Valid role in user database
     * @param startDate The first date in the specified day range.
     * @param endDate The late date in the specified day range.
     * @return List of role's activities in specified day range.
     */
    private List<Activity> getUserActivities(int role, LocalDate startDate, LocalDate endDate) {
        List<Activity> userActivities = new ArrayList<>();
        for (Activity activity : Objects.requireNonNull(getActivitiesInRange(startDate, endDate))) {
            if (userRepository.findById(activity.getUserId()).isPresent()) {
                User user = userRepository.findById(activity.getUserId()).get();
                if (RoleConverter.getUserRoleHashTable().get(user.getRole()) == role) {
                    userActivities.add(activity);
                }
            }
        }
        if (!userActivities.isEmpty()) {
            return userActivities;
        }
        return null;
    }

    /**
     * This method gets all activities in a specified day range.
     * @param startDate The first date in the specified day range.
     * @param endDate The late date in the specified day range.
     * @return List of all activities in specified day range.
     */
    private List<Activity> getActivitiesInRange(LocalDate startDate, LocalDate endDate) {
        List<Activity> activities = new ArrayList<>();
        for (Activity activity : calendarRepository.findAll()) {
            if (activity.getStartTime().toLocalDate().compareTo(startDate) >= 0 && activity.getEndTime().toLocalDate().compareTo(endDate) <= 0) {
                activities.add(activity);
            }
        }
        if (!activities.isEmpty()) {
            return activities;
        }
        return null;
    }

    /**
     * This method gets a specific doctor's available hours in a day
     * @param id Valid doctor ID from user database
     * @param date Specified date
     * @return An array of the specific doctor's available hours during a specified date.
     */
    private LocalDateTime[][] getDoctorAvailableHours(UUID id, LocalDate date) {
        LocalDateTime startTime;
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
            if (x < appointments.size()-1 && x % 2 == 0) {
                availableHours[count] = new LocalDateTime[]{appointments.get(x), appointments.get(x + 1)};
                count++;
            }
        }
        if (availableHours.length != 0) {
            return availableHours;
        }
        return null;
    }

    /**
     * This method removes both elements if it is repeated in a list
     * @param list List of LocalDateTime to be sorted and cleaned.
     * @return Sorted list that has all duplicated elements removed.
     */
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


