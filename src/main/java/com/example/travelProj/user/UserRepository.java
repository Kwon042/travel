package com.example.travelProj.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<SiteUser, Long> {
    Optional<SiteUser> findByUsername(String username);

    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname); // 닉네임 중복 확인


}
