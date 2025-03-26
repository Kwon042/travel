package com.example.travelProj.board;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ReviewBoardDTO {

    private Long id;
    private String title;
    private String nickname;
    private String content;
    private String regionName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long hit;
    private Long likeCount;
    private List<Long> commentIds;
    private List<Long> imageIds;
    private List<String> imageUrls = new ArrayList<>(); // 빈 리스트로 초기화

}
