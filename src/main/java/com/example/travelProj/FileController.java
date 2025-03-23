package com.example.travelProj;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
public class FileController {

    private final String uploadDir = "/uploads"; // 정적 파일 경로 변경

    @GetMapping("/{folderType}/{userId}/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String folderType,
                                              @PathVariable Long userId,
                                              @PathVariable String filename) {
        try {
            // 업로드 디렉토리 경로를 동적으로 설정
            Path filePath = Paths.get(uploadDir, folderType, String.valueOf(userId), filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            // 파일 존재 여부 확인
            if (!resource.exists() || !resource.isReadable()) {
                throw new RuntimeException("파일을 찾을 수 없습니다.");
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build(); // 파일이 없거나 읽을 수 없는 경우 404 반환
        }
    }
}