package com.visor.school.gateway.security

import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 * Reactive JWT authentication converter for Spring Cloud Gateway (WebFlux)
 * Extracts roles and permissions from Keycloak JWT tokens
 */
@Component
class KeycloakJwtAuthenticationConverter : Converter<Jwt, Mono<AbstractAuthenticationToken>> {
    
    private val authoritiesConverter = KeycloakJwtGrantedAuthoritiesConverter()
    
    override fun convert(jwt: Jwt): Mono<AbstractAuthenticationToken> {
        val authorities = authoritiesConverter.convert(jwt)
        return Mono.just(JwtAuthenticationToken(jwt, authorities))
    }
}

/**
 * Converter that extracts authorities from Keycloak JWT token claims
 * Supports both realm roles and custom permissions
 */
class KeycloakJwtGrantedAuthoritiesConverter : Converter<Jwt, Collection<GrantedAuthority>> {
    
    private val defaultConverter = JwtGrantedAuthoritiesConverter()
    
    override fun convert(source: Jwt): Collection<GrantedAuthority> {
        val authorities = mutableSetOf<GrantedAuthority>()

        // Extract realm roles
        val realmAccess = source.getClaimAsMap("realm_access")
        val roles = realmAccess?.get("roles") as? List<*>
        roles?.forEach { role ->
            authorities.add(SimpleGrantedAuthority("ROLE_$role"))
        }

        // Extract resource access (client roles)
        val resourceAccess = source.getClaimAsMap("resource_access")
        resourceAccess?.forEach { (client, access) ->
            val clientAccess = access as? Map<*, *>
            val clientRoles = clientAccess?.get("roles") as? List<*>
            clientRoles?.forEach { role ->
                authorities.add(SimpleGrantedAuthority("ROLE_${client}_$role"))
            }
        }

        // Extract custom permissions if present
        val permissions = source.getClaim<List<*>>("permissions")
        permissions?.forEach { permission ->
            if (permission is String) {
                authorities.add(SimpleGrantedAuthority(permission))
            }
        }

        // Add standard JWT authorities
        defaultConverter.convert(source)?.let { defaultAuthorities ->
            authorities.addAll(defaultAuthorities)
        }

        return authorities
    }
}

