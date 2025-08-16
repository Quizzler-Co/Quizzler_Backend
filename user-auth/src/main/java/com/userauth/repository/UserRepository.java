package com.userauth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.userauth.domain.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameOrEmail(String userName, String email);

    Optional<User> findByUsername(String userName);

    // List<User> findByIds(List<Long> ids);

    Boolean existsByEmail(String email);

    Boolean existsByUsername(String userName);

}
