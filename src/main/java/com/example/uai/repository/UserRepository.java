package com.example.uai.repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.uai.models.User;



public interface UserRepository extends JpaRepository<User, UUID> {
    // 通过用户名查找用户
    Optional<User> findByUsername(String username);

    // 判断邮箱是否已存在
    boolean existsByEmail(String email);

    // 通过邮箱查找用户
    Optional<User> findByEmail(String email);

    // 删除指定用户名的用户
    void deleteByUsername(String username);

}