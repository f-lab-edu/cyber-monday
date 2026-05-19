package brucehan.auth.infrastructure;

import brucehan.auth.domain.repository.MemberRepository;
import brucehan.auth.infrastructure.service.CustomOAuth2UserService;
import brucehan.auth.infrastructure.service.CustomOidcUserService;
import brucehan.auth.middleware.handler.OAuth2SuccessHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class JwtSecurityFilterChain {

    private final MemberRepository memberRepository;
    private final String loginSuccessUri;

    public JwtSecurityFilterChain(
            MemberRepository memberRepository,
           @Value("${oauth2.login-success-uri}") String loginSuccessUri
    ) {
        this.memberRepository = memberRepository;
        this.loginSuccessUri = loginSuccessUri;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/v1/auth/**"
                        ).permitAll().anyRequest().authenticated())
                .sessionManagement(configurer -> configurer
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .oauth2Login(configurer -> configurer
                        .userInfoEndpoint(conf -> conf
                                .userService(customOAuth2UserService())
                                .oidcUserService(customOidcUserService()))
                        .successHandler(oAuth2SuccessHandler()))
                .cors(AbstractHttpConfigurer::disable) // 프론트 분리할 거임. 지금은 임시조치
                .csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }

    // 이러한 bean 메서드가 점점 늘어나서 의존성이 많아지면 그때 파라미터 주입 방식으로 갈아타기
    @Bean
    public CustomOAuth2UserService customOAuth2UserService() {
        return new CustomOAuth2UserService(memberRepository);
    }

    @Bean
    public CustomOidcUserService customOidcUserService() {
        return new CustomOidcUserService(memberRepository);
    }

    @Bean
    public OAuth2SuccessHandler oAuth2SuccessHandler() {
        return new OAuth2SuccessHandler(loginSuccessUri);
    }



}
