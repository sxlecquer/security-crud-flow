package com.example.client.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
    @Autowired
    @SuppressWarnings("all")
    private DataSource dataSource;

    @Value("${session.key}")
    private String sessionKey;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(11);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorizeHttpRequest ->
                        authorizeHttpRequest
//                                .requestMatchers("**").permitAll())
                                .requestMatchers("/home").hasRole("USER")
                                .requestMatchers("/register/verify-email").hasRole("USER_NOT_VERIFIED")
                                .requestMatchers("/images/**", "/login/**", "/register/**").permitAll()
                                .anyRequest().authenticated())
                .formLogin(formLogin -> formLogin
                        .loginPage("/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/login/process", true)
                        .permitAll())
                .oauth2Login(oauth2Login -> oauth2Login
                        .loginPage("/login"))
                /*.sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
                        .invalidSessionUrl("/login?logout")
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false))*/
                .rememberMe(rememberMe -> rememberMe
                        .tokenRepository(persistentTokenRepository())
                        .key(sessionKey)
                        .tokenValiditySeconds(300))
                .logout(logout -> logout
                        .deleteCookies("JSESSIONID")
                        .invalidateHttpSession(true));

        return http.build();
    }

    @Bean
    public PersistentTokenRepository persistentTokenRepository() {
        JdbcTokenRepositoryImpl jdbcTokenRepository = new JdbcTokenRepositoryImpl();
        jdbcTokenRepository.setDataSource(dataSource);
        return jdbcTokenRepository;
    }

    /*@Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        return new InMemoryClientRegistrationRepository(this.googleClientRegistration(), this.githubClientRegistration());
    }*/

    /*private ClientRegistration googleClientRegistration() {
        return CommonOAuth2Provider.GOOGLE
                .getBuilder("google")
                .clientId("948355728137-ad9k5aguqka54f1796cudbvst64p8dgu.apps.googleusercontent.com")
                .clientSecret("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")
                .build();
    }*/

    /*private ClientRegistration githubClientRegistration() {
        return CommonOAuth2Provider.GITHUB
                .getBuilder("github")
                .clientId("e715d1bd2388a137413a")
                .clientSecret("c018620125b740207c83b27ee3f4af72deeb9ee6")
                .scope("user:email")
                .build();
        return ClientRegistration.withRegistrationId("oidc-client")
                .clientId("oidc-client")
                .clientSecret("secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/oidc-client")
                .scope("openid", "profile")
                .authorizationUri("http://auth-server:9000/oauth2/authorize")
                .tokenUri("http://auth-server:9000/oauth2/token")
                .jwkSetUri("http://auth-server:9000/oauth2/jwks")
                .issuerUri("http://auth-server:9000")
                .clientName("OIDC-Client")
                .build();
    }*/
}
