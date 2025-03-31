package com.example.travelProj.domain.board;

import com.example.travelProj.domain.region.Region;
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
    private Long userId; // db에 저장 될 어떤 유저가 리뷰 게시판에 게시글을 올렸는지 확인하고 삭제하는
    private String title;
    private String nickname;
    private String content;
    private Region region;
    private String regionName;
    private Long regionId; // 지역 ID 추가
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long hit;
    private Long likeCount;
    private List<Long> commentIds;
    private List<Long> imageIds;
    private List<String> imageUrls = new ArrayList<>(); // 빈 리스트로 초기화

}
