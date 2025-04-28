package com.example.travelProj.domain.comment;

import com.example.travelProj.domain.board.ReviewBoard;
import com.example.travelProj.domain.user.SiteUser;
import com.example.travelProj.domain.user.UserRole;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.example.travelProj.domain.board.ReviewBoardService;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final ReviewBoardService reviewBoardService;

    @Transactional
    public void addComment(Long reviewBoardId, SiteUser user, String content, Long parentId) {
        ReviewBoard reviewBoard = reviewBoardService.getBoardById(reviewBoardId);

        Comment comment = new Comment();
        comment.setReviewBoard(reviewBoard);
        comment.setUser(user);
        comment.setContent(content);

        if (parentId != null) {
            Comment parentComment = getCommentById(parentId);
            comment.setParent(parentComment);
        }

        commentRepository.save(comment);
    }

    @Transactional
    public void deleteComment(Long commentId, SiteUser currentUser) {
        Comment comment = getCommentById(commentId);
        if (!comment.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("댓글 삭제 권한이 없습니다.");
        }
        commentRepository.delete(comment);
    }

    @Transactional
    public Comment updateComment(Long commentId, String newContent, SiteUser currentUser) {
        Comment comment = getCommentById(commentId);
        if (!comment.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("댓글 수정 권한이 없습니다.");
        }
        comment.setContent(newContent);
        comment.setUpdatedAt(LocalDateTime.now());
        return comment;
    }

    public Comment getCommentById(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
    }

    // 댓글 트리 구조 조회 (대댓글 포함)
    public List<CommentResponseDTO> getCommentsWithReplies(Long reviewBoardId, SiteUser user) {
        List<Comment> comments = commentRepository.findByReviewBoard_Id(reviewBoardId);

        // 부모 댓글만 필터링
        List<Comment> parentComments = comments.stream()
                .filter(c -> c.getParent() == null)
                .collect(Collectors.toList());

        return parentComments.stream()
                .map(comment -> convertToDTO(comment, user))
                .collect(Collectors.toList());
    }

    // 댓글 수 반환 (대댓글 포함)
    public int countByReviewBoardId(Long reviewBoardId) {
        List<Comment> comments = commentRepository.findByReviewBoard_IdAndParentIsNull(reviewBoardId);
        return countTotalComments(comments);
    }

    // 전체 댓글 수 계산
    private int countTotalComments(List<Comment> comments) {
        if (comments == null || comments.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (Comment comment : comments) {
            count++; // 자기 자신
            count += countTotalComments(comment.getChildren()); // 대댓글 포함 재귀 카운트
        }
        return count;
    }

    // Entity → DTO 변환 (트리 구조)
    CommentResponseDTO convertToDTO(Comment comment, SiteUser user) {
        CommentResponseDTO dto = new CommentResponseDTO();
        dto.setId(comment.getId());
        dto.setUsername(comment.getUser().getUsername());
        dto.setNickname(comment.getUser().getNickname());
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());
        dto.setLikeStatus(comment.getLikeStatus(user));
        dto.setLikesCount(comment.getLikeCount());


        // 댓글 이미지 보이기
        dto.setProfileImageUrl(
                (comment.getUser().getProfileImage() != null) ? comment.getUser().getProfileImage().getUrl()
                        : (comment.getUser().getRole() == UserRole.ADMIN ? "/images/default-adminprofile.png" : "/images/default-profile.jpg")
        );

        // 대댓글 재귀 처리
        dto.setChildren(comment.getChildren().stream()
                .map(childComment -> convertToDTO(childComment, user))
                .collect(Collectors.toList()));
        return dto;
    }

    public long countComments() {
        return commentRepository.count();
    }


}
