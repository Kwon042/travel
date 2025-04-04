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
        SiteUser user = getUserById(userId);

        validateFile(file); // 파일 유효성 검사

        // 기존 이미지 삭제 처리
        deleteExistingProfileImageIfPresent(user);

        // 파일 저장
        String newFileName = saveNewProfileImage(file, userId);

        // 이미지 URL 설정
        String imageUrl = createImageUrl(userId, newFileName);

        // 이미지 정보 저장 또는 업데이트
        saveOrUpdateImageRecord(user, newFileName, imageUrl);

        return imageUrl;
    }

    private SiteUser getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("There are no uploaded files.");
        }
    }

    private void deleteExistingProfileImageIfPresent(SiteUser user) {
        Optional<ImageUser> existingImageOpt = imageUserRepository.findByUser(user);
        existingImageOpt.ifPresent(existingImage -> {
            // 파일 삭제
            deleteFile(existingImage.getFilename(), user.getId());
            // 기존 이미지 삭제
            imageUserRepository.delete(existingImage);
            imageUserRepository.flush(); // 삭제가 즉시 반영되도록 flush() 호출
        });
    }

    private void deleteFile(String filename, Long userId) {
        Path oldImagePath = Paths.get(uploadDir, "profile", String.valueOf(userId), filename);
        try {
            Files.deleteIfExists(oldImagePath);
        } catch (IOException e) {
            System.err.println("Failed to delete old profile image: " + e.getMessage());
        }
    }

    private String saveNewProfileImage(MultipartFile file, Long userId) throws IOException {
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

        return newFileName;
    }

    private String createImageUrl(Long userId, String newFileName) {
        return "/uploads/profile/" + userId + "/" + newFileName;
    }

    private void saveOrUpdateImageRecord(SiteUser user, String newFileName, String imageUrl) {
        Optional<ImageUser> existingImageOpt = imageUserRepository.findByUser(user);
        if (existingImageOpt.isPresent()) {
            // 기존 데이터가 있으면 업데이트
            ImageUser existingImage = existingImageOpt.get();
            existingImage.setFilename(newFileName);
            existingImage.setUrl(imageUrl);
            existingImage.setFilepath(Paths.get(uploadDir, "profile", String.valueOf(user.getId()), newFileName).toString());
            imageUserRepository.save(existingImage);
        } else {
            // 없으면 새로 저장
            ImageUser imageUser = new ImageUser();
            imageUser.setFilename(newFileName);
            imageUser.setUrl(imageUrl);
            imageUser.setFilepath(Paths.get(uploadDir, "profile", String.valueOf(user.getId()), newFileName).toString());
            imageUser.setUser(user);
            imageUserRepository.save(imageUser);
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ""; // 확장자가 없는 경우 빈 문자열 반환
        }
        return filename.substring(filename.lastIndexOf(".")); // 마지막 점 이후의 문자열을 확장자로 반환
    }

}
