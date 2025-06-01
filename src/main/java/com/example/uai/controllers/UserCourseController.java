package com.example.uai.controllers;

import com.example.uai.models.DTO.UserCourseDto;
import com.example.uai.repository.CourseRepository;
import com.example.uai.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;



@RestController
@RequestMapping("userCourse")
public class UserCourseController {
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    @Autowired
    public UserCourseController(UserRepository userRepository, CourseRepository courseRepository) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
    }

    // 为某用户新增课程（多个）
    @PostMapping("/addCourse")
    public Object addCourseToUser(@RequestBody UserCourseDto userCourseDto) {
        try {
            UUID userId = userCourseDto.getUserId();
            List<UUID> courseIds = userCourseDto.getCourseIds();
            var user = userRepository.findById(userId);
            var courses = courseRepository.findAllById(courseIds);
            if (user.isEmpty() || courses.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            user.get().getCourses().addAll(courses);
            for (var course : courses) {
                course.getUsers().add(user.get());
                courseRepository.save(course);
            }
            userRepository.save(user.get());
            return "success";
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // 为某用户删除课程（多个）
    @PostMapping("/removeCourse")
    public Object removeCourseFromUser(@RequestParam UUID userId, @RequestParam List<UUID> courseIds) {
        try {
            var user = userRepository.findById(userId);
            var courses = courseRepository.findAllById(courseIds);
            if (user.isEmpty() || courses.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            user.get().getCourses().removeAll(courses);
            for (var course : courses) {
                course.getUsers().remove(user.get());
                courseRepository.save(course);
            }
            userRepository.save(user.get());
            return "success";
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // 获取某用户的所有课程
    @PostMapping("/getUserCourses")
    public Object getUserCourses(@RequestParam UUID userId) {
        try {
            var user = userRepository.findById(userId);
            if (user.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return user.get().getCourses();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // 获取某课程的所有用户
    @PostMapping("/getCourseUsers")
    public Object getCourseUsers(@RequestParam UUID courseId) {
        try {
            var course = courseRepository.findById(courseId);
            if (course.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return course.get().getUsers();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

}
