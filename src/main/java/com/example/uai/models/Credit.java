package com.example.uai.models;
import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;


@Entity
@Data
@Table(name = "credits")
public class Credit {
    @Id
    public UUID id;
    @ManyToOne
    @JoinColumn(name = "student_id", referencedColumnName = "id")
    public User student;
    @ManyToOne
    @JoinColumn(name = "course_id", referencedColumnName = "id")
    public Course course;
    public Double creditValue;
}
