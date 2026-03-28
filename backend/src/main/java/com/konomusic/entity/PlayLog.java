package com.konomusic.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * PlayLog Entity - Lịch sử phát nhạc của người dùng
 */
@Entity
@Table(name = "play_logs", indexes = {
        @Index(name = "idx_video_id", columnList = "video_id"),
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_played_at", columnList = "played_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", nullable = false)
    private Video video;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDateTime playedAt = LocalDateTime.now();

}

