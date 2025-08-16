package com.userauth.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.NaturalId;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name="roles")
@Getter
@Setter
@Data
public class Role {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @NaturalId
    @Enumerated(EnumType.STRING)
    @Column(length=60)
    private RoleName role;

    @ManyToMany(mappedBy = "roles")
    private Set<User> users=new HashSet<>();

}
