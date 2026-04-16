package de.edvschuleplattling.irgendwieanders.config;

import de.edvschuleplattling.irgendwieanders.model.usermanagement.playermanagement.Useraccount;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Getter
public class UserPrincipal implements OidcUser {

    private final Useraccount account; // Dein DB Account
    private final OidcUser oidcUser; // Der originale OIDC User (Google, Keycloak, ...)

    public UserPrincipal(Useraccount account, OidcUser oidcUser) {
        this.account = account;
        this.oidcUser = oidcUser;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oidcUser.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority(account.getRole()));
    }

    @Override
    public String getName() {
        return account.getEmail();
    }

    @Override
    public Map<String, Object> getClaims() {
        return oidcUser.getClaims();
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return oidcUser.getUserInfo();
    }

    @Override
    public OidcIdToken getIdToken() {
        return oidcUser.getIdToken();
    }
}