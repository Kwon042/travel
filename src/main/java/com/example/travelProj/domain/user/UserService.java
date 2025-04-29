package com.example.travelProj.domain.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final Path profileImagePath = Paths.get(System.getProperty("user.dir"), "uploads/profile_images");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB (5 * 1024 * 1024 바이트)

    @Transactional
    private SiteUser buildUser(String username, String email, String password, String nickname, UserRole role) {
        SiteUser user = new SiteUser();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(nickname);
        user.setRole(role);
        return user;
    }

    @Transactional
    public SiteUser create(String username, String email, String password, String nickname, String role) {
        return userRepository.save(buildUser(username, email, password, nickname, UserRole.valueOf(role)));
    }

    @Transactional
    public SiteUser createAdmin(String username, String email, String password, String nickname, String role) {
        return userRepository.save(buildUser(username, email, password, nickname, UserRole.ADMIN));
    }

    // 닉네임 수정
    @Transactional
    public void updateNickname(Long userId, String newNickname) {
        if (existsByNickname(newNickname)) {
            throw new IllegalArgumentException("이미 등록된 닉네임입니다.");
        }
        SiteUser user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        user.setNickname(newNickname);
        userRepository.save(user);
    }

    // 이메일 수정
    @Transactional
    public void updateEmail(Long userId, String newEmail) {
        // 중복 체크
        checkFieldDuplicate("email", newEmail);  // 중복 체크만 수행, 예외가 발생하면 처리됨

        SiteUser user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        user.setEmail(newEmail);
        userRepository.save(user);
    }

    // 중복 체크 공통 메서드
    private void checkFieldDuplicate(String field, String value) {
        if ("username".equals(field) && existsByUsername(value)) {
            throw new IllegalArgumentException("이미 등록된 유저ID입니다.");
        } else if ("nickname".equals(field) && existsByEmail(value)) {
            throw new IllegalArgumentException("이미 등록된 닉네임입니다.");
        } else if ("email".equals(field) && existsByNickname(value)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
    }

    @Transactional
    public SiteUser findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    public void updateUser(SiteUser user) {
        userRepository.save(user);
    }

    public boolean existsByUsername(String username) { return userRepository.existsByEmail(username); }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean existsByNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    // 유저만
    public long countUsers() {  return userRepository.countByRoleNot(UserRole.ADMIN); }
    // 관리자 수를 세는 메서드
    public long countAdmins() { return userRepository.countByRole(UserRole.ADMIN); }

    // 모든 사용자 정보 가져오기
    public Iterable<SiteUser> findAllUsers() { return userRepository.findAll(); }
    // 일반 사용자 목록 반환
    public Iterable<SiteUser> findByRole(UserRole role) { return userRepository.findByRole(role); }

}
