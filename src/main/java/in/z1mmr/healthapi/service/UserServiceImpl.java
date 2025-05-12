package in.z1mmr.healthapi.service;

import in.z1mmr.healthapi.entity.UserEntity;
import in.z1mmr.healthapi.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserEntity saveUser(UserEntity user) {
        return userRepository.save(user);
    }

    @Override
    public Optional<UserEntity> findUserByOauthId(String oauthId) {
        return userRepository.findByOauthId(oauthId);
    }

    @Override
    public List<UserEntity> findAllUsers() {
        return userRepository.findAll();
    }

}
