package com.example.uai.controllers;

import com.example.uai.models.Course;
import com.example.uai.models.Credit;
import com.example.uai.models.DTO.CourseWithCreditDto;
import com.example.uai.models.User;
import com.example.uai.repository.CourseRepository;
import com.example.uai.repository.CreditRepository;
import com.example.uai.repository.UserRepository;
import com.example.uai.utils.TokenUtil;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@RestController
@RequestMapping("course")
public class CourseController {
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CreditRepository creditRepository;
    private final TokenUtil tokenUtil;

    @Autowired
    public CourseController(CourseRepository courseRepository, UserRepository userRepository, CreditRepository creditRepository, TokenUtil tokenUtil) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.creditRepository = creditRepository;
        this.tokenUtil = tokenUtil;
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

    // 教师接口： 获取所有关联课程
    @GetMapping("/teacher/courses")
    public Object getTeacherCourses(@RequestHeader("token") String token) {
        try {
            // 从token中获取用户ID
            UUID userId = tokenUtil.getUserIdFromToken(token);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("用户不存在"));
            if (!"staff".equals(user.getRole()) && !"admin".equals(user.getRole())) {
                return ResponseEntity.status(403).body("无权限访问");
            }
            // 获取用户所有课程(simple dto)
            return courseRepository.findCoursesByUserId(userId);
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
    @PostMapping("/addCourse")
    public Object createCourse(@RequestBody Course course, @RequestHeader("token") String token) {
        UUID id = tokenUtil.getUserIdFromToken(token);
        if (id == null) {
            return ResponseEntity.badRequest().body("无效的用户ID");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在"));
        try {
            if (courseRepository.existsByName(course.getName())) {
                return ResponseEntity.badRequest().body("课程名称已存在");
            }
            course.setId(UUID.randomUUID()); // 设置新课程的唯一ID
            // 1. 保存课程实体
            Course savedCourse = courseRepository.save(course);
            // 2. 通过拥有方(User)添加关系
            user.getCourses().add(savedCourse);
            userRepository.save(user); // 保存用户以更新关系
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // 更新课程
    @PutMapping("/courses/{id}")
    public ResponseEntity<?> updateCourse(@PathVariable UUID id, @RequestBody Course courseDetails) {
        try {
            return courseRepository.findById(id)
                    .map(course -> {
                        course.setName(courseDetails.getName());
                        course.setDescription(courseDetails.getDescription());
                        // 其他字段...
                        Course updatedCourse = courseRepository.save(course);
                        return ResponseEntity.ok(updatedCourse);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // 删除课程
    @DeleteMapping("/delete")
    public Object deleteCourse(@RequestHeader("Id") String id) {
        try {
            UUID courseId = UUID.fromString(id);
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new EntityNotFoundException("课程不存在"));
            // 删除课程与用户的关系
            List<User> users = userRepository.findUsersByCourseId(courseId);
            for (User user : users) {
                user.getCourses().remove(course);
                userRepository.save(user); // 保存用户以更新关系
            }
            // 删除课程实体
            if (creditRepository.existsByCourseId(courseId)) {
                return ResponseEntity.badRequest().body("课程有学分记录，无法删除");
            }
            // 删除课程
            courseRepository.delete(course);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // 学生端接口：获取所有已选课程及其学分信息
    @GetMapping("/student/{token}/selected")
    public Object getUserCourses(@PathVariable String token) {
        try{
            // 从token中获取用户ID
            UUID userId = tokenUtil.getUserIdFromToken(token);
            // 1. 验证用户存在
            userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("用户不存在"));
            // 2. 获取用户所有课程（包括没有学分记录的）
            List<Course> courses = courseRepository.findByUsersId(userId);
            // 3. 获取用户所有学分记录（按课程ID分组）
            Map<UUID, Credit> creditsMap = creditRepository.findByStudentId(userId)
                    .stream()
                    .collect(Collectors.toMap(
                            credit -> credit.getCourse().getId(),
                            Function.identity()
                    ));
            // 4. 构建结果集
            List<CourseWithCreditDto> result = new ArrayList<>();

            for (Course course : courses) {
                Credit credit = creditsMap.get(course.getId());
                Double creditValue = null;

                if (credit != null) {
                    creditValue = credit.getCreditValue();
                }

                result.add(new CourseWithCreditDto(
                        course.getId(),
                        course.getName(),
                        course.getDescription(),
                        creditValue
                ));
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // 学生端接口：获取所有未选课程
    @GetMapping("/student/{token}/available")
    public Object getAvailableCourses(@PathVariable String token) {
        try {
            // 从token中获取用户ID
            UUID userId = tokenUtil.getUserIdFromToken(token);
            userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("用户不存在"));
            // 获取用户未选课程
            return courseRepository.findAvailableCourses(userId);

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // 学生端接口：选课
    @PostMapping("/student/select")
    public Object selectCourse(@RequestHeader("token") String token, @RequestBody Map<String, UUID> courseIdMap) {
        try {
            // 从token中获取用户ID
            UUID userId = tokenUtil.getUserIdFromToken(token);
            UUID courseId = courseIdMap.get("courseId");
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("用户不存在"));
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new EntityNotFoundException("课程不存在"));

            // 检查用户是否已选该课程
            if (user.getCourses().contains(course)) {
                return ResponseEntity.badRequest().body("您已选此课程");
            }

            // 添加课程到用户
            user.getCourses().add(course);
            userRepository.save(user); // 保存用户以更新关系

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // 学生端接口：退课
    @PostMapping("/student/drop")
    public Object dropCourse(@RequestHeader("token") String token, @RequestBody Map<String, UUID> courseIdMap) {
        try {
            // 从token中获取用户ID
            UUID courseId = courseIdMap.get("courseId");
            UUID userId = tokenUtil.getUserIdFromToken(token);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("用户不存在"));
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new EntityNotFoundException("课程不存在"));

            // 检查用户是否已选该课程
            if (!user.getCourses().contains(course)) {
                return ResponseEntity.badRequest().body("您未选此课程");
            }

            // 从用户课程列表中移除该课程
            user.getCourses().remove(course);
            userRepository.save(user); // 保存用户以更新关系

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

}
