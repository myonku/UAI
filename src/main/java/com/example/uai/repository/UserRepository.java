package com.example.uai.repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.example.uai.models.UserDto;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.uai.models.User;
import org.springframework.data.jpa.repository.Query;


public interface UserRepository extends JpaRepository<User, UUID> {

    // 通过用户名查找
    Optional<User> findByUsername(String username);

    // 获取所有用户（排除密码字段）
    @Query("SELECT new com.example.uai.models.UserDto(u.id, u.username, u.email, u.role, u.avatarPath, u.status) FROM User u")
    List<UserDto> findAllWithoutPassword();

    // 判断邮箱是否已存在
    boolean existsByEmail(String email);

    // 判断用户名是否存在
    boolean existsByUsername(String username);

    // 通过邮箱查找用户（排除密码字段，返回UserDTO）
    @Query("SELECT new com.example.uai.models.UserDto(u.id, u.username, u.email, u.role, u.avatarPath, u.status) FROM User u WHERE u.email = :email")
    Optional<UserDto> findDTOByEmail(String email);

    // 通过用户id查找用户（排除密码字段，返回UserDTO）
    @Query("SELECT new com.example.uai.models.UserDto(u.id, u.username, u.email, u.role, u.avatarPath, u.status) FROM User u WHERE u.id = :id")
    Optional<UserDto> findDTOById(UUID id);

}