package com.visor.school.keycloak.model;

import java.util.Collections;
import java.util.Map;

public record RealmBlueprint(
    String name,
    boolean enabled,
    Map<String, String> attributes
) {
    public RealmBlueprint {
        if (attributes == null) attributes = Collections.emptyMap();
    }

    public RealmBlueprint(String name) {
        this(name, true, Collections.emptyMap());
    }
}
