package org.example.model;

import dev.karmanov.library.model.user.UserState;
import jakarta.persistence.*;


import java.util.HashSet;
import java.util.Set;

@Entity
public class UserContextEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_states", joinColumns = @JoinColumn(name = "context_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "state")
    private Set<UserState> userStates = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_action_data", joinColumns = @JoinColumn(name = "context_id"))
    @Column(name = "action_data")
    private Set<String> actionData = new HashSet<>();


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<UserState> getUserStates() {
        return userStates;
    }

    public void setUserStates(Set<UserState> userStates) {
        this.userStates = userStates;
    }

    public Set<String> getActionData() {
        return actionData;
    }

    public void setActionData(Set<String> actionData) {
        this.actionData = actionData;
    }
}
