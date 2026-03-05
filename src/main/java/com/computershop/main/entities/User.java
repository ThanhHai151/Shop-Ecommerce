package com.computershop.main.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;
    
    @Column(name = "username", nullable = false, length = 255)
    private String username;
    
    @Column(name = "password_hash", length = 255)
    private String passwordHash;
    
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roleid")
    private Role role;
    
    @Transient
    private String password;
    
    public User() {}
    
    public User(String username, String passwordHash, String email, Role role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
        this.role = role;
    }
    
    public Integer getUserId() {return userId;}
    public String getUsername() {return username;}
    public String getPasswordHash() {return passwordHash;}
    public String getEmail() {return email;}
    public Role getRole() {return role;}
    public String getPassword() {return password;}

    public void setUserId(Integer userId) {this.userId = userId;}
    public void setUsername(String username) {this.username = username;}
    public void setPasswordHash(String passwordHash) {this.passwordHash = passwordHash;}
    public void setEmail(String email) {this.email = email;}
    public void setRole(Role role) {this.role = role;}
    public void setPassword(String password) {this.password = password;}

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role=" + (role != null ? role.getRoleName() : "null") +
                '}';
    }
}