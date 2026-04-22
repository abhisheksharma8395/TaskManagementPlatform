package com.taskmanagement.app.authservice.repository;

import com.taskmanagement.app.authservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    List<User> findAllByRole(String role);
    @Query(value = "select * from users where full_name = :fullName",nativeQuery = true)
    List<User> searchByFullName(@Param("fullName") String fullName);
    void deleteByUserId(Long userId);
}
