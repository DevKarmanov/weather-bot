package org.example.model;

import jakarta.persistence.*;
import org.example.dto.AI.Message;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
public class UserEntity {
    @Id
    private Long userId;
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> userRoles = new HashSet<>();

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "context_id")
    private UserContextEntity userContext;

    private String defaultCity;

    private String chatId;

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getDefaultCity() {
        return defaultCity;
    }

    public void setDefaultCity(String defaultCity) {
        this.defaultCity = defaultCity;
    }

    public void addRole(String role) {
        userRoles.add(role.toLowerCase(Locale.ROOT).trim());
    }

    public void addRoles(String[] newRoles) {
        userRoles.addAll(Arrays.asList(newRoles));
    }

    public boolean removeRole(String role){
        return userRoles.remove(role);
    }

    public boolean removeRoles(String[] rolesToDel) {
        Set<String> rolesToRemove = Arrays.stream(rolesToDel)
                .map(role -> role.toLowerCase(Locale.ROOT).trim())
                .collect(Collectors.toSet());
        return userRoles.removeAll(rolesToRemove);
    }

    public void resetRoles() {
        userRoles.clear();
    }

    public Set<String> getRoles() {
        return userRoles;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public UserContextEntity getUserContext() {
        return userContext;
    }

    public void setUserContext(UserContextEntity userContext) {
        this.userContext = userContext;
    }
}
