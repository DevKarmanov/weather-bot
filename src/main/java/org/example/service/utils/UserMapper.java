package org.example.service.utils;

import dev.karmanov.library.model.user.DefaultUserContext;
import dev.karmanov.library.model.user.UserContext;
import org.example.model.UserContextEntity;

public class UserMapper {
    public static UserContextEntity mapToEntity(UserContext context) {
        if (context == null) return null;

        UserContextEntity entity = new UserContextEntity();
        entity.setUserStates(context.getUserStates());
        entity.setActionData(context.getActionData());
        return entity;
    }

    public static UserContext mapToContext(UserContextEntity entity) {
        if (entity == null) return null;

        return DefaultUserContext.builder()
                .addState(entity.getUserStates())
                .addActionData(entity.getActionData())
                .build();
    }
}
