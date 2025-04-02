package com.example.travelProj.domain.image.imageboard;

import com.example.travelProj.domain.board.ReviewBoard;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ImageBoardService {

    private final ImageBoardRepository imageBoardRepository;

    @Value("${upload.dir}") //
    private String uploadDir;

    // 이미지 저장
    // ✅ 단일 이미지 저장
    public ImageBoard saveImage(MultipartFile file, Long reviewBoardId) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어 있습니다.");
        }

        // 프로필과 동일하게 `uploadDir`을 사용하여 경로 설정
        String directory = uploadDir + "/board/" + reviewBoardId;
        Path uploadPath = Paths.get(directory);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(file.getOriginalFilename());
        file.transferTo(filePath.toFile());

        ImageBoard imageBoard = new ImageBoard();
        imageBoard.setFilename(file.getOriginalFilename());
        imageBoard.setUrl(filePath.toString());

        ReviewBoard reviewBoard = new ReviewBoard();
        reviewBoard.setId(reviewBoardId);
        imageBoard.setReviewBoard(reviewBoard);

        return imageBoardRepository.save(imageBoard);
    }

    // ✅ 다중 이미지 저장
    public void saveImages(List<MultipartFile> files, Long reviewBoardId) throws IOException {
        for (MultipartFile file : files) {
            saveImage(file, reviewBoardId);
        }
    }

    // 이미지 수정 (추가, 삭제)
    @Transactional
    public void updateImage(Long imageId, MultipartFile file) throws IOException {
        ImageBoard imageBoard = imageBoardRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Image not found"));

        // 새로운 파일 저장 로직
        if (file != null && !file.isEmpty()) {
            // 기존 파일 삭제 로직
            Path existingFilePath = Paths.get(imageBoard.getFilepath());
            if (Files.exists(existingFilePath)) {
                Files.delete(existingFilePath); // 기존 파일 삭제
            }

            // 새로운 파일 처리
            imageBoard.setFilename(file.getOriginalFilename());

            // 리뷰 보드 ID 가져오기
            Long reviewBoardId = imageBoard.getReviewBoard().getId();

            String directory = "uploads/board/" + reviewBoardId;
            Path uploadPath = Paths.get(directory);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(file.getOriginalFilename());
            file.transferTo(filePath.toFile());
            imageBoard.setUrl(filePath.toString()); // URL 업데이트
        }
        imageBoardRepository.save(imageBoard);
    }

    // 이미지 삭제
    @Transactional
    public void deleteImage(Long imageId) {
        ImageBoard imageBoard = imageBoardRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Image not found"));

        // 파일 삭제 로직
        Path existingFilePath = Paths.get(imageBoard.getFilepath());
        if (Files.exists(existingFilePath)) {
            try {
                Files.delete(existingFilePath);
            } catch (IOException e) {
                throw new RuntimeException("Error deleting the image file: " + e.getMessage());
            }
        }
        imageBoardRepository.delete(imageBoard);
    }
}
