package in.z1mmr.healthapi.security;

import in.z1mmr.healthapi.entity.UserEntity;
import in.z1mmr.healthapi.service.UserServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserServiceImpl userService;
    private final JwtTokenProvider tokenProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        DefaultOAuth2User oauthUser = (DefaultOAuth2User) authentication.getPrincipal();
        String oauthId = oauthUser.getName();

        Optional<UserEntity> userOpt = userService.findUserByOauthId(oauthId);
        if (userOpt.isPresent()) {
            UserEntity user = userOpt.get();
            String accessToken = tokenProvider.generateToken(user.getId(), user.getRole().name(), 60);
            String refreshToken = tokenProvider.generateToken(user.getId(), user.getRole().name(), 7 * 24 * 60);

            user.setAccessToken(accessToken);
            user.setRefreshToken(refreshToken);
            userService.saveUser(user);

            Cookie accessTokenCookie = new Cookie("token", accessToken);
            accessTokenCookie.setHttpOnly(true);
            accessTokenCookie.setSecure(true);
            accessTokenCookie.setPath("/");
            accessTokenCookie.setMaxAge(15 * 60);

            Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(true);
            refreshTokenCookie.setPath("/auth/refresh");
            refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60);

            response.addCookie(accessTokenCookie);
            response.addCookie(refreshTokenCookie);

            response.sendRedirect("http://localhost:8080/");
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not found");
        }
    }
}
