package de.edvschuleplattling.irgendwieanders.config;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;


@Configuration
@EnableWebSecurity // Aktiviert die Web-Security explizit
@EnableMethodSecurity // Optional: Nur wenn man @PreAuthorize etc. nutzen will
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectMapper objectMapper) throws Exception {

        http
                .authorizeHttpRequests(authorize -> authorize
                        // Öffentliche Seiten
                        .requestMatchers("/",
                                "/index.html",
                                "/login.html",
                                "/h2-console/**",
                                "/css/**",
                                "/js/**",
                                "/img/**",
                                "/error",
                                "/oauth2/**",
                                "/login/**")
                        .permitAll()
                        // Alles andere (API + u.a. dashboard.html) muss authentifiziert sein
                        .anyRequest().authenticated()
                )

                .oauth2Login(oauth2 -> oauth2
                        //.loginPage("/login.html")
                        // Nach Login zur innere.html leiten
                        .defaultSuccessUrl("/innere.html", false)
                )

                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .deleteCookies("JSESSIONID")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .permitAll()
                )

                .requestCache(cache -> cache.disable())
                .exceptionHandling(exceptions -> exceptions
                        .defaultAuthenticationEntryPointFor((request, response, authException) ->
                                        writeApiError(response, HttpStatus.UNAUTHORIZED, "Authentifizierung erforderlich.", request.getRequestURI(), objectMapper),
                                new AntPathRequestMatcher("/api/**"))
                        .defaultAccessDeniedHandlerFor((request, response, accessDeniedException) ->
                                        writeApiError(response, HttpStatus.FORBIDDEN, "Zugriff verweigert.", request.getRequestURI(), objectMapper),
                                new AntPathRequestMatcher("/api/**"))
                )

                // CSRF ist für Browser-basierte Anwendungen wichtig.
                // Wir verwenden CookieCsrfTokenRepository.withHttpOnlyFalse(), damit JavaScript das Token lesen kann.
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                        .ignoringRequestMatchers("/h2-console/**")
                )
                .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)

                // CSRF Schutz temporär(!) deaktivieren:
                .csrf(csrf -> csrf.disable())

                // H2-Console benötigt Frames; für gleiche Herkunft erlauben
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));

        return http.build();
    }

    private static final class CsrfCookieFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {
            CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
            if (csrfToken != null) {
                // Das Auslesen des Tokens erzwingt die Erzeugung des Cookies, falls CookieCsrfTokenRepository verwendet wird.
                csrfToken.getToken();
            }
            filterChain.doFilter(request, response);
        }
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    LogoutSuccessHandler oidcLogoutSuccessHandler(ClientRegistrationRepository repo) {
        var h = new OidcClientInitiatedLogoutSuccessHandler(repo);
        h.setPostLogoutRedirectUri("{baseUrl}/");
        return h;
    }

    private static void writeApiError(HttpServletResponse response, HttpStatus status, String message, String path, ObjectMapper objectMapper)
            throws IOException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", OffsetDateTime.now().toString());
        body.put("code", status.name());
        body.put("message", message);
        body.put("path", path);

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

}
