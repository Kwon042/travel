package com.example.travelProj.domain.comment;

import com.example.travelProj.domain.board.ReviewBoard;
import com.example.travelProj.domain.user.SiteUser;
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
    public void addComment(Long reviewBoardId, SiteUser user, String content) {
        ReviewBoard reviewBoard = reviewBoardService.getBoardById(reviewBoardId);

        Comment comment = new Comment();
        comment.setReviewBoard(reviewBoard);
        comment.setUser(user);
        comment.setContent(content);

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
    public void updateComment(Long commentId, String newContent, SiteUser currentUser) {
        Comment comment = getCommentById(commentId);
        if (!comment.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("댓글 수정 권한이 없습니다.");
        }
        comment.setContent(newContent);
        // 수정 시간 갱신 원하면 추가
        comment.setUpdatedAt(LocalDateTime.now());
        commentRepository.save(comment);
    }

    public Comment getCommentById(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
    }

    // 댓글 트리 구조 조회 (대댓글 포함)
    public List<CommentResponseDTO> getCommentsWithReplies(Long reviewBoardId) {
        List<Comment> comments = commentRepository.findByReviewBoard_Id(reviewBoardId);

        // 부모 댓글만 필터링
        List<Comment> parentComments = comments.stream()
                .filter(c -> c.getParent() == null)
                .collect(Collectors.toList());

        return parentComments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 댓글 수 반환
    public int countByReviewBoardId(Long reviewBoardId) {
        return commentRepository.countByReviewBoard_Id(reviewBoardId);
    }

    // Entity → DTO 변환 (트리 구조)
    private CommentResponseDTO convertToDTO(Comment comment) {
        CommentResponseDTO dto = new CommentResponseDTO();
        dto.setId(comment.getId());
        dto.setUsername(comment.getUser().getUsername());
        dto.setNickname(comment.getUser().getNickname());
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setLikesCount(comment.getLikes().size());


        if (comment.getUser().getProfileImage() != null) {
            dto.setProfileImageUrl(comment.getUser().getProfileImage().getUrl());
        } else {
            dto.setProfileImageUrl("/default-profile.png");
        }
        // 대댓글 재귀 처리
        dto.setChildren(comment.getChildren().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList()));
        return dto;
    }

}
