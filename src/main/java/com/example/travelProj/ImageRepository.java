package com.example.travelProj;

import com.example.travelProj.user.SiteUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Long> {

    Image findBySiteUserAndFilename(SiteUser user, String filename);
    Optional<Image> findBySiteUserIdAndFilename(Long siteUserId, String filename);
}
