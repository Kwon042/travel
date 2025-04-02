package com.example.travelProj.domain.image.imageboard;

import com.example.travelProj.domain.board.ReviewBoard;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ImageBoardService {

    private final ImageBoardRepository imageBoardRepository;

    // 이미지 저장
    @Transactional
    public void saveImages(List<MultipartFile> files, Long reviewBoardId) throws IOException {
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                ImageBoard imageBoard = new ImageBoard();
                imageBoard.setFilename(file.getOriginalFilename());
                // 파일 저장 경로 설정
                String directory = "uploads/board/" + reviewBoardId; // 경로 설정
                Path uploadPath = Paths.get(directory);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // 파일 경로 및 URL 설정
                Path filePath = uploadPath.resolve(file.getOriginalFilename());
                file.transferTo(filePath.toFile());
                imageBoard.setUrl(filePath.toString()); // URL 경로 설정

                imageBoard.setReviewBoard(new ReviewBoard()); // reviewBoard 설정 필요
                // ImageBoard 저장
                imageBoardRepository.save(imageBoard);
            }
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
