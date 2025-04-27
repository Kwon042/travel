package com.example.travelProj.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<SiteUser, Long> {
    Optional<SiteUser> findByUsername(String username);

    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname); // 닉네임 중복 확인

    Iterable<SiteUser> findByRole(UserRole role);
    // 관리자 제외한 총 사용자 수
    long countByRoleNot(UserRole role);
    // 관리자 수를 세는 메서드 (관리자만)
    long countByRole(UserRole role);

}
