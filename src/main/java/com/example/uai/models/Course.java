package com.example.uai.models;
import lombok.Data;
import java.util.UUID;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


@Data
@Entity
@Table(name = "courses")
public class Course {
    @Id
    public UUID id;
    public String name;
    public String credit;
    public String teacher_id;
    public String desc;
}
