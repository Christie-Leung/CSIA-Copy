package com.example.CSIA.repository;

import com.example.CSIA.entity.Activity;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface CalendarRepository extends CrudRepository<Activity, UUID> {

}
