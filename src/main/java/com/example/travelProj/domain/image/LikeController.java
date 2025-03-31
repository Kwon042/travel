package com.example.travelProj.domain.image;

import com.example.travelProj.domain.board.ReviewBoard;
import com.example.travelProj.domain.board.ReviewBoardService;
import com.example.travelProj.domain.like.Like;
import com.example.travelProj.domain.like.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@RequiredArgsConstructor
public class LikeController {

    final private LikeService likeService;
    final private ReviewBoardService reviewBoardService;

    // 좋아요 추가 기능
    @PostMapping("/like")
    public ResponseEntity<Void> addLike(@RequestBody Like like) {
        likeService.addLike(like);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/reviewBoard/{id}/likes")
    public ResponseEntity<Long> getReviewBoardLikes(@PathVariable Long id) {
        ReviewBoard reviewBoard = reviewBoardService.getBoardById(id); // boardService를 통해 게시물 정보 얻기
        long count = likeService.countLikesForReviewBoard(reviewBoard); // 게시물의 좋아요 수 계산
        return ResponseEntity.ok(count); // 좋아요 수 반환
    }


}
