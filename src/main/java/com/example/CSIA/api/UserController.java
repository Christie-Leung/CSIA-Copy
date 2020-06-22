package com.example.CSIA.api;

import com.example.CSIA.converter.RoleConverter;
import com.example.CSIA.entity.User;
import com.example.CSIA.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequestMapping("/api/user")
@RestController
public class UserController {


    private final UserRepository userRepository;

    @Autowired
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * This method adds a user to the user database
     *
     * @param user Valid user entity parsed through JSON file
     * @return Success message
     */
    @PostMapping
    public ResponseEntity<?> insertUser(@NonNull @Valid @RequestBody User user) {
        if (RoleConverter.getUserRoleHashTable().get(user.getRole()) != null) {
            if (user.getName().split("\\s+").length >= 2) {
                userRepository.save(user);
                return ResponseEntity.ok("Success!");
            }
            return ResponseEntity.badRequest().body("Error! Please include a first and last name!");
        }
        return ResponseEntity.badRequest().body("Error! " + user.getRole() + " is not a valid role!");
    }

    /**
     * This method gets all users in the database
     *
     * @return List of all users
     */
    @GetMapping
    public ResponseEntity<?> getAllUser() {
        List<User> users = new ArrayList<>();
        userRepository.findAll().forEach(users::add);
        users.sort(new SortUser());
        return ResponseEntity.ok(users);
    }

    /**
     * This method gets all user's ID in the database
     *
     * @return List of all user IDs.
     */
    @GetMapping("/id")
    public ResponseEntity<?> getAllUserID() {
        List<UUID> uuidList = new ArrayList<>();
        userRepository.findAll().forEach(User -> uuidList.add(User.getID()));
        return ResponseEntity.ok(uuidList);
    }

    /**
     * This method gets a user by ID.
     *
     * @param id Valid ID
     * @return Request user by ID
     */
    @GetMapping("/id/{id}")
    public ResponseEntity<?> getUserById(@PathVariable("id") UUID id) {
        if (userRepository.findById(id).isPresent()) {
            return ResponseEntity.ok(userRepository.findById(id));
        }
        return ResponseEntity.badRequest().body("Error! User ID is invalid!");
    }

    /**
     * This method gets all users by role.
     *
     * @param role role of user
     * @return List of all users with specified role
     */
    @GetMapping("/role/{role}")
    public List<User> getUserByRole(@PathVariable("role") String role) {
        List<User> userByRole = new ArrayList<>();

        for (User user : userRepository.findAll()) {
            if (user.getRole().contentEquals(role.toUpperCase())) {
                userByRole.add(user);
            }
        }
        userByRole.sort(new SortUser());
        return userByRole;
    }

    /**
     * This method deletes a user by ID
     *
     * @param id Valid user ID
     * @return Success or error message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> removeUser(@PathVariable("id") UUID id) {
        if (userRepository.findById(id).isPresent()) {
            userRepository.deleteById(id);
            return ResponseEntity.ok("Success!");
        }
        return ResponseEntity.badRequest().body("Error! User ID is invalid!");
    }

    /**
     * This method updates a user by user ID
     *
     * @param id   Valid user id
     * @param user Updated user entity
     * @return Updated user
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable("id") UUID id, @Valid @NonNull @RequestBody User user) {
        if (userRepository.findById(id).isPresent()) {
            User newUser = userRepository.findById(id).get();
            newUser.setName(user.getName());
            newUser.setRole(user.getRole());
            return ResponseEntity.ok(userRepository.save(newUser));
        }
        return ResponseEntity.badRequest().body("Error! User ID is invalid!");
    }
}