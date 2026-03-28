package com.konomusic.service;

import com.konomusic.entity.PlayLog;
import com.konomusic.entity.User;
import com.konomusic.entity.Video;
import com.konomusic.repository.PlayLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * PlayLogService - Xử lý logic PlayLog (lịch sử phát)
 */
@Service
@RequiredArgsConstructor
public class PlayLogService {

    private final PlayLogRepository playLogRepository;
    private final VideoService videoService;

    /**
     * Log khi user phát một video
     */
    public PlayLog logVideoPlay(Long videoId, User user) {
        Video video = videoService.getVideoById(videoId);

        PlayLog playLog = new PlayLog();
        playLog.setVideo(video);
        playLog.setUser(user);
        playLog.setPlayedAt(LocalDateTime.now());

        return playLogRepository.save(playLog);
    }

    /**
     * Lấy lịch sử phát của user
     */
    public List<PlayLog> getUserPlayHistory(Long userId) {
        return playLogRepository.findByUserId(userId);
    }

    /**
     * Lấy danh sách user đã phát video
     */
    public List<PlayLog> getVideoPlayHistory(Long videoId) {
        return playLogRepository.findByVideoId(videoId);
    }

}

