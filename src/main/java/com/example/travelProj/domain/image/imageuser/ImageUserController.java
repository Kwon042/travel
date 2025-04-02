package com.example.travelProj.domain.image.imageuser;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/image/user")
public class ImageUserController {

    private final ImageUserService imageUserService;

    @PostMapping("/uploadProfileImage")
    public ResponseEntity<?> uploadProfileImage(@RequestParam("profileImage") MultipartFile file,
                                                @RequestParam("userId") Long userId) {

        try {
            String imageUrl = imageUserService.uploadProfileImage(userId, file); // 이미지 업로드 처리
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "The profile image has been successfully uploaded.",
                    "newProfileImageUrl", imageUrl // 새 프로필 이미지 URL 반환
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "An error occurred during file upload."));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "An unexpected error occurred."));
        }
    }
}
