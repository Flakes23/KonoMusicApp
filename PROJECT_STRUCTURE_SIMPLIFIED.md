# 📁 Cấu Trúc Đồ Án KonoMusicApp - Phiên Bản Đơn Giản (Java + Admin/Client)

## 🎯 Cấu Trúc Thư Mục Hoàn Chỉnh

```
KonoMusicApp/                              # Root folder
│
├── 📱 app/                                # Android App (JAVA)
│   ├── src/main/
│   │   ├── java/com/example/konomusic/
│   │   │   │
│   │   │   ├── 👤 client/                # CLIENT VERSION (User)
│   │   │   │   ├── ui/
│   │   │   │   │   ├── activities/
│   │   │   │   │   │   ├── MainActivity.java
│   │   │   │   │   │   ├── PlayerActivity.java
│   │   │   │   │   │   ├── LoginActivity.java
│   │   │   │   │   │   └── RegisterActivity.java
│   │   │   │   │   ├── fragments/
│   │   │   │   │   │   ├── HomeFragment.java
│   │   │   │   │   │   ├── SearchFragment.java
│   │   │   │   │   │   ├── PlaylistFragment.java
│   │   │   │   │   │   └── ProfileFragment.java
│   │   │   │   │   └── adapters/
│   │   │   │   │       ├── VideoAdapter.java
│   │   │   │   │       └── PlaylistAdapter.java
│   │   │   │   │
│   │   │   │   ├── viewmodel/
│   │   │   │   │   ├── HomeViewModel.java
│   │   │   │   │   ├── SearchViewModel.java
│   │   │   │   │   ├── PlayerViewModel.java
│   │   │   │   │   ├── PlaylistViewModel.java
│   │   │   │   │   └── AuthViewModel.java
│   │   │   │   │
│   │   │   │   └── utils/
│   │   │   │       ├── ClientConstants.java
│   │   │   │       └── ClientLogger.java
│   │   │   │
│   │   │   ├── 🔐 admin/                 # ADMIN VERSION (Manager)
│   │   │   │   ├── ui/
│   │   │   │   │   ├── activities/
│   │   │   │   │   │   ├── AdminMainActivity.java
│   │   │   │   │   │   ├── AdminDashboardActivity.java
│   │   │   │   │   │   ├── SubmitVideoActivity.java
│   │   │   │   │   │   ├── ManageCurationActivity.java
│   │   │   │   │   │   ├── ViewAnalyticsActivity.java
│   │   │   │   │   │   └── AdminLoginActivity.java
│   │   │   │   │   └── fragments/
│   │   │   │   │       ├── AdminHomeFragment.java
│   │   │   │   │       ├── CurationFragment.java
│   │   │   │   │       ├── AnalyticsFragment.java
│   │   │   │   │       └── AdminSettingsFragment.java
│   │   │   │   │
│   │   │   │   ├── viewmodel/
│   │   │   │   │   ├── AdminDashboardViewModel.java
│   │   │   │   │   ├── CurationViewModel.java
│   │   │   │   │   ├── AnalyticsViewModel.java
│   │   │   │   │   └── AdminAuthViewModel.java
│   │   │   │   │
│   │   │   │   └── utils/
│   │   │   │       ├── AdminConstants.java
│   │   │   │       └── AdminLogger.java
│   │   │   │
│   │   │   ├── 🔧 shared/                # SHARED CODE (Dùng cho cả client và admin)
│   │   │   │   ├── data/
│   │   │   │   │   ├── api/
│   │   │   │   │   │   ├── ApiService.java
│   │   │   │   │   │   ├── RetrofitClient.java
│   │   │   │   │   │   └── AuthInterceptor.java
│   │   │   │   │   │
│   │   │   │   │   ├── db/
│   │   │   │   │   │   ├── KonoMusicDatabase.java
│   │   │   │   │   │   ├── dao/
│   │   │   │   │   │   │   ├── VideoDao.java
│   │   │   │   │   │   │   ├── UserDao.java
│   │   │   │   │   │   │   ├── PlaylistDao.java
│   │   │   │   │   │   │   └── PlayLogDao.java
│   │   │   │   │   │   └── entity/
│   │   │   │   │   │       ├── VideoEntity.java
│   │   │   │   │   │       ├── UserEntity.java
│   │   │   │   │   │       ├── PlaylistEntity.java
│   │   │   │   │   │       └── PlayLogEntity.java
│   │   │   │   │   │
│   │   │   │   │   └── repository/
│   │   │   │   │       ├── VideoRepository.java
│   │   │   │   │       ├── PlaylistRepository.java
│   │   │   │   │       ├── UserRepository.java
│   │   │   │   │       └── PlayLogRepository.java
│   │   │   │   │
│   │   │   │   ├── model/
│   │   │   │   │   ├── Video.java
│   │   │   │   │   ├── Playlist.java
│   │   │   │   │   ├── User.java
│   │   │   │   │   └── PlayLog.java
│   │   │   │   │
│   │   │   │   └── utils/
│   │   │   │       ├── Constants.java
│   │   │   │       ├── Logger.java
│   │   │   │       ├── DateUtils.java
│   │   │   │       └── SharedPrefManager.java
│   │   │   │
│   │   │   └── KonoMusicApp.java         # Main Application class
│   │   │
│   │   └── res/
│   │       ├── layout/
│   │       │   ├── client/
│   │       │   │   ├── activity_main.xml
│   │       │   │   ├── activity_player.xml
│   │       │   │   ├── fragment_home.xml
│   │       │   │   ├── fragment_search.xml
│   │       │   │   ├── fragment_playlist.xml
│   │       │   │   ├── item_video.xml
│   │       │   │   └── item_playlist.xml
│   │       │   └── admin/
│   │       │       ├── activity_admin_main.xml
│   │       │       ├── activity_submit_video.xml
│   │       │       ├── fragment_curation.xml
│   │       │       ├── fragment_analytics.xml
│   │       │       ├── item_curation.xml
│   │       │       └── item_analytics.xml
│   │       ├── drawable/
│   │       │   ├── ic_home.xml
│   │       │   ├── ic_search.xml
│   │       │   ├── ic_favorite.xml
│   │       │   ├── ic_profile.xml
│   │       │   ├── ic_admin.xml
│   │       │   └── ic_analytics.xml
│   │       ├── values/
│   │       │   ├── strings.xml
│   │       │   ├── strings_admin.xml
│   │       │   ├── colors.xml
│   │       │   ├── dimens.xml
│   │       │   ├── styles.xml
│   │       │   └── themes.xml
│   │       └── menu/
│   │           ├── bottom_navigation_client.xml
│   │           └── bottom_navigation_admin.xml
│   │
│   ├── src/test/java/          # Unit Tests
│   ├── src/androidTest/java/   # Instrumented Tests
│   ├── build.gradle.kts        # Two build variants: client, admin
│   ├── proguard-rules.pro
│   └── AndroidManifest.xml
│
├── ☕ backend/                  # Spring Boot Backend (Java)
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/konomusic/
│   │   │   │   ├── controller/
│   │   │   │   │   ├── VideoController.java
│   │   │   │   │   ├── PlaylistController.java
│   │   │   │   │   ├── AuthController.java
│   │   │   │   │   ├── PlayLogController.java
│   │   │   │   │   └── AdminController.java      # Admin API
│   │   │   │   │
│   │   │   │   ├── service/
│   │   │   │   │   ├── VideoService.java
│   │   │   │   │   ├── PlaylistService.java
│   │   │   │   │   ├── AuthService.java
│   │   │   │   │   ├── UserService.java
│   │   │   │   │   ├── YoutubeService.java
│   │   │   │   │   └── AdminService.java         # Admin logic
│   │   │   │   │
│   │   │   │   ├── entity/
│   │   │   │   │   ├── Video.java
│   │   │   │   │   ├── User.java
│   │   │   │   │   ├── Playlist.java
│   │   │   │   │   ├── PlaylistVideo.java
│   │   │   │   │   ├── PlayLog.java
│   │   │   │   │   ├── Genre.java
│   │   │   │   │   ├── CurationItem.java        # Admin: curation
│   │   │   │   │   └── AuditLog.java            # Admin: audit logs
│   │   │   │   │
│   │   │   │   ├── repository/
│   │   │   │   │   ├── VideoRepository.java
│   │   │   │   │   ├── PlaylistRepository.java
│   │   │   │   │   ├── UserRepository.java
│   │   │   │   │   ├── PlayLogRepository.java
│   │   │   │   │   ├── CurationItemRepository.java
│   │   │   │   │   └── AuditLogRepository.java
│   │   │   │   │
│   │   │   │   ├── dto/
│   │   │   │   │   ├── VideoDTO.java
│   │   │   │   │   ├── PlaylistDTO.java
│   │   │   │   │   ├── UserDTO.java
│   │   │   │   │   ├── LoginRequest.java
│   │   │   │   │   ├── RegisterRequest.java
│   │   │   │   │   ├── CurationItemDTO.java     # Admin
│   │   │   │   │   └── ApiResponse.java
│   │   │   │   │
│   │   │   │   ├── security/
│   │   │   │   │   ├── JwtTokenProvider.java
│   │   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   │   ├── SecurityConfig.java
│   │   │   │   │   └── RoleChecker.java         # Admin roles
│   │   │   │   │
│   │   │   │   ├── exception/
│   │   │   │   │   ├── ApiException.java
│   │   │   │   │   └── GlobalExceptionHandler.java
│   │   │   │   │
│   │   │   │   ├── util/
│   │   │   │   │   ├── YoutubeApiClient.java
│   │   │   │   │   ├── Logger.java
│   │   │   │   │   └── Constants.java
│   │   │   │   │
│   │   │   │   ├── config/
│   │   │   │   │   ├── ApplicationConfig.java
│   │   │   │   │   ├── WebConfig.java
│   │   │   │   │   └── ApplicationProperties.java
│   │   │   │   │
│   │   │   │   └── KonoMusicApplication.java
│   │   │   │
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       ├── application-dev.yml
│   │   │       ├── application-prod.yml
│   │   │       └── db/
│   │   │           └── schema.sql
│   │   │
│   │   └── test/
│   │       └── java/com/konomusic/
│   │           ├── service/
│   │           │   ├── VideoServiceTest.java
│   │           │   └── AuthServiceTest.java
│   │           └── controller/
│   │               └── VideoControllerTest.java
│   │
│   ├── pom.xml
│   ├── mvnw
│   ├── mvnw.cmd
│   └── .mvn/
│
├── 🗄️ database/                 # Database Files
│   ├── schema.sql              # Main schema
│   └── seed-data.sql           # Sample data
│
├── 📚 docs/                    # Documentation
│   ├── API_DOCS.md             # API documentation
│   ├── DATABASE.md             # Database schema
│   ├── JAVA_BACKEND_SETUP.md   # Backend setup
│   ├── YOUTUBE_API_SETUP.md    # YouTube API setup
│   ├── ANDROID_SETUP.md        # Android setup
│   └── ARCHITECTURE.md         # Architecture overview
│
├── 📋 README.md                # Main README
├── .gitignore                  # Git ignore rules
├── .env.example                # Environment template
└── SIMPLIFIED_VERSION_SUMMARY.md # Project summary
```

