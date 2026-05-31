package brucehan.auth.infrastructure;

import brucehan.auth.application.RedisTokenManagementService;
import brucehan.auth.domain.repository.MemberRepository;
import brucehan.auth.infrastructure.kakao_client.dto.JwtProperties;
import brucehan.auth.infrastructure.service.CustomOAuth2UserService;
import brucehan.auth.infrastructure.service.CustomOidcUserService;
import brucehan.auth.middleware.JwtAuthenticationFilter;
import brucehan.auth.middleware.handler.OAuth2SuccessHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

@Slf4j
@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class JwtSecurityFilterChain {

    private final MemberRepository memberRepository;
    private final String loginSuccessUri;
    private final TokenService tokenService;
    private final TokenFactory tokenFactory;
    private final RedisTokenManagementService tokenManagementService;
    private final JwtProperties jwtProperties;

    public JwtSecurityFilterChain(
            MemberRepository memberRepository,
            @Value("${oauth2.login-success-uri}") String loginSuccessUri,
            TokenService tokenService,
            TokenFactory tokenfactory,
            RedisTokenManagementService tokenManagementService,
            JwtProperties jwtProperties
    ) {
        this.memberRepository = memberRepository;
        this.loginSuccessUri = loginSuccessUri;
        this.tokenService = tokenService;
        this.tokenFactory = tokenfactory;
        this.tokenManagementService = tokenManagementService;
        this.jwtProperties = jwtProperties;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/v1/auth/**", "/login", "/logout", "/h2-console/**"
                        ).permitAll().anyRequest().authenticated())
                .sessionManagement(configurer -> configurer
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new JwtAuthenticationFilter(tokenService, tokenManagementService, tokenFactory),
                        AuthorizationFilter.class)
                .oauth2Login(configurer -> configurer
                        .userInfoEndpoint(conf -> conf
                                .userService(customOAuth2UserService())
                                .oidcUserService(customOidcUserService()))
                        .successHandler(oAuth2SuccessHandler()))
                .cors(AbstractHttpConfigurer::disable) // 프론트 분리할 거임. 지금은 임시조치
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(PathRequest.toH2Console()))
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));
        return http.build();
    }

    // 이러한 bean 메서드가 점점 늘어나서 의존성이 많아지면 그때 파라미터 주입 방식으로 갈아타기
    @Bean
    public CustomOAuth2UserService customOAuth2UserService() {
        log.info("CustomOAuth2UserService");
        return new CustomOAuth2UserService(memberRepository);
    }

    @Bean
    public CustomOidcUserService customOidcUserService() {
        log.info("CustomOidcUserService init...");
        return new CustomOidcUserService(memberRepository);
    }

    @Bean
    public OAuth2SuccessHandler oAuth2SuccessHandler() {
        log.info("CustomOAuth2SuccessHandler init...");
        log.info(loginSuccessUri);
        return new OAuth2SuccessHandler(loginSuccessUri, tokenService, tokenFactory, jwtProperties);
    }



}
