package com.example.travelProj.domain.user;

import com.example.travelProj.domain.board.ReviewBoard;
import com.example.travelProj.domain.comment.Comment;
import com.example.travelProj.domain.image.imageuser.ImageUser;
import com.example.travelProj.domain.like.boardlike.ReviewBoardLike;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "siteUser")
public class SiteUser implements UserDetails {
    private List<GrantedAuthority> authorities = new ArrayList<>();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private ImageUser profileImage;

    // 연관관계 설정: 사용자가 작성한 리뷰와 댓글
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<ReviewBoard> reviewBoards;

    // ReviewBoardLike와의 관계 추가 (양방향)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewBoardLike> reviewBoardLikes = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Comment> comments;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public String getProfileImageUrl() {
        if (profileImage != null && profileImage.getUrl() != null) {
            return profileImage.getUrl();
        }
        if (UserRole.ADMIN.equals(this.getRole())) {
            return "/images/default-adminprofile.png";
        }
        return "/images/default-profile.jpg";
    }
    public void setProfileImageUrl(String url) {
        if (this.profileImage != null) {
            this.profileImage.setUrl(url);
        } else {
            ImageUser newProfileImage = new ImageUser();
            newProfileImage.setUrl(url);
            newProfileImage.setUser(this); // 역방향 매핑도 설정해줌
            this.profileImage = newProfileImage;
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.getValue()));
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // 계정 만료 여부, 기본적으로 true로 설정
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // 계정 잠금 여부, 기본적으로 true로 설정
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 자격 증명 만료 여부, 기본적으로 true로 설정
    }

    @Override
    public boolean isEnabled() {
        return true; // 사용자 활성화 여부, 기본적으로 true로 설정
    }

    // toString() 메서드 오버라이드
    @Override
    public String toString() {
        return "SiteUser{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                '}';
    }


}