---

## 📊 Phân Chia Chi Tiết

### 📱 **Android App - CLIENT VERSION** (app/src/main/java/com/example/konomusic/client/)
**Dành cho: Người dùng thường**
- **4 Activities**: MainActivity, PlayerActivity, LoginActivity, RegisterActivity
- **4 Fragments**: HomeFragment, SearchFragment, PlaylistFragment, ProfileFragment
- **5 ViewModels**: MVVM pattern
- **Features**: 
  - ✅ Duyệt videos
  - ✅ Tìm kiếm nhạc
  - ✅ Tạo playlist
  - ✅ Phát nhạc

### 🔐 **Android App - ADMIN VERSION** (app/src/main/java/com/example/konomusic/admin/)
**Dành cho: Quản trị viên/Content manager**
- **6 Activities**: AdminMainActivity, AdminDashboardActivity, SubmitVideoActivity, ManageCurationActivity, ViewAnalyticsActivity, AdminLoginActivity
- **4 Fragments**: AdminHomeFragment, CurationFragment, AnalyticsFragment, AdminSettingsFragment
- **4 ViewModels**: MVVM pattern
- **Features**:
  - ✅ Submit videos để curation
  - ✅ Duyệt/từ chối submissions
  - ✅ Xem analytics
  - ✅ Quản lý curation queue

