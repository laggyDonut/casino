package de.edvschuleplattling.irgendwieanders.config;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.context.annotation.Configuration;
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


@Configuration
@EnableWebSecurity // Aktiviert die Web-Security explizit
@EnableMethodSecurity // Optional: Nur wenn man @PreAuthorize etc. nutzen will
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

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
    LogoutSuccessHandler oidcLogoutSuccessHandler(ClientRegistrationRepository repo) {
        var h = new OidcClientInitiatedLogoutSuccessHandler(repo);
        h.setPostLogoutRedirectUri("{baseUrl}/");
        return h;
    }

}
