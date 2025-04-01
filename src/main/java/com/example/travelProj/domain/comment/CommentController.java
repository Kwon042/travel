package com.example.travelProj.domain.comment;

import com.example.travelProj.domain.user.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comments")
public class CommentController {
    private final CommentService commentService;

    @GetMapping("/{reviewBoardId}/view")
    public String showComment(@PathVariable Long reviewBoardId, Model model) {
        Map<String, Object> commentsWithCount = commentService.getCommentsWithCount(reviewBoardId);

        model.addAttribute("comments", commentsWithCount.get("comments"));
        model.addAttribute("commentsCount", commentsWithCount.get("commentsCount"));
        //model.addAttribute("likeCount", commentsWithCount.get("likeCount"));
        model.addAttribute("reviewBoardId", reviewBoardId);

        return "board/comment";
    }

    // 댓글 추가
    @PostMapping("/{reviewBoardId}")
    public ResponseEntity<?> addComment(@PathVariable Long reviewBoardId,
                                        @AuthenticationPrincipal SiteUser user,
                                        @RequestParam String content) {
        commentService.addComment(reviewBoardId, user, content);
        return ResponseEntity.ok("Comment added successfully.");
    }

    // 댓글 조회 (목록과 수 반환)
    @GetMapping("/{reviewBoardId}")
    public ResponseEntity<Map<String, Object>> getComments(@PathVariable Long reviewBoardId) {
        Map<String, Object> commentsWithCount = commentService.getCommentsWithCount(reviewBoardId);
        return ResponseEntity.ok(commentsWithCount);
    }

    // 댓글 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.ok("Comment removed successfully.");
    }
}
