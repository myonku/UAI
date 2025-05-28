package com.example.uai.models;
import lombok.Data;
import java.util.UUID;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


@Entity
@Data
@Table(name = "users")
public class User {
    @Id
    public UUID id;
    public String email;
    public String username;
    public String password;
    public String role;
}
