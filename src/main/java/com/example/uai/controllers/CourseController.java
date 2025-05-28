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
import com.example.uai.models.Course;
import com.example.uai.repository.CourseRepository;



@Controller("course")
public class CourseController {
    private final CourseRepository courseRepository;

    public CourseController(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    // 获取所有课程
    @GetMapping("/courses")
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    // 根据ID获取课程
    @GetMapping("/courses/{id}")
    public ResponseEntity<Course> getCourseById(@PathVariable UUID id) {
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
    public Course createCourse(@RequestBody Course course) {
        return courseRepository.save(course);
    }

    // 更新课程
    @PutMapping("/courses/{id}")
    public ResponseEntity<Course> updateCourse(@PathVariable UUID id, @RequestBody Course courseDetails) {
        try {
            return courseRepository.findById(id)
                    .map(course -> {
                        course.setName(courseDetails.getName());
                        course.setDesc(courseDetails.getDesc());
                        // 其他字段...
                        return ResponseEntity.ok(courseRepository.save(course));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // 删除课程
    @DeleteMapping("/courses/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable UUID id) {
        try {
            return courseRepository.findById(id)
                    .map(course -> {
                        courseRepository.delete(course);
                        return ResponseEntity.noContent().<Void>build();
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
