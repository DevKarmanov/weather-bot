package org.example.repository;

import org.example.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface UserRepo extends JpaRepository<UserEntity,Long> {

    Set<UserEntity> findAllByUserIdIn(Set<Long> ids);

}
