package com.example.CSIA.api;

import com.example.CSIA.entity.Activity;
import com.example.CSIA.repository.CalendarRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;
import java.util.UUID;

@RequestMapping("/api/calendar")
@RestController
public class CalendarController {

    CalendarRepository calendarRepository;

    @Autowired
    public CalendarController(CalendarRepository calendarRepository) {
        this.calendarRepository = calendarRepository;
    }

    @PostMapping("/{id}")
    public int insertActivity(@PathVariable("id") UUID id, @Valid @NonNull @RequestBody Activity activity) {
        calendarRepository.save(activity);
        return 1;
    }

    @DeleteMapping("/{id}")
    public int deleteActivityByUserId(@PathVariable("id") UUID id) {
        calendarRepository.deleteById(id);
        return 1;
    }

    @DeleteMapping("/{id}/{activityId}")
    public int deleteActivityByActivityId(@PathVariable("id") UUID userId, @PathVariable("activityId") UUID activityId) {
        calendarRepository.findById(userId).filter(Activity -> {
            if (Activity.getActivityID().equals(activityId)) {
                calendarRepository.delete(Activity);
                return true;
            }
            return false;
        });
        return 1;
    }

    @PutMapping("/{id}/{activityId}")
    public Activity updateActivityById(@PathVariable("id") UUID userId, @PathVariable("activityId") UUID activityId,
                                  @NonNull @Valid @RequestBody Activity activity) {

        Activity newActivity = calendarRepository.findById(userId).filter(Activity ->
                Activity.getActivityID().equals(activityId)).get();
        newActivity.setActivityName(activity.getActivityName());
        newActivity.setEndTime(activity.getEndTime());
        newActivity.setStartTime(activity.getStartTime());
        return calendarRepository.save(newActivity);
    }


    @GetMapping("/{id}")
    public Optional<Activity> getActivityByUserId(@PathVariable("id") UUID id) {
        return calendarRepository.findById(id);
    }

    @GetMapping
    public Iterable<Activity> getAllActivity() {
        return calendarRepository.findAll();
    }

    @GetMapping("/{id}/{activityId}")
    public Activity getCalendarByActivityId(@PathVariable("id") UUID userID, @PathVariable("activityId") UUID activityID) {
        return calendarRepository.findById(userID).filter(Activity -> Activity.getActivityID().equals(activityID)).orElse(null);
    }

}





















