package com.example.uai.repository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.uai.models.Course;



public interface CourseRepository extends JpaRepository<Course, UUID> {
    // 根据课程名称查找课程
    List<Course> findByName(String name);

    // 根据教师ID查找课程
    List<Course> findByTeacherId(UUID teacherId);

    // 判断某课程名是否已存在
    boolean existsByName(String name);

    // 删除指定教师的所有课程
    void deleteByTeacherId(UUID teacherId);
}
