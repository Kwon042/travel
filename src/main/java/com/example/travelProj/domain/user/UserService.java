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
    public SiteUser create(String username, String email, String password, String nickname) {
        SiteUser user = new SiteUser();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(nickname);

        this.userRepository.save(user);
        return user;
    }

    @Transactional
    public SiteUser authenticate(String username, String password) {
        Optional<SiteUser> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isPresent()) {
            SiteUser user = optionalUser.get();
            // 비밀번호 매칭 확인
            if (passwordEncoder.matches(password, user.getPassword())) {
                return user;
            }
        }
        return null; // 인증 실패 시 null 리턴
    }

    // 닉네임 수정
    @Transactional
    public void updateNickname(Long userId, String newNickname) {
        // 중복 체크
        String duplicateMessage = checkFieldDuplicate("nickname", newNickname);
        if (duplicateMessage != null) {
            throw new IllegalArgumentException(duplicateMessage);
        }

        SiteUser user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        user.setNickname(newNickname);
        userRepository.save(user);
    }

    // 이메일 수정
    @Transactional
    public void updateEmail(Long userId, String newEmail) {
        // 중복 체크
        String duplicateMessage = checkFieldDuplicate("email", newEmail);
        if (duplicateMessage != null) {
            throw new IllegalArgumentException(duplicateMessage);
        }

        SiteUser user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        user.setEmail(newEmail);
        userRepository.save(user);
    }

    // 중복 체크 공통 메서드
    @Transactional
    private String checkFieldDuplicate(String field, String value) {
        if ("nickname".equals(field)) {
            boolean nicknameExists = isNicknameAlreadyRegistered(value);
            if (nicknameExists) {
                throw new IllegalArgumentException("이미 등록된 닉네임입니다.");
            }
        } else if ("email".equals(field)) {
            boolean emailExists = isEmailAlreadyRegistered(value);
            if (emailExists) {
                throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
            }
        }
        return null;
    }

    public SiteUser getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));
    }

    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    public void updateUser(SiteUser user) {
        userRepository.save(user);
    }

    public boolean isEmailAlreadyRegistered(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean isNicknameAlreadyRegistered(String nickname) {
        return userRepository.existsByNickname(nickname);
    }


}
