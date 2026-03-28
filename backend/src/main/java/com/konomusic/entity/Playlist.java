package com.konomusic.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Playlist Entity - Danh sách phát của người dùng
 */
@Entity
@Table(name = "user_playlists", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Playlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    @ManyToMany
    @JoinTable(
            name = "playlist_videos",
            joinColumns = @JoinColumn(name = "playlist_id"),
            inverseJoinColumns = @JoinColumn(name = "video_id")
    )
    private List<Video> videos;

    private LocalDateTime createdAt = LocalDateTime.now();

}

