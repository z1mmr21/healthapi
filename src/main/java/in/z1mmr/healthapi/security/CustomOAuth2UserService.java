package in.z1mmr.healthapi.security;

import in.z1mmr.healthapi.entity.Role;
import in.z1mmr.healthapi.entity.UserEntity;
import in.z1mmr.healthapi.service.UserServiceImpl;
import lombok.AllArgsConstructor;
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
    private final JwtTokenProvider tokenProvider;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = new DefaultOAuth2UserService().loadUser(request);

        String oauthId = oauth2User.getName();
        String provider = request.getClientRegistration().getRegistrationId();
        String name = (String) oauth2User.getAttributes().get("name");
        String email = (String) oauth2User.getAttributes().get("email");
        String avatarUrl = (String) oauth2User.getAttributes().get("avatar_url");

        UserEntity user;

        Optional<UserEntity> userOpt = userService.findUserByOauthId(oauthId);
        if (userOpt.isEmpty()) {
            Role assignedRole = userService.findAllUsers().isEmpty() ? Role.ADMIN : Role.NONE; // Если это первый пользователь, даем роль ADMIN

            user = UserEntity.builder()
                    .oauthId(oauthId)
                    .provider(provider)
                    .name(name)
                    .email(email)
                    .avatarUrl(avatarUrl)
                    .role(assignedRole)
                    .build();

            userService.saveUser(user);
        } else {
            user = userOpt.get();
            user.setName(name);
            user.setEmail(email);
            user.setAvatarUrl(avatarUrl);
        }

        return oauth2User;
    }
}
