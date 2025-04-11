package com.example.travelProj.domain.comment;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class CommentResponseDTO {
    private Long id;
    private String username;
    private String nickname;
    private String content;
    private LocalDateTime createdAt;
    private String profileImageUrl;
    private int likesCount;
    private List<CommentResponseDTO> children = new ArrayList<>();
}
