package org.example.service;


import dev.karmanov.library.model.user.DefaultUserContext;
import dev.karmanov.library.model.user.UserContext;
import dev.karmanov.library.model.user.UserState;
import dev.karmanov.library.service.listener.role.RoleChangeListener;
import dev.karmanov.library.service.listener.state.StateChangeListener;
import dev.karmanov.library.service.state.StateManager;
import org.example.model.UserContextEntity;
import org.example.model.UserEntity;
import org.example.repository.UserRepo;
import org.example.service.utils.UserMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CustomStateManager implements StateManager {
    private String defaultStartActionName = "start-command-method";
    private final List<StateChangeListener> stateChangeListeners = new ArrayList<>();
    private final List<RoleChangeListener> roleChangeListeners = new ArrayList<>();
    private final UserRepo userRepo;

    public CustomStateManager(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public void setNextStep(Long userId, UserContext context) {
        UserEntity user = userRepo.findById(userId).orElseGet(() -> {
            UserEntity newUser = new UserEntity();
            newUser.setUserId(userId);
            newUser.addRole("user");
            return newUser;
        });

        UserContextEntity newContext = UserMapper.mapToEntity(context);
        UserContextEntity oldContext = user.getUserContext();

        user.setUserContext(newContext);
        userRepo.save(user);

        stateChangeListeners.forEach(l -> l.onStateChange(userId, UserMapper.mapToContext(oldContext), context));
    }

    @Override
    public void addUserRole(Long userId, String... roles) {
        UserEntity user = userRepo.findById(userId).orElse(null);
        if (user == null) return;

        String oldRoles = String.join(", ", user.getRoles());

        user.addRoles(roles);

        userRepo.save(user);

        roleChangeListeners.forEach(l -> l.onRoleChange(userId, oldRoles, String.join(", ", user.getRoles())));
    }

    @Override
    public boolean removeUserRole(Long userId, String... rolesToRemove) {
        UserEntity user = userRepo.findById(userId).orElse(null);
        if (user == null) return false;

        String oldRoles = user.getRoles().toString();
        boolean result = user.removeRoles(rolesToRemove);

        userRepo.save(user);

        roleChangeListeners.forEach(l -> l.onRoleChange(userId, oldRoles, user.getRoles().toString()));
        return result;
    }

    @Override
    public Set<String> getUserRoles(Long userId) {
        return userRepo.findById(userId).map(UserEntity::getRoles).orElse(null);
    }

    @Override
    public Set<Long> getAllUserIds() {
        return userRepo.findAll().stream().map(UserEntity::getUserId).collect(Collectors.toSet());
    }

    @Override
    public Set<UserState> getStates(Long userId) {
        return userRepo.findById(userId)
                .map(UserEntity::getUserContext)
                .map(UserContextEntity::getUserStates)
                .orElse(null);
    }

    @Override
    public void resetState(Long userId) {
        setNextStep(userId, DefaultUserContext.builder()
                .addState(UserState.DEFAULT)
                .addActionData("/start")
                .build());
        addUserRole(userId, "user");
    }

    @Override
    public Set<String> getUserAction(Long userId) {
        return userRepo.findById(userId)
                .map(UserEntity::getUserContext)
                .map(UserContextEntity::getActionData)
                .orElse(null);
    }

    @Override
    public String getDefaultStartActionName() {
        return defaultStartActionName;
    }

    @Override
    public void setDefaultStartActionName(String newDefaultActionName) {
        this.defaultStartActionName = newDefaultActionName;
    }

    @Override
    public void addStateChangeListener(StateChangeListener listener) {
        stateChangeListeners.add(listener);
    }

    @Override
    public void removeStateChangeListener(StateChangeListener listener) {
        stateChangeListeners.remove(listener);
    }

    @Override
    public void addRoleChangeListener(RoleChangeListener listener) {
        roleChangeListeners.add(listener);
    }

    @Override
    public void removeRoleChangeListener(RoleChangeListener listener) {
        roleChangeListeners.remove(listener);
    }
}
