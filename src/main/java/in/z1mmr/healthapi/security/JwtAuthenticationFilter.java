package in.z1mmr.healthapi.security;

import in.z1mmr.healthapi.service.UserServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserServiceImpl userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws IOException, jakarta.servlet.ServletException {

        System.out.println("JwtAuthenticationFilter CALLED: " + request.getMethod() + " " + request.getRequestURI());

        String token = resolveToken(request);

        if (token != null && jwtTokenProvider.validateToken(token)) {
            String userId = jwtTokenProvider.getUserIdFromToken(token);
            String role = jwtTokenProvider.getRoleFromToken(token);

            userService.findUserById(userId).ifPresent(user -> {
                var authority = new SimpleGrantedAuthority("ROLE_" + role);
                var auth = new UsernamePasswordAuthenticationToken(user, null, List.of(authority));
                SecurityContextHolder.getContext().setAuthentication(auth);

                System.out.println("Authorization header token: " + token);
                System.out.println("User ID: " + userId + ", Role: " + role);
                System.out.println("SecurityContext auth: " + SecurityContextHolder.getContext().getAuthentication());
                System.out.println("Granted Authorities: " + auth.getAuthorities());
            });

        } else {
            String refreshToken = resolveRefreshToken(request);
            if (refreshToken != null && jwtTokenProvider.validateToken(refreshToken)) {
                String userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
                userService.findUserById(userId).ifPresent(user -> {
                    var authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());
                    var auth = new UsernamePasswordAuthenticationToken(user, null, List.of(authority));
                    SecurityContextHolder.getContext().setAuthentication(auth);

                    System.out.println("Refresh token from cookie: " + refreshToken);
                    System.out.println("User ID: " + userId + ", Role: " + user.getRole().name());
                    System.out.println("SecurityContext auth: " + SecurityContextHolder.getContext().getAuthentication());
                    System.out.println("Granted Authorities: " + auth.getAuthorities());
                });
            }

            System.out.println("Authorization header token: " + token);
            System.out.println("Refresh token from cookie: " + refreshToken);
            System.out.println("SecurityContext auth: " + SecurityContextHolder.getContext().getAuthentication());
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }

    private String resolveRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }


}
