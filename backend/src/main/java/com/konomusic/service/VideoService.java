package com.konomusic.service;

import com.konomusic.entity.Video;
import com.konomusic.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * VideoService - Xử lý logic Video
 */
@Service
@RequiredArgsConstructor
public class VideoService {

    private final VideoRepository videoRepository;

    /**
     * Lấy tất cả videos
     */
    public List<Video> getAllVideos() {
        return videoRepository.findAll();
    }

    /**
     * Lấy video theo ID
     */
    public Video getVideoById(Long id) {
        return videoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Video not found: " + id));
    }

    /**
     * Tìm kiếm videos theo tiêu đề
     */
    public List<Video> searchVideos(String query) {
        return videoRepository.findByTitleContainingIgnoreCase(query);
    }

    /**
     * Lưu video mới
     */
    public Video saveVideo(Video video) {
        return videoRepository.save(video);
    }

    /**
     * Kiểm tra video đã tồn tại chưa
     */
    public boolean videoExists(String youtubeId) {
        return videoRepository.findByYoutubeId(youtubeId).isPresent();
    }

}

