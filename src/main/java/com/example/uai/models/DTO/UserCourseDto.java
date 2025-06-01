package com.example.uai.models.DTO;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class UserCourseDto {
    public UUID userId;
    public List<UUID> courseIds;
}
