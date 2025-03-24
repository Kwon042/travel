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
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ImageService {

    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private final ReviewBoardRepository reviewBoardRepository;

    @Value("${upload.dir}")
    private String uploadDir;

    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB 제한

    // 프로필 이미지 업로드 메서드
    public String uploadProfileImage(Long userId, MultipartFile file) throws IOException {
        // 사용자 확인
        SiteUser user = userRepository.findById(userId).orElseThrow(() ->
                new IllegalArgumentException("User not found."));

        // 파일이 비어있는지 확인
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("No file uploaded.");
        }

        // 파일 유효성 검사
        validateFile(file);

        // 파일 경로 설정 및 저장
        return saveFile(userId, "profile", file, user);
    }

    // 리뷰 보드 이미지 업로드 메서드
    public String uploadReviewBoardImage(Long reviewBoardId, MultipartFile file) throws IOException {
        // 리뷰 보드 확인
        ReviewBoard reviewBoard = reviewBoardRepository.findById(reviewBoardId).orElseThrow(() ->
                new IllegalArgumentException("ReviewBoard Not Found"));

        // 파일 유효성 검사
        validateFile(file);

        // 파일 경로 설정 및 저장
        return saveFile(reviewBoardId, "reviewBoard", file, null);
    }

    // 공통 파일 저장 메서드
    private String saveFile(Long id, String folderType, MultipartFile file, SiteUser user) throws IOException {
        // 고유한 파일명 생성
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

        // 업로드 디렉토리 경로 설정 (프로젝트 루트에서 상대 경로 사용)
        String projectDir = System.getProperty("user.dir");  // 프로젝트 루트 디렉토리
        String uploadDir = projectDir + "/uploads";  // 업로드 폴더 경로
        Path uploadPath = Paths.get(uploadDir, folderType, String.valueOf(id));

        // 디렉토리가 없으면 생성
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            System.out.println("Created Directory: " + uploadPath.toString());
        }

        // userId로 SiteUser 객체 조회
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        // 기존 이미지를 삭제하는 로직
        if (user.getProfileImage() != null) {
            // 기존 이미지가 있을 경우, 삭제 후 새 이미지 업로드
            Image existingImage = user.getProfileImage();
            Path existingFilePath = Paths.get(existingImage.getFilepath());
            try {
                Files.delete(existingFilePath);  // 파일 삭제
                imageRepository.delete(existingImage);  // DB에서 삭제
                System.out.println("Deleted existing image: " + existingImage.getFilename());
            } catch (IOException e) {
                System.err.println("Error occurred while deleting the file: " + e.getMessage());
                throw new RuntimeException("Error occurred while deleting the existing file: " + e.getMessage());
            }
        }

        Path filePath = uploadPath.resolve(fileName); // 파일 경로 생성

        // 파일 저장
        try {
            file.transferTo(filePath.toFile());
        } catch (IOException e) {
            System.err.println("Error occurred while saving the file: " + e.getMessage());
            throw new RuntimeException("Error occurred while saving the file: " + e.getMessage());
        }

        // 저장 후 확인
        if (!Files.exists(filePath)) {
            throw new RuntimeException("The file was not saved: " + filePath.toString());
        }

        // 새로운 프로필 이미지 정보 객체 생성
        Image profileImage = new Image();
        profileImage.setFilename(file.getOriginalFilename());
        profileImage.setFilepath(filePath.toString());
        profileImage.setSiteUser(user);  // 올바른 사용자 정보 설정
        profileImage.setUrl("/uploads/profile/" + id + "/" + file.getOriginalFilename());

        // 데이터베이스에 저장
        imageRepository.save(profileImage);

        // 프로필 이미지 URL 반환
        return "/uploads/" + folderType + "/" + id + "/" + fileName; // 요청 URL 형식으로 리턴
    }

    // 파일 유효성 검사 메서드
    private void validateFile(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("The file name is invalid.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("The file size is too large.");
        }

        // 여기에 MIME 타입 검사 추가 가능 (예: 이미지 파일인지 확인)
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Unsupported file type. Only image files are allowed.");
        }
    }

}
