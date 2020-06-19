package com.example.CSIA.api;

import com.example.CSIA.entity.User;

import java.util.Comparator;

public class SortUser implements Comparator<User> {

    @Override
    public int compare(User user1, User user2) {
        return user1.getName().compareTo(user2.getName());
    }
}
