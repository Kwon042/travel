package com.example.travelProj.domain.comment;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Data
public class CommentResponseDTO {
    private Long id;
    private String username;
    private String nickname;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String profileImageUrl;
    private Boolean likeStatus;
    private Long likesCount;
    private List<CommentResponseDTO> children = new ArrayList<>();


}
