package com.example.uai.repository;

import com.example.uai.models.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;



public interface CourseRepository extends JpaRepository<Course, UUID> {
    // 根据课程名称查找课程
    List<Course> findByName(String name);

    // 判断某课程名是否已存在
    boolean existsByName(String name);

}
