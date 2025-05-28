package com.example.uai.controllers;
import com.example.uai.models.User;
import com.example.uai.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller("auth")
public class AuthController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 注册
    @PostMapping("/register")
    public ResponseEntity<Object> register(@RequestBody User user) {
        try {
            String password = user.getPassword();
            String encrypted = passwordEncoder.encode(password);
            user.setPassword(encrypted);
            userRepository.save(user);
            return ResponseEntity.ok(user.getId());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 登录
    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestParam String name, @RequestParam String password) {
        try {
            Optional<User> user = userRepository.findByUsername(name);
            if (user.isEmpty()) {
                return ResponseEntity.badRequest().body("User not found");
            } else {
                boolean match = passwordEncoder.matches(password, user.get().getPassword());
                if (match) {
                    return ResponseEntity.ok(user.get().getId());
                } else return ResponseEntity.ok().body("Incorrect password");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 验证注册数据
    @GetMapping("/checkRegister")
    public ResponseEntity<Object> checkRegister(@RequestParam String name, @RequestParam String email) {
        try {
            if (userRepository.existsByUsername(name) || userRepository.existsByEmail(email)) {
                return ResponseEntity.ok().body("User already exists");
            } else {
                return ResponseEntity.ok().body("ok");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

