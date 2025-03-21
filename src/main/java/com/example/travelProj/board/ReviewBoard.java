package com.example.travelProj.board;

import com.example.travelProj.comment.Comment;
import com.example.travelProj.user.SiteUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "reviewBoard")
public class ReviewBoard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private SiteUser user;

    @Column(nullable = false)
    private String title;

    private String nickname;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String region;
    private Long originalPostId;

    @Column(name = "created_at",updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    private Long hit = 0L;

    @Column(name = "like_count")
    private Long likeCount = 0L;

    // 여러 댓글을 포함할 수 있도록 관계 설정
    @OneToMany(mappedBy = "reviewBoard")
    private List<Comment> comments;

    // PrePersist 메소드 하나로 모든 초기화를 처리 (timestamp)
    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
    }

    // 객체가 수정될 때마다 updatedAt을 현재 시간으로 갱신
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Column(name = "reviewfile_img")
    private String reviewFileImg;

    public String getImageName() {
        return reviewFileImg;
    }

    public Long getId() { return id; }



}
