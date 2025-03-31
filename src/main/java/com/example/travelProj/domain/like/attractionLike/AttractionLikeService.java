package com.example.travelProj.domain.like.attractionLike;

import com.example.travelProj.domain.attraction.Attraction;
import com.example.travelProj.domain.attraction.AttractionService;
import com.example.travelProj.domain.user.SiteUser;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AttractionLikeService {

    private final AttractionLikeRepository likeRepository;
    private final AttractionService attractionService;

    @Transactional
    public void addLike(Long attractionId, SiteUser user) {
        Attraction attraction = attractionService.getAttractionById(attractionId);
        if (likeRepository.existsByAttractionAndUser(attraction, user)) {
            throw new IllegalStateException("이미 좋아요를 눌렀습니다.");
        }
        AttractionLike like = new AttractionLike();
        like.setAttraction(attraction);
        like.setUser(user);
        likeRepository.save(like);
    }

    @Transactional
    public void removeLike(Long attractionId, SiteUser user) {
        Attraction attraction = attractionService.getAttractionById(attractionId);
        likeRepository.deleteByAttractionAndUser(attraction, user);
    }

    public long countLikes(Long attractionId) {
        Attraction attraction = attractionService.getAttractionById(attractionId);
        return likeRepository.countByAttraction(attraction);
    }
}
