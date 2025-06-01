package com.example.uai.models.DTO;
import lombok.Data;
import java.util.UUID;

@Data
public class CourseWithCreditDto {
    private UUID id;
    private String name;
    private String description;
    private Double creditValue;

    // 构造函数
    public CourseWithCreditDto(UUID id, String name, String description,
                               Double creditValue) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.creditValue = creditValue;
    }
}
