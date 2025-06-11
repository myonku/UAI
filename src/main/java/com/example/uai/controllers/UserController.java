package com.example.uai.controllers;
import com.example.uai.models.DTO.StudentCredit;
import com.example.uai.models.DTO.UserDto;
import com.example.uai.models.User;
import com.example.uai.repository.CreditRepository;
import com.example.uai.repository.UserRepository;
import com.example.uai.utils.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.*;


@RestController
@RequestMapping("user")
public class UserController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenUtil tokenUtil;

    @Autowired
    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenUtil tokenUtil, CreditRepository creditRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenUtil = tokenUtil;
    }

    // 获取所有用户（排除所有role是admin的用户）
    @GetMapping("/all")
    public Object getAllUsers(@RequestHeader("token") String token) {
        if (token == null || token.isEmpty()) {
            return ResponseEntity.badRequest().body("Token is required");
        }
        UUID userId = tokenUtil.getUserIdFromToken(token);
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(403).body("User not found");
        }
        if (!"admin".equals(userOptional.get().getRole())) {
            return ResponseEntity.status(403).body("Access denied");
        }
        try {
            List<UserDto> users = userRepository.findAllWithoutPassword();
            return users.stream()
                .filter(user -> !"admin".equals(user.getRole()))
                .toList();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // 获取用户信息
    @GetMapping("/userInfo")
    public Object getUserInfo(@RequestHeader("token") String token) {
        try {
            UUID id = tokenUtil.getUserIdFromToken(token);
            if (token.isEmpty() || id == null) {
                return ResponseEntity.badRequest().body("Invalid token");
            }
            UserDto user = userRepository.findDTOById(id);
            return Objects.requireNonNullElseGet(user, () -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // 获取用户名
    @GetMapping("/username")
    public ResponseEntity<String> getUsername(@RequestHeader("token") String token) {
        if (token == null || token.isEmpty()) {
            return ResponseEntity.badRequest().body("Token is required");
        }
        try {
            UUID id = tokenUtil.getUserIdFromToken(token);
            return userRepository.findById(id)
                .map(user -> ResponseEntity.ok(user.getUsername()))
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }


    // 更新用户
    @PutMapping("/{id}")
    public ResponseEntity<Object> updateUser(@PathVariable UUID id, @RequestBody User userDetails) {
        try {
            return userRepository.findById(id)
                .map(user -> {
                    user.setUsername(userDetails.getUsername());
                    user.setEmail(userDetails.getEmail());
                    if (!Objects.equals(userDetails.getPassword(), "")) {
                        String newPassword = passwordEncoder.encode(userDetails.getPassword());
                        user.setPassword(newPassword);
                    }
                    user.setRole(userDetails.getRole());
                    userRepository.save(user);
                    return ResponseEntity.ok().build();
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

    // 根据课程ID获取所有学生的课程和学分信息
    @GetMapping("/course/{courseId}/students")
    public ResponseEntity<?> getStudentsByCourseId(@PathVariable UUID courseId) {
        try {
            List<StudentCredit> studentCredits = userRepository.findStudentCreditsByCourseId(courseId);
            return ResponseEntity.ok(studentCredits);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

}
