package com.example.travelProj.domain.comment;

import com.example.travelProj.domain.like.commentlike.CommentLikeService;
import com.example.travelProj.domain.user.SiteUser;
import com.example.travelProj.domain.user.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/comments")
public class CommentController {
    private final CommentService commentService;
    private final UserService userService;
    private final CommentLikeService commentLikeService;

    @GetMapping("/{reviewBoardId}/show")
    public String showComment(@PathVariable Long reviewBoardId,
                              @AuthenticationPrincipal SiteUser user,
                              Model model, HttpSession session) {
        model.addAttribute("reviewBoardId", reviewBoardId);
        SiteUser updatedUser = userService.findUserById(user.getId());
        session.setAttribute("user", updatedUser);
        model.addAttribute("user", updatedUser);

        List<CommentResponseDTO> comments = commentService.getCommentsWithReplies(reviewBoardId, user);
        int commentsCount = commentService.countByReviewBoardId(reviewBoardId);

        for (CommentResponseDTO comment : comments) {
            long likeCount = commentLikeService.countLikes(comment.getId()); // 좋아요 수
            comment.setLikesCount(likeCount);
        }

        model.addAttribute("comments", comments);
        model.addAttribute("commentsCount", commentsCount);

        return "board/comment";
    }

    // 댓글 추가
    @PostMapping("/{reviewBoardId}")
    public ResponseEntity<?> addComment(@PathVariable Long reviewBoardId,
                                        @AuthenticationPrincipal SiteUser user,
                                        @RequestParam String content,
                                        @RequestParam(required = false) Long parentId) {
        commentService.addComment(reviewBoardId, user, content, parentId);
        return ResponseEntity.ok("Comment added successfully.");
    }

    // 댓글 조회 (목록  + 대댓글 트리 구조)
    @GetMapping("/{reviewBoardId}")
    public ResponseEntity<List<CommentResponseDTO>> getComments(@PathVariable Long reviewBoardId,
                                                                @AuthenticationPrincipal SiteUser user) {
        List<CommentResponseDTO> comments = commentService.getCommentsWithReplies(reviewBoardId, user);
        return ResponseEntity.ok(comments);
    }

    // 댓글 수만 반환
    @GetMapping("/{reviewBoardId}/count")
    public ResponseEntity<Integer> getCommentCount(@PathVariable Long reviewBoardId) {
        int count = commentService.countByReviewBoardId(reviewBoardId);
        return ResponseEntity.ok(count);
    }

    // 댓글 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId,
                                              @AuthenticationPrincipal SiteUser user) {
        commentService.deleteComment(commentId, user);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<Void> updateComment(@PathVariable Long commentId,
                                              @RequestBody Map<String, String> requestBody,
                                              @AuthenticationPrincipal SiteUser user) {
        String newContent = requestBody.get("content");
        commentService.updateComment(commentId, newContent, user);
        Comment updatedComment = commentService.getCommentById(commentId);
        CommentResponseDTO dto = commentService.convertToDTO(updatedComment, user);
        return ResponseEntity.ok().build();
    }
}
