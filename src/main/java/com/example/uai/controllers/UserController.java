package com.example.uai.controllers;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.uai.models.User;
import com.example.uai.repository.UserRepository;

@Controller("user")
public class UserController {
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 获取所有用户
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // 根据ID获取用户
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable UUID id) {
        try {
            return userRepository.findById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }


    // 更新用户
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable UUID id, @RequestBody User userDetails) {
        try {
            return userRepository.findById(id)
                    .map(user -> {
                        user.setUsername(userDetails.getUsername());
                        user.setEmail(userDetails.getEmail());
                        // 其他字段...
                        userRepository.save(user);
                        return ResponseEntity.ok(user);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // 删除用户
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteUser(@PathVariable UUID id) {
        try {
            return userRepository.findById(id)
                    .map(user -> {
                        userRepository.delete(user);
                        return ResponseEntity.noContent().build();
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