### 🔧 **Shared Code** (app/src/main/java/com/example/konomusic/shared/)
**Dùng chung cho cả client và admin**
- Data layer (Retrofit, Room)
- Models
- Utilities
- Constants

### ☕ **Backend** (backend/)
- **4 Controllers**: Video, Playlist, Auth, PlayLog + Admin APIs
- **7 Services**: Video, Playlist, Auth, User, YouTube, PlayLog + Admin
- **8 Entities**: Video, User, Playlist, PlaylistVideo, PlayLog, Genre, **CurationItem**, **AuditLog**
- **Security**: JWT authentication + Admin roles
- **Testing**: Unit tests, integration tests

### 🗄️ **Database** (database/)
- **7 Main Tables**: videos, genres, users, playlists, playlist_videos, play_logs, video_genres
- **2 Admin Tables**: curation_items, audit_logs
- **Total: 9 tables**
- **Relationships**: Foreign keys, indexes
- **Sample Data**: 5 videos, 1 user, genres

---

## 📈 Quy Mô Dự Án

| Thành Phần | Số Lượng | Loại |
|-----------|---------|------|
| **CLIENT Activities** | 4 | Java |
| **CLIENT Fragments** | 4 | Java |
| **CLIENT ViewModels** | 5 | Java |
| **ADMIN Activities** | 6 | Java |
| **ADMIN Fragments** | 4 | Java |
| **ADMIN ViewModels** | 4 | Java |
| **Backend Controllers** | 5 | Java |
| **Backend Services** | 7 | Java |
| **Backend Entities** | 8 | Java |
| **Database Tables** | 9 | SQL |
| **Tổng files code** | ~80+ | Java |

---