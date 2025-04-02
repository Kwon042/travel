package com.example.travelProj.domain.like.attractionlike;

import com.example.travelProj.domain.attraction.Attraction;
import com.example.travelProj.domain.user.SiteUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttractionLikeRepository extends JpaRepository<AttractionLike, Long> {
    boolean existsByAttractionAndUser(Attraction attraction, SiteUser user);
    long countByAttraction(Attraction attraction);
    void deleteByAttractionAndUser(Attraction attraction, SiteUser user);
}
