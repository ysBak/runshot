package com.runshot.user.repository;

import com.runshot.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // 기존 회원가입용 이메일 중복 검사
    boolean existsByEmail(String email);

    // 로그인용 이메일 검색
    Optional<User> findByEmail(String email);
}