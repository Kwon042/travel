package com.example.travelProj.domain.attraction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AttractionRepository extends JpaRepository<Attraction, Long> {

    List<Attraction> findByNameContaining(String keyword);

    @Query(value = "SELECT * FROM attraction ORDER BY RAND() LIMIT 3", nativeQuery = true)
    List<Attraction> findRandomAttractions();

}
