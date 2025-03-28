package com.example.travelProj.domain.image;

import com.example.travelProj.domain.board.ReviewBoard;
import com.example.travelProj.domain.board.ReviewBoardDTO;
import com.example.travelProj.domain.board.ReviewBoardRepository;
import com.example.travelProj.domain.user.SiteUser;
import com.example.travelProj.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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

    @Value("${upload.dir}")
    private String uploadDir;

    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB 제한

    // 프로필 이미지 업로드 메서드
    @Transactional
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

    @Transactional
    public void uploadReviewImage(Long reviewBoardId, ReviewBoardDTO reviewBoardDTO, MultipartFile file) throws IOException {
        // ReviewBoard 객체 가져오기
        ReviewBoard reviewBoard = reviewBoardRepository.findById(reviewBoardId)
                .orElseThrow(() -> new IllegalArgumentException("Review Board not found"));

        // 제목과 내용 업데이트
        reviewBoard.setTitle(reviewBoardDTO.getTitle());
        reviewBoard.setContent(reviewBoardDTO.getContent());

        // 이미지 처리
        if (file != null && !file.isEmpty()) {
            // 기존 메서드 재사용
            String imageUrl = saveFile(reviewBoardId, "review", file, reviewBoard);
            reviewBoardDTO.getImageUrls().add(imageUrl); // 이미지 리스트 업데이트
        }

        // Board 업데이트 및 저장
        reviewBoard.setImageUrls(reviewBoardDTO.getImageUrls()); // 이미지 URL 리스트 설정
        reviewBoardRepository.save(reviewBoard);
    }

    @Transactional
    public String saveFile(Long id, String folderType, MultipartFile file, Object entity) throws IOException {
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

        // 이미지 객체 생성
        Image image = new Image();
        image.setFilename(file.getOriginalFilename());
        image.setFilepath(filePath.toString());
        image.setUrl("/uploads/" + folderType + "/" + id + "/" + fileName);

        // entity가 SiteUser인지 ReviewBoard인지 확인 후 저장
        if (entity instanceof SiteUser) {
            SiteUser user = (SiteUser) entity;
            image.setSiteUser(user);
            user.setProfileImage(image); // 프로필 이미지 설정
        } else if (entity instanceof ReviewBoard) {
            ReviewBoard reviewBoard = (ReviewBoard) entity;
            image.setReviewBoard(reviewBoard);
            reviewBoard.getReview_images().add(image); // 리뷰 이미지 리스트에 추가
        }

        // 데이터베이스에 저장
        imageRepository.save(image);
        System.out.println("Image saved at: " + image.getUrl());

        return image.getUrl();  // 저장된 이미지 URL 반환
    }

    // 파일 유효성 검사 메서드
    @Transactional
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
