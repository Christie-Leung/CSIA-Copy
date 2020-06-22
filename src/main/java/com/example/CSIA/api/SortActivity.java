package com.example.CSIA.api;

import com.example.CSIA.entity.Activity;

import java.util.Comparator;

public class SortActivity implements Comparator<Activity> {
    @Override
    public int compare(Activity o1, Activity o2) {
        return o1.getStartTime().compareTo(o2.getEndTime());
    }
}
