package in.z1mmr.healthapi.security;

import in.z1mmr.healthapi.entity.UserEntity;
import in.z1mmr.healthapi.repository.UserRepository;
import in.z1mmr.healthapi.service.UserServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {


    private final UserServiceImpl userService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String oauthId = oAuth2User.getName();
        String provider = userRequest.getClientRegistration().getRegistrationId();
        String name = (String) oAuth2User.getAttributes().get("name");
        String email = (String) oAuth2User.getAttributes().get("email");
        String avatarUrl = (String) oAuth2User.getAttributes().get("avatar_url");

        Optional<UserEntity> existingUser = userService.findUserByOauthId(oauthId);
        if (!existingUser.isPresent()) {
            UserEntity newUser = UserEntity.builder()
                    .oauthId(oauthId)
                    .provider(provider)
                    .name(name)
                    .email(email)
                    .avatarUrl(avatarUrl)
                    .build();
            userService.saveUser(newUser);
        }

        return oAuth2User;
    }
}