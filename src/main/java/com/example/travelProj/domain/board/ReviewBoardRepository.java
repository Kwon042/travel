package com.example.travelProj.domain.board;

import com.example.travelProj.domain.region.Region;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewBoardRepository extends JpaRepository<ReviewBoard, Long> {
    List<ReviewBoard> findByRegion_RegionName(String regionName);
    Page<ReviewBoard> findByRegion(Region region, Pageable pageable); // 특정 지역의 게시글
}
