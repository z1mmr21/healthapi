package in.z1mmr.healthapi.service;

import in.z1mmr.healthapi.entity.Role;
import in.z1mmr.healthapi.entity.UserEntity;
import in.z1mmr.healthapi.repository.UserRepository;

import java.util.List;
import java.util.Optional;

public interface UserService {
    public UserEntity saveUser(UserEntity user);
    public Optional<UserEntity> findUserByOauthId(String oauthId);
    public List<UserEntity> findAllUsers();
    public Optional<UserEntity> findUserById(String id);
    public UserEntity updateUserRole(String userId, Role newRole);


}
