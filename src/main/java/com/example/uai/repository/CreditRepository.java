package com.example.uai.repository;
import com.example.uai.models.Credit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CreditRepository extends JpaRepository<Credit, UUID> {
    // 通过学生ID和课程ID查找学分记录
    Optional<Credit> findByStudentIdAndCourseId(UUID studentId, UUID courseId);

    // 根据学生ID查找所有学分记录
    List<Credit> findByStudentId(UUID studentId);

    // 根据课程ID查找所有学分记录
    List<Credit> findByCourseId(UUID courseId);

    // 删除指定学生的所有学分记录
    void deleteByStudentId(UUID studentId);

    // 检查是否存在某学生某门课程的学分记录
    boolean existsByStudentIdAndCourseId(UUID studentId, UUID courseId);
}
