package com.example.clothes;

public class User {
    private int id;
    private String name;
    private String email;
    private String role;
    private String brigade;
    private String position;
    private String avatarUrl;

    public User(int id, String name, String email, String role, String brigade, String position, String avatarUrl) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.brigade = brigade;
        this.position = position;
        this.avatarUrl = avatarUrl;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getBrigade() { return brigade; }
    public String getPosition() { return position; }
    public String getAvatarUrl() { return avatarUrl; }
}