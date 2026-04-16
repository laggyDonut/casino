package de.edvschuleplattling.irgendwieanders.config;

import de.edvschuleplattling.irgendwieanders.model.usermanagement.playermanagement.Role;
import de.edvschuleplattling.irgendwieanders.model.usermanagement.playermanagement.Useraccount;
import de.edvschuleplattling.irgendwieanders.repository.UseraccountRepository;
import de.edvschuleplattling.irgendwieanders.service.UseraccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * OIDC-UserService für ALLE OIDC-Provider (z. B. Keycloak, Google).
 * Delegiert zunächst an die Standard-OidcUserService-Implementierung und
 * synchronisiert anschließend unseren Account in der Datenbank und wrapped in unseren UserPrincipal.
 */
@Service
@RequiredArgsConstructor
public class OidcProvisioningUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private final UseraccountService accountService;
    private final OidcUserService delegate = new OidcUserService();
    private final UseraccountRepository accountRepository;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        // Standard OIDC-User laden (Keycloak, Google, ...)
        OidcUser oidcUser = delegate.loadUser(userRequest);

        // Email aus den Claims lesen (Scope "email" ist in application.yml konfiguriert)
        String email = oidcUser.getEmail();
        if (email == null) {
            email = (String) oidcUser.getClaims().get("email");
        }

        // Falls keine Email vorhanden ist, einfach den Original-User zurückgeben
        if (email == null || email.isBlank()) {
            return oidcUser;
        }

        // Account in der DB finden oder anlegen/aktualisieren
        Optional<Useraccount> accountOpt = accountRepository.findByEmail(email);
        if (accountOpt.isEmpty()) {
            // Account anlegen

            // erst Rolle ermitteln.
            Role role = Role.GAMER; // default Rolle
//            Map<String, Object> claims = oidcUser.getUserInfo().getClaims();
//            if (claims != null
//                    && claims.containsKey("groups")) { // bei google null, bei keycloak ArrayList von z.B. ["fs2024", "fs2024_mail"] bei Schülern oder "Lehrer" bei Lehrern
//                if (claims.get("groups") instanceof List list) {
//                    if (list.contains("Lehrer")) {
//                        role = "ROLE_TEACHER";
//                    }
//                }
//            }

            Useraccount account = accountService.anlegen(email, role);
            return new UserPrincipal(account, oidcUser);
        } else {
            return new UserPrincipal(accountOpt.get(), oidcUser);
        }


    }
}
