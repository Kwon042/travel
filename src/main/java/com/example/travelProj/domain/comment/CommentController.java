package com.example.travelProj.domain.comment;

import com.example.travelProj.domain.user.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comments")
public class CommentController {
    private final CommentService commentService;

    // 댓글 추가
    @PostMapping("/{reviewBoardId}")
    public ResponseEntity<?> addComment(@PathVariable Long reviewBoardId,
                                        @AuthenticationPrincipal SiteUser user,
                                        @RequestParam String content) {
        commentService.addComment(reviewBoardId, user, content);
        return ResponseEntity.ok("Comment added successfully.");
    }

    // 댓글 조회
    @GetMapping("/{reviewBoardId}")
    public ResponseEntity<List<Comment>> getComments(@PathVariable Long reviewBoardId) {
        List<Comment> comments = commentService.getComments(reviewBoardId);
        return ResponseEntity.ok(comments);
    }

    // 댓글 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.ok("Comment removed successfully.");
    }
}
