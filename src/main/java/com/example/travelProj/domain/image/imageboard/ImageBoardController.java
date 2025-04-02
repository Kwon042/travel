package com.example.travelProj.domain.image.imageboard;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/image/board")
public class ImageBoardController {

    private final ImageBoardService imageBoardService;

    // 이미지 업로드
    @PostMapping("/upload")
    public ResponseEntity<?> uploadImages(@RequestParam("files") List<MultipartFile> files,
                                          @RequestParam("reviewBoardId") Long reviewBoardId) {
        try {
            imageBoardService.saveImages(files, reviewBoardId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Images have been uploaded successfully."));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // 이미지 업데이트
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
