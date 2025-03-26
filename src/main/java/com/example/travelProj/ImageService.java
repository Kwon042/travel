package com.example.travelProj;

import com.example.travelProj.board.ReviewBoard;
import com.example.travelProj.board.ReviewBoardDTO;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ImageService {

    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private final ImageService imageService;
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

        // 기존 이미지 조회
        Image existingImage = user.getProfileImage();

        // 기존 이미지가 있는 경우 삭제
        if (existingImage != null) {
            Path existingFilePath = Paths.get(existingImage.getFilepath());
            try {
                Files.deleteIfExists(existingFilePath); // 기존 파일 삭제
                imageRepository.delete(existingImage);  // DB에서 삭제
            } catch (IOException e) {
                throw new RuntimeException("Error occurred while deleting the existing file: " + e.getMessage());
            }
        }

        // 파일 경로 설정 및 저장
        return saveFile(userId, "profile", file, user);
    }

    public void updateReviewBoard(Long reviewBoardId, ReviewBoardDTO boardDTO, MultipartFile file) throws IOException {
        // ReviewBoard 객체 가져오기
        ReviewBoard reviewBoard = reviewBoardRepository.findById(reviewBoardId)
                .orElseThrow(() -> new IllegalArgumentException("Review Board not found"));

        // 제목과 내용 업데이트
        reviewBoard.setTitle(boardDTO.getTitle());
        reviewBoard.setContent(boardDTO.getContent());

        // 이미지 처리
        if (file != null && !file.isEmpty()) {
            // 이미지가 첨부된 경우
            String imageUrl = imageService.uploadProfileImage(reviewBoardId, file); // 프로필 이미지 업로드 로직 호출
            List<String> updatedImageUrls = new ArrayList<>(boardDTO.getImageUrls());
            updatedImageUrls.add(imageUrl);
            boardDTO.setImageUrls(updatedImageUrls);
        }

        // Board 업데이트 및 저장
        reviewBoard.setImageUrls(boardDTO.getImageUrls()); // 이미지 URL 리스트 설정
        reviewBoardRepository.save(reviewBoard);
    }

    private String saveFile(Long id, String folderType, MultipartFile file, SiteUser user) throws IOException {
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

        // 사용자의 업로드 경로 설정
        String projectDir = System.getProperty("user.dir");
        Path uploadPath = Paths.get(projectDir + "/uploads", folderType, String.valueOf(id));

        // 디렉토리가 없으면 생성
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 파일 저장
        Path filePath = uploadPath.resolve(fileName);
        file.transferTo(filePath.toFile());

        // 새로운 프로필 이미지 정보 객체 생성
        Image profileImage = new Image();
        profileImage.setFilename(file.getOriginalFilename());
        profileImage.setFilepath(filePath.toString());
        profileImage.setSiteUser(user);
        // 프로필 이미지 URL 설정 (외부 URL)
        profileImage.setUrl("/uploads/" + folderType + "/" + id + "/" + fileName);

        // 데이터베이스에 저장
        imageRepository.save(profileImage);
        System.out.println("Profile image saved at: " + profileImage.getUrl());

        return profileImage.getUrl();  // 저장된 프로필 이미지 URL 반환
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
