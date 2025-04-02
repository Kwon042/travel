package com.example.travelProj.domain.image.imageuser;

import com.example.travelProj.domain.user.SiteUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ImageUserRepository extends JpaRepository<ImageUser, Long> {
    Optional<ImageUser> findByUser(SiteUser user);
}
