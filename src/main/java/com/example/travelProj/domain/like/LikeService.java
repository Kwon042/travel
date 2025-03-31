package com.example.travelProj.domain.like;

import com.example.travelProj.domain.attraction.Attraction;
import com.example.travelProj.domain.board.ReviewBoard;
import com.example.travelProj.domain.comment.Comment;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LikeService {

    final private LikeRepository likeRepository;

    public void addLike(Like like) {
        likeRepository.save(like);
    }

    public long countLikesForReviewBoard(ReviewBoard reviewBoard) {
        return likeRepository.countByReviewBoard(reviewBoard);
    }

    public long countLikesForComment(Comment comment) {
        return likeRepository.countByComment(comment);
    }

    public long countLikesForAttraction(Attraction attraction) {
        return likeRepository.countByAttraction(attraction);
    }


}
