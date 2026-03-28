package com.konomusic.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Video Entity - Đại diện cho một bài hát/video từ YouTube
 */
@Entity
@Table(name = "videos", indexes = {
        @Index(name = "idx_youtube_id", columnList = "youtube_id"),
        @Index(name = "idx_title", columnList = "title")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String youtubeId;

    @Column(nullable = false)
    private String title;

    private String channelName;

    private Integer durationMs;

    private Long viewCount = 0L;

    @Column(columnDefinition = "TEXT")
    private String thumbnailUrl;

    private LocalDateTime fetchedAt = LocalDateTime.now();

}

