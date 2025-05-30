package com.example.uai.models;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
import java.util.UUID;


@Data
@Entity
@Table(name = "courses")
public class Course {
    @Id
    public UUID id;
    public String name;
    public String description;
    @ManyToMany(mappedBy = "courses")
    public List<User> users;
    @OneToMany(mappedBy = "course")
    public List<Credit> credits;
}
