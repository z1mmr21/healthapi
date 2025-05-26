package in.z1mmr.healthapi.config;

import in.z1mmr.healthapi.security.CustomOAuth2UserService;
import in.z1mmr.healthapi.security.JwtAuthenticationFilter;
import in.z1mmr.healthapi.security.JwtTokenProvider;
import in.z1mmr.healthapi.security.OAuth2SuccessHandler;
import in.z1mmr.healthapi.service.UserServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.multipart.support.MultipartFilter;

@Configuration
@Profile("!test")
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)

public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserServiceImpl userService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    public SecurityConfig(CustomOAuth2UserService customOAuth2UserService, OAuth2SuccessHandler oAuth2SuccessHandler, JwtTokenProvider jwtTokenProvider, UserServiceImpl userService) {
        this.customOAuth2UserService = customOAuth2UserService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
        this.oAuth2SuccessHandler = oAuth2SuccessHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers("/error", "/webjars/**").permitAll()
                                .requestMatchers("/doctors/**").hasRole("DOCTOR")
                                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").authenticated()
                                .anyRequest().authenticated()
                )
                .oauth2Login(oauth2Login ->
                        oauth2Login
                                .loginPage("/oauth2/authorization/github")
                                .defaultSuccessUrl("/", true)
                                .userInfoEndpoint(userInfo ->
                                        userInfo.userService(customOAuth2UserService)
                                )
                                .successHandler(oAuth2SuccessHandler)
                )
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling
                                .authenticationEntryPoint((request, response, authException) ->
                                        response.sendRedirect("/oauth2/authorization/github")
                                )
                );
        return http.build();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider, userService);
    }
}