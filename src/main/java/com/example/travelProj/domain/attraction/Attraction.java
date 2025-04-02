package com.example.travelProj.domain.attraction;

import com.example.travelProj.domain.image.imageboard.ImageBoard;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "attraction")
public class Attraction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @OneToMany(cascade = CascadeType.ALL)
//    @JoinColumn(name = "attraction_id")
//    private List<ImageBoard> attraction_images = new ArrayList<>(); // 관광지와 연결된 이미지 리스트

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
