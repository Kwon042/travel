package com.example.travelProj.domain.bookmark;

import com.example.travelProj.domain.user.SiteUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class AttractionBookmark {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long attractionId;
    private String contentTypeId;
    private String areaCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private SiteUser user;

    // 필수 필드만 명확하게 설정해서 바로 생성할 수 있도록 하기 위해 설정
    public AttractionBookmark(SiteUser user, Long attractionId, String contentTypeId, String areaCode) {
        this.user = user;
        this.attractionId = attractionId;
        this.contentTypeId = contentTypeId;
        this.areaCode = areaCode;
    }

    // toString() 메서드 오버라이드
    @Override
    public String toString() {
        return "AttractionBookmark{" +
                "attractionId=" + attractionId +
                ", contentTypeId='" + contentTypeId + '\'' +
                ", user=" + user +
                ", areaCode='" + areaCode + '\'' +
                '}';
    }

}
