package com.example.travelProj.user;

import com.example.travelProj.Image;
import com.example.travelProj.board.ReviewBoard;
import com.example.travelProj.comment.Comment;
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
    private UserRole role = UserRole.USER;

    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToOne(mappedBy = "siteUser", cascade = CascadeType.ALL)
    @JoinColumn(name = "profile_image_id")
    private Image profileImage;

    private String profileImageUrl;

    // 연관관계 설정: 사용자가 작성한 리뷰와 댓글
    @OneToMany(mappedBy = "user")
    private List<ReviewBoard> reviewBoards;

    @OneToMany(mappedBy = "user")
    private List<Comment> comments;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.getValue()));
    }

    @Override
    public String getPassword() {
        return this.password; // 비밀번호 반환
    }

    @Override
    public String getUsername() {
        return this.username; // 사용자 이름 반환
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



}
