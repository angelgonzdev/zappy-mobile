package com.example.zappy_mobile;

public class Comic {
    private long id;
    private String title;
    private String author;
    private String filePath;
    private String createdAt;

    public Comic(long id, String title, String author, String filePath, String createdAt) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.filePath = filePath;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getFilePath() { return filePath; }
    public String getCreatedAt() { return createdAt; }
}
