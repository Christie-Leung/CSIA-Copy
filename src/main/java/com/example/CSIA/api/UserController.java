package com.example.CSIA.api;

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

    // Adds a User
    @PostMapping
    public ResponseEntity<?> insertUser(@NonNull @Valid @RequestBody User user) {
        userRepository.save(user);
        return ResponseEntity.ok("Success!");
    }

    // Get All Users
    @GetMapping
    public ResponseEntity<?> getAllUser() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    // Get All ID
    @GetMapping("/id")
    public List<UUID> getAllUserID() {
        List<UUID> uuidList = new ArrayList<>();
        userRepository.findAll().forEach(User -> uuidList.add(User.getID()));
        return uuidList;
    }

    // Get User by ID
    @GetMapping("/id/{id}")
    public User getUserById(@PathVariable("id") UUID id) {
        return userRepository.findById(id)
                .orElse(null);
    }

    // Get Users by Role
    @GetMapping("/role/{role}")
    public List<User> getUserByRole(@PathVariable("role") String role) {
        List<User> userByRole = new ArrayList<>();

        for (User user : userRepository.findAll()) {
            if (user.getRole().contentEquals(role.toUpperCase())) {
                userByRole.add(user);
            }
        }
        return userByRole;
    }

    // Delete User
    @DeleteMapping("/{id}")
    public void removeUser(@PathVariable("id") UUID id) {
        userRepository.deleteById(id);
    }

    // Update User
    @PutMapping("/{id}")
    public User updateUser(@PathVariable("id") UUID id, @Valid @NonNull @RequestBody User user) {
        User newUser = userRepository.findById(id).orElse(null);
        assert newUser != null;
        newUser.setName(user.getName());
        newUser.setRole(user.getRole());
        return userRepository.save(newUser);
    }
}