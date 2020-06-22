package com.example.CSIA.entity;


import com.example.CSIA.converter.RoleConverter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.lang.NonNull;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.UUID;

@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(allowGetters = true, ignoreUnknown = true)
public class User {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @NotBlank
    @JsonProperty(value = "name")
    private String name;

    @NotBlank
    @NonNull
    @Convert(converter = RoleConverter.class)
    @JsonProperty(value = "role")
    private String role;

    public UUID getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String roleName) {
        this.role = roleName.toUpperCase();
    }

    public void setName(String name) {
        this.name = name;
    }
}