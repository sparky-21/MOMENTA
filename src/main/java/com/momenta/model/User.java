package com.momenta.model;

public class User {
    private int id;
    private String name;
    private String username;
    private String passwordHash;
    private String persona;

    public User() {}

    public User(String name, String username, String passwordHash, String persona) {
        this.name = name;
        this.username = username;
        this.passwordHash = passwordHash;
        this.persona = persona;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getPersona() { return persona; }
    public void setPersona(String persona) { this.persona = persona; }
}
