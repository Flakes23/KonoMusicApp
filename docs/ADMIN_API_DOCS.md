# 🔐 Admin API Documentation

## 📋 Mục Lục
- [Admin Authentication](#admin-authentication)
- [Curation Management](#curation-management)
- [Analytics](#analytics)
- [Audit Logs](#audit-logs)
- [User Management](#user-management)

---

## 🔑 Admin Authentication

### Login Admin
```
POST /api/admin/auth/login

Request:
{
  "email": "admin@konomusic.com",
  "password": "admin123"
}

Response (200):
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "admin": {
      "id": 1,
      "email": "admin@konomusic.com",
      "role": "ADMIN"
    },
    "expiresIn": 86400
  }
}
```

---

## 📝 Curation Management

### 1. Submit Video for Curation
```
POST /api/admin/curation/submit

Headers:
Authorization: Bearer <token>

Request:
{
  "youtubeId": "dQw4w9WgXcQ",
  "title": "Never Gonna Give You Up",
  "channelName": "Rick Astley",
  "note": "Classic 80s hit"
}

Response (201):
{
  "success": true,
  "data": {
    "id": 1,
    "youtubeId": "dQw4w9WgXcQ",
    "title": "Never Gonna Give You Up",
    "status": "DRAFT",
    "submittedAt": "2026-03-28T10:00:00Z",
    "submittedBy": 1
  }
}
```

### 2. Get Pending Curations (Queue)
```
GET /api/admin/curation/pending?page=1&limit=10

Headers:
Authorization: Bearer <token>

Response (200):
{
  "success": true,
  "data": {
    "curations": [
      {
        "id": 1,
        "youtubeId": "dQw4w9WgXcQ",
        "title": "Never Gonna Give You Up",
        "status": "DRAFT",
        "submittedAt": "2026-03-28T10:00:00Z",
        "note": "Classic 80s hit"
      },
      {
        "id": 2,
        "youtubeId": "9bZkp7q19f0",
        "title": "PSY - GANGNAM STYLE",
        "status": "DRAFT",
        "submittedAt": "2026-03-28T09:30:00Z"
      }
    ],
    "pagination": {
      "page": 1,
      "limit": 10,
      "total": 2,
      "totalPages": 1
    }
  }
}
```

### 3. Approve Curation
```
POST /api/admin/curation/{id}/approve

Headers:
Authorization: Bearer <token>

Request:
{
  "note": "Approved - good quality"
}

Response (200):
{
  "success": true,
  "data": {
    "id": 1,
    "status": "APPROVED",
    "approvedAt": "2026-03-28T10:05:00Z",
    "approvedBy": 2
  }
}
```

### 4. Reject Curation
```
POST /api/admin/curation/{id}/reject

Headers:
Authorization: Bearer <token>

Request:
{
  "reason": "Duplicate entry"
}

Response (200):
{
  "success": true,
  "data": {
    "id": 1,
    "status": "REJECTED",
    "rejectedAt": "2026-03-28T10:05:00Z",
    "rejectionReason": "Duplicate entry"
  }
}
```

### 5. Get All Approved Videos
```
GET /api/admin/curation/approved?page=1&limit=20

Headers:
Authorization: Bearer <token>

Response (200):
{
  "success": true,
  "data": {
    "videos": [
      {
        "id": 1,
        "youtubeId": "dQw4w9WgXcQ",
        "title": "Never Gonna Give You Up",
        "approvedAt": "2026-03-28T10:05:00Z",
        "approvedBy": 2,
        "views": 1200000000
      }
    ],
    "pagination": { ... }
  }
}
```

---

## 📊 Analytics

### 1. Get Video Statistics
```
GET /api/admin/analytics/videos/{videoId}?period=7d

Headers:
Authorization: Bearer <token>

Response (200):
{
  "success": true,
  "data": {
    "videoId": 1,
    "title": "Never Gonna Give You Up",
    "period": "7d",
    "statistics": {
      "totalViews": 1200000,
      "totalPlays": 50000,
      "uniqueUsers": 30000,
      "averagePlayDuration": 120,
      "completionRate": 0.85
    },
    "dailyData": [
      {
        "date": "2026-03-28",
        "views": 200000,
        "plays": 8000,
        "users": 5000
      }
    ]
  }
}
```

### 2. Get Most Played Videos
```
GET /api/admin/analytics/top-videos?limit=10&period=30d

Headers:
Authorization: Bearer <token>

Response (200):
{
  "success": true,
  "data": {
    "topVideos": [
      {
        "rank": 1,
        "videoId": 1,
        "title": "Never Gonna Give You Up",
        "plays": 150000,
        "uniqueUsers": 100000
      }
    ]
  }
}
```

### 3. Get User Analytics
```
GET /api/admin/analytics/users?period=30d

Headers:
Authorization: Bearer <token>

Response (200):
{
  "success": true,
  "data": {
    "totalUsers": 1000,
    "activeUsers": 600,
    "newUsers": 150,
    "retention": 0.85,
    "averageSessionDuration": 300,
    "dailyActiveUsers": [
      {
        "date": "2026-03-28",
        "count": 500
      }
    ]
  }
}
```

---

## 📋 Audit Logs

### Get Audit Logs
```
GET /api/admin/audit-logs?page=1&limit=20&action=APPROVE

Headers:
Authorization: Bearer <token>

Response (200):
{
  "success": true,
  "data": {
    "logs": [
      {
        "id": 1,
        "adminId": 2,
        "adminEmail": "admin@konomusic.com",
        "action": "APPROVE",
        "entityType": "CURATION_ITEM",
        "entityId": 1,
        "metadata": {
          "videoTitle": "Never Gonna Give You Up",
          "note": "Approved - good quality"
        },
        "createdAt": "2026-03-28T10:05:00Z"
      }
    ],
    "pagination": { ... }
  }
}
```

---

## 👥 User Management

### 1. Get All Users
```
GET /api/admin/users?page=1&limit=20&status=active

Headers:
Authorization: Bearer <token>

Response (200):
{
  "success": true,
  "data": {
    "users": [
      {
        "id": 1,
        "email": "test@example.com",
        "displayName": "Test User",
        "status": "active",
        "createdAt": "2026-03-20T10:00:00Z",
        "lastLogin": "2026-03-28T09:30:00Z"
      }
    ],
    "pagination": { ... }
  }
}
```

### 2. Ban User
```
POST /api/admin/users/{userId}/ban

Headers:
Authorization: Bearer <token>

Request:
{
  "reason": "Inappropriate content"
}

Response (200):
{
  "success": true,
  "data": {
    "userId": 1,
    "status": "BANNED",
    "bannedAt": "2026-03-28T10:05:00Z"
  }
}
```

### 3. Get User Play History
```
GET /api/admin/users/{userId}/play-history?limit=20

Headers:
Authorization: Bearer <token>

Response (200):
{
  "success": true,
  "data": {
    "userId": 1,
    "playHistory": [
      {
        "videoId": 1,
        "title": "Never Gonna Give You Up",
        "playedAt": "2026-03-28T10:00:00Z",
        "duration": 213000
      }
    ]
  }
}
```

---

## 🛡️ Security & Roles

**Admin Roles:**
```
SUPER_ADMIN   - Toàn quyền
ADMIN         - Quản lý curation + analytics
MODERATOR     - Chỉ duyệt content
VIEWER        - Chỉ xem analytics
```

**Headers Required:**
```
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

**Admin Token Example:**
```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.
eyJzdWIiOiIyIiwicm9sZXMiOlsiQURNSU4iXSwiZXhwIjoxNjQ1OTM2MDAwfQ.
abc123...
```

---

## ❌ Error Codes

| Code | Message | Solution |
|------|---------|----------|
| 401 | Unauthorized | Invalid/expired token |
| 403 | Forbidden | Insufficient permissions |
| 404 | Not found | Curation item doesn't exist |
| 409 | Conflict | Video already approved |
| 500 | Server error | Contact admin |

---

## 🔄 Workflow Example

```
1. Admin Login
   POST /api/admin/auth/login
   ↓ Get token
   
2. Submit Video
   POST /api/admin/curation/submit
   ↓ Video in DRAFT status
   
3. View Queue
   GET /api/admin/curation/pending
   ↓ See submissions
   
4. Approve Video
   POST /api/admin/curation/{id}/approve
   ↓ Video now APPROVED
   
5. View Analytics
   GET /api/admin/analytics/videos/{id}
   ↓ See performance
   
6. Check Audit
   GET /api/admin/audit-logs
   ↓ See all actions
```

---

**Total Admin Endpoints: 12**

Endpoints Summary:
- ✅ 2 Auth
- ✅ 5 Curation
- ✅ 3 Analytics
- ✅ 1 Audit
- ✅ 1 User Management

