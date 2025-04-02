package com.example.travelProj.domain.image.imageuser;

import com.example.travelProj.domain.user.SiteUser;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Entity
@Data
@Table(name = "image_user")
public class ImageUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String filename;
    private String filepath;
    private String url;

    @OneToOne
    @JoinColumn(name = "site_user_id", nullable = false)
    private SiteUser user;

    public String getImageUrl() {
        return url;
    }
}
