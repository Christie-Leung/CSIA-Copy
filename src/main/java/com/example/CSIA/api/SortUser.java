package com.example.CSIA.api;

import com.example.CSIA.entity.User;

import java.util.Comparator;

public class SortUser implements Comparator<User> {

    /**
     * Sorts the user entity by name
     *
     * @param user1 First user to be compared with
     * @param user2 Second user to be compared with
     * @return value of comparator
     */
    @Override
    public int compare(User user1, User user2) {
        return user1.getName().compareTo(user2.getName());
    }
}
