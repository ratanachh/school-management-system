package com.visor.school.userservice.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

/**
 * Maps Keycloak JWT claims to Spring {@link GrantedAuthority} entries so {@code hasRole} /
 * {@code hasAuthority} in {@code @PreAuthorize} work like the API gateway.
 * <p>Realm roles become {@code ROLE_&lt;name&gt;} (e.g. {@code ROLE_SUPER_ADMIN}). Client roles are
 * added as {@code ROLE_&lt;clientId&gt;_&lt;role&gt;} and as the raw role name (e.g.
 * {@code MANAGE_ADMINISTRATORS}) for {@code hasAuthority('MANAGE_ADMINISTRATORS')}.</p>
 */
@Component
public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final KeycloakJwtGrantedAuthoritiesConverter authoritiesConverter =
        new KeycloakJwtGrantedAuthoritiesConverter();

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = authoritiesConverter.convert(jwt);
        return new JwtAuthenticationToken(jwt, authorities);
    }

    static final class KeycloakJwtGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

        private final JwtGrantedAuthoritiesConverter defaultConverter = new JwtGrantedAuthoritiesConverter();

        @Override
        public Collection<GrantedAuthority> convert(Jwt jwt) {
            Set<GrantedAuthority> authorities = new HashSet<>();

            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
            if (realmAccess != null) {
                List<?> roles = castList(realmAccess.get("roles"));
                for (Object role : roles) {
                    if (role != null) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                    }
                }
            }

            Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
            if (resourceAccess != null) {
                resourceAccess.forEach((clientId, access) -> {
                    if (!(access instanceof Map<?, ?> clientAccess)) {
                        return;
                    }
                    List<?> clientRoles = castList(clientAccess.get("roles"));
                    for (Object roleObj : clientRoles) {
                        if (roleObj == null) {
                            continue;
                        }
                        String role = roleObj.toString();
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + clientId + "_" + role));
                        authorities.add(new SimpleGrantedAuthority(role));
                    }
                });
            }

            Object permissionsClaim = jwt.getClaim("permissions");
            if (permissionsClaim instanceof List<?> permissionList) {
                for (Object o : permissionList) {
                    if (o != null) {
                        String permission = o.toString();
                        if (!permission.isEmpty()) {
                            authorities.add(new SimpleGrantedAuthority(permission));
                        }
                    }
                }
            }

            Collection<GrantedAuthority> scopeAuthorities = defaultConverter.convert(jwt);
            if (scopeAuthorities != null) {
                authorities.addAll(scopeAuthorities);
            }

            return new ArrayList<>(authorities);
        }

        private static List<?> castList(Object raw) {
            if (raw instanceof List<?> list) {
                return list;
            }
            return List.of();
        }
    }
}
