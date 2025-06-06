package com.example.travelProj.domain.board;

import com.example.travelProj.domain.image.imageboard.ImageBoard;
import com.example.travelProj.domain.like.boardlike.ReviewBoardLike;
import com.example.travelProj.domain.region.Region;
import com.example.travelProj.domain.comment.Comment;
import com.example.travelProj.domain.user.SiteUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @Column(name = "created_at",updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 여러 댓글을 포함할 수 있도록 관계 설정
    @OneToMany(mappedBy = "reviewBoard")
    private List<Comment> comments;

    @OneToMany(mappedBy = "reviewBoard")
    private List<ReviewBoardLike> reviewBoardLikes;

    // 좋아요 수 계산
    public long getLikesCount() {
        return reviewBoardLikes != null ? reviewBoardLikes.size() : 0;
    }

    // cascade = CascadeType.ALL : 부모 엔티티(reviewBoard)에서 생성, 업데이트, 삭제되면 image도 동일하게 처리
    // orphanRemoval = true : 부모 엔티티(reviewBoard)에서 image를 참조 제거하면 image엔티티에서도 DB에서 삭제
    @OneToMany(mappedBy = "reviewBoard", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ImageBoard> review_images = new ArrayList<>();

    @ElementCollection
    private List<String> imageUrls = new ArrayList<>(); // 이미지 URL 리스트

    @Transient // DB에 저장되지 않도록 지정할 수 있음
    private String mainImageUrl; // 메인 이미지 URL

    // 이미지가 있을 경우 메인 이미지 URL 설정
    public void setMainImageUrlFromImages() {
        if (review_images != null && !review_images.isEmpty()) {
            this.mainImageUrl = review_images.get(0).getImageUrl(); // 첫 번째 이미지의 URL을 메인으로 사용
        } else {
            this.mainImageUrl = "/images/default-thumbnail.png"; // 기본 이미지 URL 설정
        }
    }

    // PrePersist 메소드 하나로 모든 초기화를 처리 (timestamp)
    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    // 객체가 수정될 때마다 updatedAt을 현재 시간으로 갱신
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }



}
