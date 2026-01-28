package de.edvschuleplattling.irgendwieanders.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authorize -> authorize
                /*
                .requestMatchers("/", "/index.html", "/api/hello").permitAll()
                .anyRequest().authenticated()
                */

                .anyRequest().permitAll()
            )
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
        ;
        return http.build();
    }
}
