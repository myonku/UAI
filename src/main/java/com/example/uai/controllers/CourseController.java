package com.example.uai.controllers;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.uai.models.Course;
import com.example.uai.repository.CourseRepository;



@RestController
@RequestMapping("course")
public class CourseController {
    private final CourseRepository courseRepository;

    @Autowired
    public CourseController(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    // 获取所有课程
    @GetMapping("/courses")
    public Object getAllCourses() {
        try {
            return courseRepository.findAll();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // 根据ID获取课程
    @GetMapping("/courses/{id}")
    public Object getCourseById(@PathVariable UUID id) {
        try {
            return courseRepository.findById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // 创建新课程
    @PostMapping("/courses")
    public Object createCourse(@RequestBody Course course) {
        try {
            return courseRepository.save(course);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // 更新课程
    @PutMapping("/courses/{id}")
    public Object updateCourse(@PathVariable UUID id, @RequestBody Course courseDetails) {
        try {
            return courseRepository.findById(id)
                    .map(course -> {
                        course.setName(courseDetails.getName());
                        course.setDescription(courseDetails.getDescription());
                        // 其他字段...
                        return courseRepository.save(course);
                    })
                    .orElse((Course) ResponseEntity.notFound());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest();
        }
    }

    // 删除课程
    @DeleteMapping("/courses/{id}")
    public Object deleteCourse(@PathVariable UUID id) {
        try {
            return courseRepository.findById(id)
                    .map(course -> {
                        courseRepository.delete(course);
                        return ResponseEntity.noContent().<Void>build();  // 204 No Content
                    })
                    .orElse(ResponseEntity.notFound().build()); // 404 Not Found
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
