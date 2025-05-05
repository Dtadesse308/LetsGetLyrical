package edu.usc.csci310.project.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Song {
    private Integer id;
    private String title;
    private String artist;
    private List<User> favoritedByUsers;
    private Integer year;
    private String lyrics;

    private Integer position;


    public Song() {
    }

    public Song(Integer id, String title, String artist) {
        this.id = id;
        this.title = title;
        this.artist = artist;
    }
    public Song(Integer id, String title, String artist,Integer year, List<User> favoritedByUsers) {
       this.id = id;
        this.title = title;
        this.artist = artist;
        this.year = year;
        this.favoritedByUsers = favoritedByUsers;
    }

    public Song(Integer id) {
        this.id = id;
    }

  
    public Song(Integer id, String title, String artist, Integer year) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.year = year;
    }

    public Song(Integer id, String title, String artist, Integer year, Integer position) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.year = year;
        this.position = position;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Song song = (Song) o;
        return Objects.equals(id, song.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public List<User> getFavoritedByUsers() {
        return favoritedByUsers;
    }
    public void setFavoritedByUsers(List<User> favoritedByUsers) {
        this.favoritedByUsers = favoritedByUsers;
    }
    public void addFavoritedByUser(User user) {
        if (this.favoritedByUsers == null) {
            this.favoritedByUsers = new ArrayList<>();
        }
        this.favoritedByUsers.add(user);
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getLyrics() {
        return lyrics;
    }

    public void setLyrics(String lyrics) {
        this.lyrics = lyrics;
    }


    public Integer getPosition() {return position;}

    public void setPosition(Integer position) {this.position = position;}
}
