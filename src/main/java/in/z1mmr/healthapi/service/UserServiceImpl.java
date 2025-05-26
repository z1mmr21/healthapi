package in.z1mmr.healthapi.service;

import in.z1mmr.healthapi.entity.Role;
import in.z1mmr.healthapi.entity.UserEntity;
import in.z1mmr.healthapi.repository.UserRepository;
import in.z1mmr.healthapi.security.JwtTokenProvider;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

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

    @Override
    public Optional<UserEntity> findUserById(String id) {
        return userRepository.findById(id);
    }

    @Override
    public UserEntity updateUserRole(String userId, Role newRole) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        user.setRole(newRole);

        String accessToken = jwtTokenProvider.generateToken(user.getId(), user.getRole().name(), 60);
        String refreshToken = jwtTokenProvider.generateToken(user.getId(), user.getRole().name(), 7 * 24 * 60);

        user.setAccessToken(accessToken);
        user.setRefreshToken(refreshToken);

        return userRepository.save(user);
    }

}