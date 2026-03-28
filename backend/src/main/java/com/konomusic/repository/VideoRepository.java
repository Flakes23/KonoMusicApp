package com.konomusic.repository;

import com.konomusic.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * VideoRepository - Truy cập dữ liệu Video
 */
@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {

    Optional<Video> findByYoutubeId(String youtubeId);

    List<Video> findByTitleContainingIgnoreCase(String title);

}

