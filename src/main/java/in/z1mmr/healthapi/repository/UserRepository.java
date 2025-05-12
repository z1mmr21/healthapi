package in.z1mmr.healthapi.repository;

import in.z1mmr.healthapi.entity.UserEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<UserEntity, String> {
    Optional<UserEntity> findByOauthId(String oauthId);
}
