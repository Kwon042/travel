package com.example.travelProj.domain.attraction;

import com.example.travelProj.domain.attraction.Attraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AttractionRepository extends JpaRepository<Attraction, Long> {
    @Query(value = "SELECT * FROM attraction ORDER BY RAND() LIMIT 3", nativeQuery = true)
    List<Attraction> findRandomAttractions();


}
