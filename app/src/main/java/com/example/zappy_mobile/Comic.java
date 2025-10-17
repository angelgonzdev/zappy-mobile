package com.example.zappy_mobile;

/**
 * Clase que representa una historieta (Comic) dentro de la aplicación Zappy.
 * Cada comic contiene información básica como título, autor, ruta del archivo PDF y fecha de creación.
 */
public class Comic {
    private long id;
    private String title;
    private String author;
    private String filePath;
    private String createdAt;

    // Constructor completo
    public Comic(long id, String title, String author, String filePath, String createdAt) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.filePath = filePath;
        this.createdAt = createdAt;
    }

    // Constructor sin ID (por ejemplo, antes de guardar en base de datos)
    public Comic(String title, String author, String filePath, String createdAt) {
        this.title = title;
        this.author = author;
        this.filePath = filePath;
        this.createdAt = createdAt;
    }

    // Getters
    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getFilePath() { return filePath; }
    public String getCreatedAt() { return createdAt; }

    // Setters
    public void setId(long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setAuthor(String author) { this.author = author; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Comic{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", filePath='" + filePath + '\'' +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }
}
