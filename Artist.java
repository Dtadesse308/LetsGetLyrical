package edu.usc.csci310.project.models;

public class Artist {
    private Integer id;
    private String name;
    private String imageUrl;

    public Artist(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Artist(Integer id, String name, String imageUrl) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() { return imageUrl; }

    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
