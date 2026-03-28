package com.konomusic.repository;

import com.konomusic.entity.PlayLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * PlayLogRepository - Truy cập dữ liệu PlayLog
 */
@Repository
public interface PlayLogRepository extends JpaRepository<PlayLog, Long> {

    List<PlayLog> findByUserId(Long userId);

    List<PlayLog> findByVideoId(Long videoId);

}

