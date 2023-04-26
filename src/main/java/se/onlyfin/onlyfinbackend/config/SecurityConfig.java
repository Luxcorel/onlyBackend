package se.onlyfin.onlyfinbackend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import se.onlyfin.onlyfinbackend.service.OnlyfinUserDetailsService;

/**
 * This class is used to configure the security settings in Spring Security.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final OnlyfinUserDetailsService onlyfinUserDetailsService;

    @Autowired
    public SecurityConfig(OnlyfinUserDetailsService onlyfinUserDetailsService) {
        this.onlyfinUserDetailsService = onlyfinUserDetailsService;
    }

    /**
     * This method is used to configure which endpoints are protected by roles and which are not.
     * It is here that you can see which endpoints need authentication and which do not.
     * This method also sets up the login form.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors()
                .and().requiresChannel(channel ->
                        channel.anyRequest().requiresSecure())
                .csrf().disable()
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers(
                                "/user",
                                "/search-all-analysts",
                                "/get-analyst-by-name",
                                "/search-analyst",
                                "/subscribe",
                                "/unsubscribe",
                                "/enable-analyst",
                                "/disable-analyst",
                                "/fetch-current-user-id",
                                "/fetch-about-me",
                                "/update-about-me",
                                "/dashboard/**",
                                "/studio/**",
                                "/studio/deleteStock/**",
                                "/studio/deleteCategory/**",
                                "/studio/deleteModule/**",
                                "/principal-username",
                                "/principal-id",
                                "/feed/**",
                                "/fetch-current-user-subscriptions",
                                "/stonks/**",
                                "/search-analyst-include-sub-info",
                                "/search-all-analysts-include-sub-info",
                                "/fetch-about-me-with-sub-info",
                                "/user-subscription-list-sorted-by-postdate",
                                "/user-subscription-list-sorted-by-update-date",
                                "/algo/**",
                                "/find-analysts-that-cover-stock"
                        )
                        .hasRole("USER")
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/",
                                "/register",
                                "/plz",
                                "/login",
                                "/assets/**"
                        )
                        .permitAll()
                        //uncomment the row below to enable user debug:
                        .requestMatchers("/user-debug").permitAll()
                )
                .formLogin().loginProcessingUrl("/plz");
        return http.build();
    }

    /**
     * This method can be used to disable authentication globally for testing purposes.
     * It should not be used in production.
     */
    /*
    @Bean
    public WebSecurityCustomizer ignoringCustomizer() {
        //DISABLE AUTH GLOBALLY
        return (web) -> web.ignoring().requestMatchers("/**");

    }

     */

    /**
     * This method is used to configure which password encoder to use.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**").allowedOrigins("https://onlyfrontend-production.up.railway.app").allowCredentials(true);
            }
        };
    }


}
