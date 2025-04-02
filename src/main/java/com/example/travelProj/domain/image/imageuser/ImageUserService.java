package com.example.travelProj.domain.image.imageuser;

import com.example.travelProj.domain.user.SiteUser;
import com.example.travelProj.domain.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageUserService {

    private final ImageUserRepository imageUserRepository;
    private final UserRepository userRepository;

    private final String uploadDir = System.getProperty("user.dir") + "/uploads";

    @Transactional
    public String uploadProfileImage(Long userId, MultipartFile file) throws IOException {
        SiteUser user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        if (file.isEmpty()) {
            throw new IllegalArgumentException("There are no uploaded files.");
        }

        // 기존 이미지 삭제 처리
        Optional<ImageUser> existingImageOpt = imageUserRepository.findByUser(user);
        existingImageOpt.ifPresent(existingImage -> {
            // 파일 삭제
            Path oldImagePath = Paths.get(uploadDir, "profile", String.valueOf(userId), existingImage.getFilename());
            try {
                Files.deleteIfExists(oldImagePath);
            } catch (IOException e) {
                System.err.println("Failed to delete old profile image: " + e.getMessage());
            }
            // 기존 이미지 삭제 (DB에서 삭제 후 flush 실행)
            imageUserRepository.delete(existingImage);
            imageUserRepository.flush(); // 삭제가 즉시 반영되도록 flush() 호출
        });

        // 저장 디렉토리 설정 (상대 경로)
        Path uploadPath = Paths.get(uploadDir, "profile", String.valueOf(userId));
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 원본 파일명과 확장자 추출
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String baseFilename = originalFilename.substring(0, originalFilename.lastIndexOf(".")); // 확장자 제외한 파일명

        // UUID + 원본 파일명으로 새로운 파일명 생성
        String newFileName = UUID.randomUUID() + "_" + baseFilename + fileExtension;
        
        // 파일 저장
        Path filePath = uploadPath.resolve(newFileName);
        file.transferTo(filePath.toFile());

        // 상대 경로
        String imageUrl = "/uploads/profile/" + userId + "/" + newFileName;
        System.out.println("Saved image URL: " + imageUrl);

        if (existingImageOpt.isPresent()) {
            // 기존 데이터가 있으면 업데이트
            ImageUser existingImage = existingImageOpt.get();
            existingImage.setFilename(newFileName);
            existingImage.setUrl(imageUrl);
            existingImage.setFilepath(filePath.toString());
            imageUserRepository.save(existingImage);
        } else {
            // 없으면 새로 저장
            ImageUser imageUser = new ImageUser();
            imageUser.setFilename(newFileName);
            imageUser.setUrl(imageUrl);
            imageUser.setFilepath(filePath.toString());
            imageUser.setUser(user);
            imageUserRepository.save(imageUser);
        }

        return imageUrl;
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ""; // 확장자가 없는 경우 빈 문자열 반환
        }
        return filename.substring(filename.lastIndexOf(".")); // 마지막 점 이후의 문자열을 확장자로 반환
    }

}
