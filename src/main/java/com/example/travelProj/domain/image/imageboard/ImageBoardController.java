package com.example.travelProj.domain.image.imageboard;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/image/board")
public class ImageBoardController {

    private final ImageBoardService imageBoardService;

    // 이미지 업로드
    @PostMapping("/upload")
    public ResponseEntity<?> uploadImages(@RequestParam("files[]") List<MultipartFile> files,
                                          @RequestParam("reviewBoardId") Long reviewBoardId,
                                          @RequestParam(value = "existingImageIds", required = false) List<Long> existingImageIds) {
        System.out.println("uploadImages 호출됨 - reviewBoardId: " + reviewBoardId + ", files 수: " + files.size() + ", existingImageIds: " + (existingImageIds != null ? existingImageIds.size() : "null"));

        try {
            // 기존 이미지를 가져오기
            List<ImageBoard> existingImages = new ArrayList<>();
            if (existingImageIds != null) {
                for (Long imageId : existingImageIds) {
                    ImageBoard existingImage = imageBoardService.getImageById(imageId); // 이미지 가져오기 메서드 추가 필요
                    existingImages.add(existingImage);
                }
            }

            // 새 이미지를 추가하면서 기존 이미지를 유지하는 방식
            List<ImageBoard> allImages = imageBoardService.saveImages(files, reviewBoardId, existingImages);

            // 이미지 URL 리스트 처리
            List<String> imageUrls = allImages.stream()
                    .map(ImageBoard::getUrl)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of("success", true, "message", "Images have been uploaded successfully.", "imageUrls", imageUrls));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // 이미지 수정
    @PutMapping("/{imageId}")
    public ResponseEntity<?> updateImage(@PathVariable Long imageId,
                                         @RequestParam("file") MultipartFile file) {
        try {
            imageBoardService.updateImage(imageId, file);
            return ResponseEntity.ok(Map.of("success", true, "message", "Image has been updated successfully."));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // 이미지 삭제
    @DeleteMapping("/{imageId}")
    public ResponseEntity<?> deleteImage(@PathVariable Long imageId) {
        try {
            imageBoardService.deleteImage(imageId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Image has been deleted successfully."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "Error deleting image."));
        }
    }

}
