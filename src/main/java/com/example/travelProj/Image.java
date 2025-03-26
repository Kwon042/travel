package com.example.travelProj;

import com.example.travelProj.board.ReviewBoard;
import com.example.travelProj.user.SiteUser;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Entity
@Data
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String filename;  // 파일명
    private String filepath;  // 파일경로

    private String url;

    @OneToOne
    @JoinColumn(name = "site_user_id")
    private SiteUser siteUser;

    @ManyToOne
    @JoinColumn(name = "reviewBoard_id")
    // JSON으로 변환 시, 참조하는 객체의 값을 넣지 않음
    @JsonBackReference
    private ReviewBoard reviewBoard;  // 연관관계의 게시글

    public String getImageUrl() {
        return url; // url 필드를 반환
    }

}
