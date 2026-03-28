package com.konomusic.controller;

import com.konomusic.dto.ApiResponse;
import com.konomusic.entity.PlayLog;
import com.konomusic.entity.User;
import com.konomusic.service.PlayLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * PlayLogController - API endpoints cho PlayLog
 */
@RestController
@RequestMapping("/play-logs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class PlayLogController {

    private final PlayLogService playLogService;

    /**
     * POST /api/play-logs - Log khi user phát một video
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PlayLog>> logVideoPlay(
            @RequestParam Long videoId,
            @RequestParam Long userId) {

        // TODO: Lấy user từ JWT token
        User user = new User();
        user.setId(userId);

        PlayLog playLog = playLogService.logVideoPlay(videoId, user);

        return ResponseEntity.status(201).body(
                new ApiResponse<>(true, "Video play logged successfully", playLog)
        );
    }

}

