package edu.usc.csci310.project.models;

public class User {
    private Integer id;
    private String username;
    private String password;
    private boolean is_private;

    // Default constructor
    public User() {}

    public User(Integer id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }
    public User(Integer id, String username, String password, boolean is_private) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.is_private = is_private;
    }

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public boolean getIs_private() {
        return is_private;
    }

    public void setIs_private(boolean is_private) {
        this.is_private = is_private;
    }
}