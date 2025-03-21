package com.example.travelProj;

import com.example.travelProj.attraction.Attraction;
import com.example.travelProj.board.ReviewBoard;
import com.example.travelProj.comment.Comment;
import com.example.travelProj.user.SiteUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "likes")
public class Like {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private SiteUser user;

    // 여행지 좋아요 추가 - 마이페이지에 추가
    @ManyToOne
    @JoinColumn(name = "attraction_id")
    private Attraction attraction;

    // 리뷰게시판 단순 좋아요
    @ManyToOne
    @JoinColumn(name = "reviewBoard_id", nullable = false)
    private ReviewBoard reviewBoard;

    // 댓글 단순 좋아요
    @ManyToOne
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
