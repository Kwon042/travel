package com.example.travelProj.domain.image.imageuser;

import com.example.travelProj.domain.user.SiteUser;
import com.example.travelProj.domain.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class ImageUserService {

    private final ImageUserRepository imageUserRepository;
    private final UserRepository userRepository;

    @Transactional
    public String uploadProfileImage(Long userId, MultipartFile file) throws IOException {
        SiteUser user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        // ImageUser 객체 생성
        ImageUser imageUser = new ImageUser();
        imageUser.setFilename(file.getOriginalFilename());

        // 파일 저장 경로 설정
        String directory = "uploads/profile/" + userId; // 업로드 할 디렉토리
        Path uploadPath = Paths.get(directory);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath); // 디렉토리가 없으면 생성
        }

        // 파일 저장
        Path filePath = uploadPath.resolve(file.getOriginalFilename());
        file.transferTo(filePath.toFile());
        imageUser.setUrl(filePath.toString());

        imageUser.setUser(user);
        imageUserRepository.save(imageUser); // 이미지 정보 저장

        return imageUser.getImageUrl();
    }



}
