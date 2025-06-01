package com.example.uai.models.DTO;

import lombok.Data;

import java.util.UUID;

@Data
public class SimpleCourseDto {
    private UUID id;
    private String name;
    private String description;

    // 构造函数
    public SimpleCourseDto(UUID id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

}