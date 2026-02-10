package com.visor.school.keycloak.state;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Snapshot of the existing Keycloak realm used to determine whether provisioning is required.
 */
public record RealmState(
    boolean exists,
    boolean initializedFlag,
    Set<String> realmRoles,
    Map<String, Set<String>> clientRoles,
    Map<String, Map<String, Set<String>>> composites
) {
    public RealmState {
        if (realmRoles == null) realmRoles = Collections.emptySet();
        if (clientRoles == null) clientRoles = Collections.emptyMap();
        if (composites == null) composites = Collections.emptyMap();
    }
    
    public RealmState(boolean exists, boolean initializedFlag) {
        this(exists, initializedFlag, Collections.emptySet(), Collections.emptyMap(), Collections.emptyMap());
    }
}
