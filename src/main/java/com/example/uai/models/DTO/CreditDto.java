package com.example.uai.models.DTO;
import lombok.Data;
import java.util.UUID;

@Data
public class CreditDto {
    UUID studentId;
    UUID courseId;
    Double creditValue;

    public CreditDto(UUID studentId, UUID courseId, Double creditValue) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.creditValue = creditValue;
    }
}
