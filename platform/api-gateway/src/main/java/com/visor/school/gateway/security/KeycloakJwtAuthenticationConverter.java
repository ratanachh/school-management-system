package com.visor.school.gateway.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Reactive JWT authentication converter for Spring Cloud Gateway (WebFlux)
 * Extracts roles and permissions from Keycloak JWT tokens
 */
@Component
public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {

    private final KeycloakJwtGrantedAuthoritiesConverter authoritiesConverter = new KeycloakJwtGrantedAuthoritiesConverter();

    @Override
    public Mono<AbstractAuthenticationToken> convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = authoritiesConverter.convert(jwt);
        return Mono.just(new JwtAuthenticationToken(jwt, authorities));
    }

    /**
     * Converter that extracts authorities from Keycloak JWT token claims
     * Supports both realm roles and custom permissions
     */
    static class KeycloakJwtGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

        private final JwtGrantedAuthoritiesConverter defaultConverter = new JwtGrantedAuthoritiesConverter();

        @Override
        public Collection<GrantedAuthority> convert(Jwt source) {
            Set<GrantedAuthority> authorities = new HashSet<>();

            // Extract realm roles
            Map<String, Object> realmAccess = source.getClaimAsMap("realm_access");
            if (realmAccess != null) {
                List<?> roles = (List<?>) realmAccess.get("roles");
                if (roles != null) {
                    for (Object role : roles) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                    }
                }
            }

            // Extract resource access (client roles)
            Map<String, Object> resourceAccess = source.getClaimAsMap("resource_access");
            if (resourceAccess != null) {
                resourceAccess.forEach((client, access) -> {
                    if (access instanceof Map) {
                        Map<?, ?> clientAccess = (Map<?, ?>) access;
                        List<?> clientRoles = (List<?>) clientAccess.get("roles");
                        if (clientRoles != null) {
                            for (Object role : clientRoles) {
                                authorities.add(new SimpleGrantedAuthority("ROLE_" + client + "_" + role));
                            }
                        }
                    }
                });
            }

            // Extract custom permissions if present
            List<?> permissions = source.getClaim("permissions");
            if (permissions != null) {
                for (Object permission : permissions) {
                    if (permission instanceof String) {
                        authorities.add(new SimpleGrantedAuthority((String) permission));
                    }
                }
            }

            // Add standard JWT authorities
            Collection<GrantedAuthority> defaultAuthorities = defaultConverter.convert(source);
            if (defaultAuthorities != null) {
                authorities.addAll(defaultAuthorities);
            }

            return new ArrayList<>(authorities);
        }
    }
}
