package com.example.travelProj;

import com.example.travelProj.user.SiteUser;
import com.example.travelProj.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ImageService {

    @Autowired
    private UserRepository userRepository;

    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB 제한

    // 파일 업로드 메서드
    public String uploadFile(Long userId, MultipartFile file, String folderType) throws IOException {
        // 사용자 확인
        SiteUser user = userRepository.findById(userId).orElseThrow(() ->
                new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 파일 유효성 검사
        validateFile(file);

        // 업로드 디렉토리 설정
        Path filePath = prepareUploadDirectory(userId, folderType, file);

        // 파일 저장
        file.transferTo(filePath.toFile());

        // 파일 URL 생성
        return "/uploads/" + folderType + "/" + userId + "/" + filePath.getFileName().toString();
    }

    // 파일 유효성 검사
    private void validateFile(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("파일 이름이 유효하지 않습니다.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 크기가 너무 큽니다.");
        }
    }

    // 업로드 디렉토리 준비
    private Path prepareUploadDirectory(Long userId, String folderType, MultipartFile file) throws IOException {
        String basePath = "uploads/" + folderType;
        Path uploadPath = Paths.get(basePath, String.valueOf(userId)); // 유저 ID별로 구분하여 폴더 생성

        // 디렉토리가 없으면 생성
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 고유한 파일명 생성
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        return uploadPath.resolve(fileName);
    }
}
