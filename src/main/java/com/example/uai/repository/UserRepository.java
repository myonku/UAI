package com.example.uai.repository;

import com.example.uai.models.User;
import com.example.uai.models.DTO.StudentCredit;
import com.example.uai.models.DTO.UserDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface UserRepository extends JpaRepository<User, UUID> {

    // 通过用户名查找
    Optional<User> findByUsername(String username);

    // 获取所有用户（排除密码字段）
    @Query("SELECT new com.example.uai.models.DTO.UserDto(u.id, u.username, u.email, u.role, u.avatarPath, u.status) FROM User u")
    List<UserDto> findAllWithoutPassword();

    // 判断邮箱是否已存在
    boolean existsByEmail(String email);

    // 判断用户名是否存在
    boolean existsByUsername(String username);

    // 通过邮箱查找用户（排除密码字段，返回UserDTO）
    @Query("SELECT new com.example.uai.models.DTO.UserDto(u.id, u.username, u.email, u.role, u.avatarPath, u.status) FROM User u WHERE u.email = :email")
    Optional<UserDto> findDTOByEmail(String email);

    // 通过用户id查找用户（排除密码字段，返回UserDTO）
    @Query("SELECT new com.example.uai.models.DTO.UserDto(u.id, u.username, u.email, u.role, u.avatarPath, u.status) FROM User u WHERE u.id = :id")
    UserDto findDTOById(UUID id);

    // 通过课程id查找用户，返回student角色用户
    @Query("SELECT u FROM User u JOIN u.courses c WHERE c.id = :courseId AND u.role = 'student'")
    List<User> findUsersByCourseId(UUID courseId);

    // 查询课程关联的StudentCredit信息（根据新DTO模型）
    @Query("""
        select new com.example.uai.models.DTO.StudentCredit(
            u.id,
            u.username,
            c.id,
            u.id,
            c.name,
            cr.creditValue
        )
        from User u
        join u.courses c
        left join Credit cr on cr.student = u and cr.course = c
        where c.id = :courseId and u.role = 'student'
    """)
    List<StudentCredit> findStudentCreditsByCourseId(@Param("courseId") UUID courseId);

    // 根据id查询用户关联的课程总数、总学分值（学分表的value字段）、以及没有学分信息的课程数量
    @Query("""
        select
            count(c.id) as courseCount,
            sum(cr.creditValue) as totalCredits,
            count(case when cr.creditValue is null then 1 end) as noCreditCount
        from User u
        join u.courses c
        left join Credit cr on cr.student = u and cr.course = c
        where u.id = :userId
    """)
    Object findCourseCountAndTotalCreditsAndNoCreditCountByUserId(@Param("userId") UUID userId);
}