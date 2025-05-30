package com.example.uai.controllers;
import com.example.uai.models.Course;
import com.example.uai.models.Credit;
import com.example.uai.models.User;
import com.example.uai.repository.CourseRepository;
import com.example.uai.repository.CreditRepository;
import com.example.uai.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;



@RestController
@RequestMapping("credit")
    public class CreditController {
    private final CreditRepository creditRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    @Autowired
    public CreditController(CreditRepository creditRepository, UserRepository userRepository, CourseRepository courseRepository) {
        this.creditRepository = creditRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
    }

    // 获取某课程的所有学分
    @GetMapping("/course/{courseId}")
    public Object getCreditsByCourse(@PathVariable UUID courseId) {
        try {
            return creditRepository.findByCourseId(courseId);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // 获取某学生的所有学分
    @GetMapping("/student/{studentId}")
    public Object getCreditsByStudent(@PathVariable UUID studentId) {
        try {
            return creditRepository.findByStudentId(studentId);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // 新增某学生的学分记录
    @PostMapping("/credits")
    public ResponseEntity<Object> addCredit(@RequestBody Credit credit) {
        try {
            // 只用id查找student和course
            UUID studentId = credit.getStudent() != null ? credit.getStudent().getId() : null;
            UUID courseId = credit.getCourse() != null ? credit.getCourse().getId() : null;
            if (creditRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
                return ResponseEntity.badRequest().body("该学生已存在此课程的学分记录");
            }
            if (studentId == null || courseId == null) {
                return ResponseEntity.badRequest().body("studentId和courseId不能为空");
            }
            User student = userRepository.findById(studentId).orElse(null);
            Course course = courseRepository.findById(courseId).orElse(null);
            if (student == null || course == null) {
                return ResponseEntity.badRequest().body("student或course不存在");
            }
            credit.setStudent(student);
            credit.setCourse(course);
            creditRepository.save(credit);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // 修改某学生的学分记录(仅学分值）
    @PutMapping("/credits/{creditId}")
    public ResponseEntity<Object> updateCredit(@PathVariable UUID creditId, @RequestBody Double creditValue) {
        try {
            Credit credit = creditRepository.findById(creditId).orElse(null);
            if (credit == null) {
                return ResponseEntity.notFound().build();
            }
            credit.setCreditValue(creditValue);
            creditRepository.save(credit);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

}
