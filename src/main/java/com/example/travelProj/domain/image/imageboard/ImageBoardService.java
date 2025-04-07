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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ImageBoardService {

    private final ImageBoardRepository imageBoardRepository;

    private final String uploadDir = System.getProperty("user.dir") + "/uploads";

    // 단일 이미지 저장
    public ImageBoard saveImage(MultipartFile file, Long reviewBoardId) throws IOException {
        if (file == null || file.isEmpty()) {
            return null; // 이미지 없이도 저장 허용
        }

        // 프로필과 동일하게 `uploadDir`을 사용하여 경로 설정
        String directory = uploadDir + "/board/" + reviewBoardId;
        Path uploadPath = Paths.get(directory);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 원본 파일명과 확장자 추출
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String baseFilename = originalFilename.substring(0, originalFilename.lastIndexOf("."));

        // UUID + 원본 파일명으로 새로운 파일명 생성
        String newFileName = UUID.randomUUID() + "_" + baseFilename + fileExtension;

        Path filePath = uploadPath.resolve(newFileName);
        file.transferTo(filePath.toFile()); // 파일 저장

        ImageBoard imageBoard = new ImageBoard();
        imageBoard.setFilename(newFileName);

        // 상대 경로만 저장
        String relativePath = "board/" + reviewBoardId + "/" + newFileName;
        imageBoard.setUrl(relativePath);

        ReviewBoard reviewBoard = new ReviewBoard();
        reviewBoard.setId(reviewBoardId);
        imageBoard.setReviewBoard(reviewBoard);

        System.out.println("Saving image for ReviewBoard ID: " + reviewBoardId + " with filename: " + newFileName);
        return imageBoardRepository.save(imageBoard);
    }

    // 다중 이미지 저장 (기존 이미지 유지) > 글 수정할 떄
    public List<ImageBoard> saveImages(List<MultipartFile> files, Long reviewBoardId, List<ImageBoard> existingImages) throws IOException {
        List<ImageBoard> imageBoards = new ArrayList<>(existingImages); // 기존 이미지 유지
        for (MultipartFile file : files) {
            ImageBoard imageBoard = saveImage(file, reviewBoardId); // 새 이미지 저장
            imageBoards.add(imageBoard); // 새 이미지를 리스트에 추가
        }
        return imageBoards; // 업데이트된 이미지 리스트 반환
    }

    // 기존 이미지 없이 새 이미지만 저장하는 메서드 > 새로운 글 작성할 때
    public List<ImageBoard> saveNewImages(List<MultipartFile> files, Long reviewBoardId) throws IOException {
        List<ImageBoard> imageBoards = new ArrayList<>();
        for (MultipartFile file : files) {
            ImageBoard imageBoard = saveImage(file, reviewBoardId);
            imageBoards.add(imageBoard); // 새 이미지 추가
        }
        return imageBoards; // 리스트 반환
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
                Files.delete(existingFilePath);
            }

            // 새로운 파일 저장 로직
            Long reviewBoardId = imageBoard.getReviewBoard().getId();
            String directory = uploadDir + "/board/" + reviewBoardId;
            Path uploadPath = Paths.get(directory);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            String newFileName = UUID.randomUUID() + fileExtension;

            Path newFilePath = uploadPath.resolve(newFileName);
            file.transferTo(newFilePath.toFile());

            imageBoard.setFilename(newFileName);
            String relativePath = "board/" + reviewBoardId + "/" + newFileName;
            imageBoard.setUrl(relativePath);
        }
        imageBoardRepository.save(imageBoard);
    }

    // 이미지 삭제
    @Transactional
    public void deleteImage(Long imageId) {
        ImageBoard imageBoard = imageBoardRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Image not found"));

        // 파일 삭제 로직
        Path existingFilePath = Paths.get(uploadDir + "/" + imageBoard.getUrl());
        if (Files.exists(existingFilePath)) {
            try {
                Files.delete(existingFilePath);
            } catch (IOException e) {
                throw new RuntimeException("Error deleting the image file: " + e.getMessage());
            }
        }
        imageBoardRepository.delete(imageBoard);
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ""; // 확장자가 없는 경우 빈 문자열 반환
        }
        return filename.substring(filename.lastIndexOf(".")); // 마지막 점 이후의 문자열을 확장자로 반환
    }

    public ImageBoard getImageById(Long imageId) {
        return imageBoardRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Image not found"));
    }
}
