package com.example.clothes;


public class User {
    private int id;
    private String name;
    private String email;
    private String role;
    private String brigade;
    private String position;
    private String avatarUrl;

    public User() {}

    public User(int id, String name, String email, String role, String brigade, String position, String avatarUrl) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.brigade = brigade;
        this.position = position;
        this.avatarUrl = avatarUrl;
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getBrigade() { return brigade; }
    public void setBrigade(String brigade) { this.brigade = brigade; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}
