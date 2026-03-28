-- ============================================
-- KonoMusicApp - Database Schema
-- ============================================

CREATE DATABASE IF NOT EXISTS konomusic CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE konomusic;

-- ============================================
-- VIDEOS (Từ YouTube)
-- ============================================
CREATE TABLE videos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    youtube_id VARCHAR(100) UNIQUE NOT NULL,
    title VARCHAR(255) NOT NULL,
    channel_name VARCHAR(255),
    duration_ms INT DEFAULT 0,
    view_count BIGINT DEFAULT 0,
    thumbnail_url TEXT,
    fetched_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_youtube_id (youtube_id),
    INDEX idx_title (title)
);

-- ============================================
-- GENRES (Thể Loại)
-- ============================================
CREATE TABLE genres (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL
);

-- ============================================
-- VIDEO_GENRES (Liên Kết Video-Genres)
-- ============================================
CREATE TABLE video_genres (
    video_id INT,
    genre_id INT,
    PRIMARY KEY (video_id, genre_id),
    FOREIGN KEY (video_id) REFERENCES videos(id) ON DELETE CASCADE,
    FOREIGN KEY (genre_id) REFERENCES genres(id) ON DELETE CASCADE
);

-- ============================================
-- USERS (Người Dùng)
-- ============================================
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(100),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_email (email)
);

-- ============================================
-- USER_PLAYLISTS (Danh Sách Phát Của Người Dùng)
-- ============================================
CREATE TABLE user_playlists (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    name VARCHAR(255) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id)
);

-- ============================================
-- PLAYLIST_VIDEOS (Videos Trong Playlist)
-- ============================================
CREATE TABLE playlist_videos (
    playlist_id INT,
    video_id INT,
    position INT DEFAULT 0,
    added_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (playlist_id, video_id),
    FOREIGN KEY (playlist_id) REFERENCES user_playlists(id) ON DELETE CASCADE,
    FOREIGN KEY (video_id) REFERENCES videos(id) ON DELETE CASCADE
);

-- ============================================
-- PLAY_LOGS (Lịch Sử Phát)
-- ============================================
CREATE TABLE play_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    video_id INT NOT NULL,
    user_id INT,
    played_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (video_id) REFERENCES videos(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_video_id (video_id),
    INDEX idx_user_id (user_id),
    INDEX idx_played_at (played_at)
);

-- ============================================
-- SEED DATA (Dữ Liệu Mẫu)
-- ============================================

-- Genres
INSERT INTO genres (name) VALUES
('Pop'), ('V-Pop'), ('Hip-hop'), ('R&B'), ('Rock'),
('Jazz'), ('EDM'), ('Ballad'), ('Indie'), ('Classical'),
('Rap'), ('Soul'), ('Reggae'), ('Country'), ('Funk'),
('Disco'), ('House'), ('Techno'), ('Alternative'), ('Metal');

-- Sample User (email: test@example.com, password hashed: test123)
INSERT INTO users (email, password_hash, display_name) VALUES
('test@example.com', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/iq', 'Test User');

-- Sample Videos (YouTube ID của các bài hát nổi tiếng)
INSERT INTO videos (youtube_id, title, channel_name, duration_ms, view_count, thumbnail_url) VALUES
('dQw4w9WgXcQ', 'Never Gonna Give You Up', 'Rick Astley', 212000, 1200000000, 'https://img.youtube.com/vi/dQw4w9WgXcQ/default.jpg'),
('9bZkp7q19f0', 'PSY - GANGNAM STYLE', 'officialpsy', 253000, 4600000000, 'https://img.youtube.com/vi/9bZkp7q19f0/default.jpg'),
('kJQP7kiw9Fk', 'Luis Fonsi - Despacito', 'Luis Fonsi', 228000, 8000000000, 'https://img.youtube.com/vi/kJQP7kiw9Fk/default.jpg'),
('tYzD14yoYt8', 'Ed Sheeran - Shape of You', 'Ed Sheeran', 234000, 3400000000, 'https://img.youtube.com/vi/tYzD14yoYt8/default.jpg'),
('hHW1oY26kxQ', 'Billie Eilish - bad guy', 'Billie Eilish', 194000, 2000000000, 'https://img.youtube.com/vi/hHW1oY26kxQ/default.jpg');

-- Gán genres cho videos
INSERT INTO video_genres (video_id, genre_id) VALUES
(1, 1),    -- Rick Astley - Pop
(2, 3),    -- PSY - Hip-hop
(3, 1),    -- Luis Fonsi - Pop
(4, 1),    -- Ed Sheeran - Pop
(5, 1);    -- Billie Eilish - Pop

-- Sample Playlist
INSERT INTO user_playlists (user_id, name) VALUES
(1, 'My Favorite Songs');

-- Add videos to playlist
INSERT INTO playlist_videos (playlist_id, video_id, position) VALUES
(1, 1, 1),
(1, 2, 2),
(1, 3, 3);

