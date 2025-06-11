package com.example.uai.models.DTO;
import java.util.UUID;

import lombok.Data;

@Data
public class StudentCredit {
    UUID id;
    UUID studentId;
    String userName;
    UUID courseId;
    String courseName;
    Double creditValue;

    public StudentCredit(UUID id,
    String userName,
    UUID courseId,
    UUID studentId,
    String courseName,
    Double creditValue) {
        this.id = id;
        this.userName = userName;
        this.courseName = courseName;
        this.creditValue = creditValue;
        this.studentId = studentId;
        this.courseId = courseId;
    }
}
