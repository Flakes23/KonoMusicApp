package com.example.konomusic.domain.model;

public class CategoryItem {
    private final String id;
    private final String name;
    private final String imageUrl;
    private final String type;

    public CategoryItem(String id, String name, String imageUrl, String type) {
        this.id = id == null ? "" : id;
        this.name = name == null ? "" : name;
        this.imageUrl = imageUrl == null ? "" : imageUrl;
        this.type = type == null ? "" : type;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getType() {
        return type;
    }
}
