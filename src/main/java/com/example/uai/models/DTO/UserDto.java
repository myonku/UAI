package com.example.uai.models.DTO;

import lombok.Data;
import java.util.UUID;


@Data
public class UserDto {
    private UUID id;
    private String username;
    private String email;
    private String role;
    private String avatarPath;
    private String status;
    public Long courseNum;
    public Double totalScore;
    public Long noCreditCourseNum;

    public UserDto(UUID id, String username, String email, String role, String avatarPath, String status) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.avatarPath = avatarPath;
        this.status = status;
    }

}