package com.dku.council.domain.user.repository;

import com.dku.council.domain.user.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByStudentId(String studentId);
    Optional<User> findByPhone(String phone);
    Optional<User> findByNickname(String nickname);
}
