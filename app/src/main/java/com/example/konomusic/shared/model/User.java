package com.example.konomusic.shared.model;

/**
 * User Model - Đại diện cho người dùng
 */
public class User {

    private Long id;
    private String email;
    private String displayName;
    private String createdAt;

    // Constructor
    public User() {}

    public User(Long id, String email, String displayName) {
        this.id = id;
        this.email = email;
        this.displayName = displayName;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

}

