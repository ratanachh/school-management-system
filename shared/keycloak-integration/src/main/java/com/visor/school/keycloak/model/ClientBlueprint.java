package com.visor.school.keycloak.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public record ClientBlueprint(
    String clientId,
    String protocol,
    boolean publicClient,
    boolean serviceAccountsEnabled,
    List<String> redirectUris,
    List<String> webOrigins,
    Map<String, String> attributes,
    String secret
) {
    public ClientBlueprint {
        if (protocol == null) protocol = "openid-connect";
        if (redirectUris == null) redirectUris = Collections.emptyList();
        if (webOrigins == null) webOrigins = Collections.emptyList();
        if (attributes == null) attributes = Collections.emptyMap();
    }

    public ClientBlueprint(String clientId) {
        this(clientId, "openid-connect", false, true, Collections.emptyList(), Collections.emptyList(), Collections.emptyMap(), null);
    }
}
