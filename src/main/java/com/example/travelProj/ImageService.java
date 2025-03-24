package com.example.travelProj;

import com.example.travelProj.board.ReviewBoard;
import com.example.travelProj.board.ReviewBoardRepository;
import com.example.travelProj.user.SiteUser;
import com.example.travelProj.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ImageService {

    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private final ReviewBoardRepository reviewBoardRepository;

    private static final String UPLOAD_DIR = "uploads"; // 루트 직접 지정
    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB 제한

    // 프로필 이미지 업로드 메서드
    public String uploadProfileImage(Long userId, MultipartFile file) throws IOException {
        // 사용자 확인
        SiteUser user = userRepository.findById(userId).orElseThrow(() ->
                new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 파일이 비어있는지 확인
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 비어 있습니다.");
        }

        // 파일 유효성 검사
        validateFile(file);

        // 파일 경로 설정 및 저장
        return saveFile(userId, "profile", file);
    }

    // 리뷰 보드 이미지 업로드 메서드
    public String uploadReviewBoardImage(Long reviewBoardId, MultipartFile file) throws IOException {
        // 리뷰 보드 확인
        ReviewBoard reviewBoard = reviewBoardRepository.findById(reviewBoardId).orElseThrow(() ->
                new IllegalArgumentException("리뷰 보드를 찾을 수 없습니다."));

        // 파일 유효성 검사
        validateFile(file);

        // 파일 경로 설정 및 저장
        return saveFile(reviewBoardId, "reviewBoard", file);
    }

    // 공통 파일 저장 메서드
    private String saveFile(Long id, String folderType, MultipartFile file) throws IOException {
        // 고유한 파일명 생성
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

        // uploadDir 경로 설정
        Path uploadPath = Paths.get(UPLOAD_DIR, folderType, String.valueOf(id));

        // 디렉토리가 없으면 생성
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            System.out.println("디렉토리 생성됨: " + uploadPath.toString());
        }

        Path filePath = uploadPath.resolve(fileName); // 파일 경로 생성

        // 파일 저장
        try {
            file.transferTo(filePath.toFile());
        } catch (IOException e) {
            System.err.println("파일 저장 중 오류 발생: " + e.getMessage());
            throw new RuntimeException("파일 저장 중 오류가 발생했습니다: " + e.getMessage());
        }

        // 저장 후 확인
        if (!Files.exists(filePath)) {
            throw new RuntimeException("파일이 저장되지 않았습니다: " + filePath.toString());
        }

        // 새로운 프로필 이미지 정보 객체 생성
        Image profileImage = new Image();
        profileImage.setFilename(file.getOriginalFilename());
        profileImage.setFilepath(filePath.toString());
        profileImage.setSiteUser(profileImage.getSiteUser());

        // 데이터베이스에 저장
        imageRepository.save(profileImage);

        // 프로필 이미지 URL 반환
        return "/uploads/" + folderType + "/" + id + "/" + fileName; // 요청 URL 형식으로 리턴
    }

    // 파일 유효성 검사 메서드
    private void validateFile(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("파일 이름이 유효하지 않습니다."); // 파일 이름 유효성 검사
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 크기가 너무 큽니다."); // 파일 크기 검사
        }

        // 여기에 MIME 타입 검사 추가 가능 (예: 이미지 파일인지 확인)
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("지원하지 않는 파일 형식입니다. 이미지 파일만 가능합니다.");
        }
    }

}
