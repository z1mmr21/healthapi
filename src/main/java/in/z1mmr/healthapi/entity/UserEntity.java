package in.z1mmr.healthapi.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "users")
public class UserEntity {
    @Id
    private String id;
    private String oauthId;
    private String name;
    private String email;
    private String avatarUrl;
    private String provider;
    private String accessToken;
    private String refreshToken;
    private Role role;
}