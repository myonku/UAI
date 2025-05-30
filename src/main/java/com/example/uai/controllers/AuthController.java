package com.example.uai.controllers;
import com.example.uai.models.User;
import com.example.uai.repository.UserRepository;
import com.example.uai.utils.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;
import java.util.UUID;


@RestController
@RequestMapping("auth")
public class AuthController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenUtil tokenUtil;

    @Autowired
    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenUtil tokenUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenUtil = tokenUtil;
    }

    // 注册
    @PostMapping("/register")
    public Object register(@RequestBody User user) {
        try {
            String password = user.getPassword();
            String encrypted = passwordEncoder.encode(password);
            user.setPassword(encrypted);
            UUID id = UUID.randomUUID();
            user.setId(id);
            user.setRole("default");
            user.setStatus("active");

            userRepository.save(user);
            return "success";
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // 登录
    @PostMapping("/login")
    public Object login(@RequestParam String name, @RequestParam String password) {
        try {
            Optional<User> user = userRepository.findByUsername(name);
            if (user.isEmpty()) {
                return "notfound";
            } else {
                boolean match = passwordEncoder.matches(password, user.get().getPassword());
                if (match) {
                    String token = tokenUtil.generateToken(user.get().getId());
                    return ResponseEntity.ok(token);
                } else return "error";
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // 验证注册数据
    @PostMapping("/checkRegister")
    public Object checkRegister(@RequestParam String name) {
        try {
            if (userRepository.existsByUsername(name)) {
                return "error";
            } else
                return "success";
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // 验证token
    @PostMapping("/verify_token")
    public Object verifyToken(@RequestParam String token) {
        if (tokenUtil.validateToken(token)){
            UUID userId = tokenUtil.getUserIdFromToken(token);
            Optional<User> user = userRepository.findById(userId);
            if (user.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            return user.get().getRole();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

}

