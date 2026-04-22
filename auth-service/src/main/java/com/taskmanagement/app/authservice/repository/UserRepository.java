package com.taskmanagement.app.authservice.repository;

import com.taskmanagement.app.authservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Integer> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String UserName);
    User findByUserId(Integer userId);
    boolean existsByEmail(String email);
    List<User> findAllByRole(String role);
    List<User> searchByFullName(String fullName);
    void deleteByUserId(Integer userId);
}
