package com.konomusic.controller;

import com.konomusic.dto.ApiResponse;
import com.konomusic.dto.VideoDTO;
import com.konomusic.entity.Video;
import com.konomusic.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * VideoController - API endpoints cho Video
 */
@RestController
@RequestMapping("/videos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class VideoController {

    private final VideoService videoService;

    /**
     * GET /api/videos - Lấy tất cả videos
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<VideoDTO>>> getAllVideos() {
        List<Video> videos = videoService.getAllVideos();
        List<VideoDTO> videoDTOs = videos.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Videos retrieved successfully", videoDTOs)
        );
    }

    /**
     * GET /api/videos/{id} - Lấy chi tiết video
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VideoDTO>> getVideoById(@PathVariable Long id) {
        Video video = videoService.getVideoById(id);
        VideoDTO videoDTO = convertToDTO(video);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Video retrieved successfully", videoDTO)
        );
    }

    /**
     * GET /api/videos/search - Tìm kiếm videos
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<VideoDTO>>> searchVideos(
            @RequestParam String q) {
        List<Video> videos = videoService.searchVideos(q);
        List<VideoDTO> videoDTOs = videos.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Search results", videoDTOs)
        );
    }

    /**
     * Chuyển đổi Video entity thành VideoDTO
     */
    private VideoDTO convertToDTO(Video video) {
        VideoDTO dto = new VideoDTO();
        dto.setId(video.getId());
        dto.setYoutubeId(video.getYoutubeId());
        dto.setTitle(video.getTitle());
        dto.setChannelName(video.getChannelName());
        dto.setDurationMs(video.getDurationMs());
        dto.setViewCount(video.getViewCount());
        dto.setThumbnailUrl(video.getThumbnailUrl());
        dto.setFetchedAt(video.getFetchedAt());
        return dto;
    }

}

