package com.konomusic.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * VideoDTO - DTO cho Video
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoDTO {

    private Long id;
    private String youtubeId;
    private String title;
    private String channelName;
    private Integer durationMs;
    private Long viewCount;
    private String thumbnailUrl;
    private LocalDateTime fetchedAt;

}

