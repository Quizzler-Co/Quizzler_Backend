package com.userauth.domain;

import java.util.HashSet;
import java.util.Set;

import lombok.*;
import org.hibernate.annotations.NaturalId;

import com.userauth.domain.base.DateAudit;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "users", uniqueConstraints = {
                @UniqueConstraint(columnNames = {
                                "username"
                }),
                @UniqueConstraint(columnNames = {
                                "email"
                })
})
@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString(exclude = { "password", "roles" })
@EqualsAndHashCode(of = { "id", "username", "email" }, callSuper = true)
public class User extends DateAudit {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @NotBlank
        @Size(max = 40, message = "You can use only {max} characters for name")
        private String name;

        @NotBlank
        @Size(max = 15, message = "You can use only {max} characters for username")
        private String username;

        // The @NaturalId annotation is used to indicate that the email field is a
        // natural identifier for the User entity.
        // In this context, email is unique and can be used to look up users
        // efficiently.
        // Example use case: finding a user by their email address.
        @NaturalId
        @NotBlank
        @Size(max = 50, message = "You can use only {max} characters for email")
        @Email
        private String email;

        @NotBlank
        @Size(max = 100)
        private String password;

        @ManyToMany(fetch = FetchType.LAZY)
        @JoinTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
        private Set<Role> roles = new HashSet<>();

}
