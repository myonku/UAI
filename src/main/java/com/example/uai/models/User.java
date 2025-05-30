package com.example.uai.models;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;
import java.util.UUID;


@Entity
@Data
@Table(name = "users")
public class User {
    @Id
    public UUID id;
    public String email;
    public String username;
    public String avatarPath;
    public String password;
    public String role;    // 角色：student, staff, default, admin
    public String status;  // 状态：active, inactive, banned
    @ManyToMany
    @JoinTable(
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    public List<Course> courses;
    @OneToMany(mappedBy = "student")
    public List<Credit> credits;
}
