package com.example.uai.repository;

import com.example.uai.models.Course;
import com.example.uai.models.DTO.SimpleCourseDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;



public interface CourseRepository extends JpaRepository<Course, UUID> {
    // 根据课程名称查找课程
    List<Course> findByName(String name);

    // 判断某课程名是否已存在
    boolean existsByName(String name);

    // 根据用户ID查找用户所选课程
    List<Course> findByUsersId(UUID userId);

    // 根据用户ID查找用户未选的课程
    @Query("SELECT new com.example.uai.models.DTO.SimpleCourseDto(c.id, c.name, c.description) " +
            "FROM Course c " +
            "WHERE c.id NOT IN " +
            "(SELECT c2.id FROM Course c2 JOIN c2.users u WHERE u.id = :userId)")
    List<SimpleCourseDto> findAvailableCourses(@Param("userId") UUID userId);

    // 根据用户ID查找关联课程
    @Query("SELECT new com.example.uai.models.DTO.SimpleCourseDto(c.id, c.name, c.description) " +
            "FROM Course c JOIN c.users u WHERE u.id = :userId")
    List<SimpleCourseDto> findCoursesByUserId(@Param("userId") UUID userId);

}
