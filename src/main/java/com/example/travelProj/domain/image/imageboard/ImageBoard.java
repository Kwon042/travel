package com.example.travelProj.domain.image.imageboard;

import com.example.travelProj.domain.board.ReviewBoard;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Entity
@Data
@Table(name = "image_review_board")
public class ImageBoard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String filename;
    private String filepath;
    private String url;

    @ManyToOne
    @JoinColumn(name = "reviewBoard_id", nullable = false)
    @JsonBackReference // JSON 직렬화 시 무한 루프를 방지하고, 해당 참조의 직렬화를 걸러냄
    private ReviewBoard reviewBoard;

    public String getImageUrl() {
        return url;
    }
}
