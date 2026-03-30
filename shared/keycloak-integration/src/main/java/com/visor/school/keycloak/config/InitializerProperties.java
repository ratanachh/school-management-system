package com.visor.school.keycloak.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import com.visor.school.keycloak.model.ClientBlueprint;
import com.visor.school.keycloak.model.ClientRoleBlueprint;
import com.visor.school.keycloak.model.KeycloakBlueprint;
import com.visor.school.keycloak.model.RealmBlueprint;
import com.visor.school.keycloak.model.RoleBlueprint;
import com.visor.school.keycloak.model.RoleCompositeMapping;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Validated
@ConfigurationProperties(prefix = "keycloak.initializer")
public class InitializerProperties {
    private static final String DEFAULT_INITIALIZED_FLAG_KEY = "sso.system.initialized";
    private static final String DEFAULT_TRUE_VALUE = "true";
    private static final String DEFAULT_PROTOCOL = "openid-connect";

    private boolean enabled = true;
    @Valid
    @NestedConfigurationProperty
    private Admin admin = new Admin();
    @Valid
    @NestedConfigurationProperty
    private Realm realm = new Realm();
    @Valid
    private List<Client> clients = new ArrayList<>();
    @Valid
    private List<Role> realmRoles = new ArrayList<>();
    @Valid
    private List<ClientRole> clientRoles = new ArrayList<>();
    @Valid
    private List<Composite> composites = new ArrayList<>();
    @Valid
    @NestedConfigurationProperty
    private Retry retry = new Retry();

    public KeycloakBlueprint toBlueprint() {
        return new KeycloakBlueprint(
            new RealmBlueprint(
                realm.getName(),
                realm.isEnabled(),
                realm.getAttributes()
            ),
            clients.stream().map(client -> new ClientBlueprint(
                client.getClientId(),
                client.getProtocol(),
                client.isPublicClient(),
                client.isServiceAccountsEnabled(),
                client.getRedirectUris(),
                client.getWebOrigins(),
                client.getAttributes(),
                client.getSecret()
            )).toList(),
            realmRoles.stream().map(role -> new RoleBlueprint(
                role.getName(),
                role.getDescription()
            )).toList(),
            clientRoles.stream().map(role -> new ClientRoleBlueprint(
                role.getClientId(),
                role.getName(),
                role.getDescription()
            )).toList(),
            composites.stream().map(composite -> new RoleCompositeMapping(
                composite.getRealmRole(),
                composite.getClientId(),
                composite.getClientRoles()
            )).toList()
        );
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Admin getAdmin() {
        return admin;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    public Realm getRealm() {
        return realm;
    }

    public void setRealm(Realm realm) {
        this.realm = realm;
    }

    public List<Client> getClients() {
        return clients;
    }

    public void setClients(List<Client> clients) {
        this.clients = clients;
    }

    public List<Role> getRealmRoles() {
        return realmRoles;
    }

    public void setRealmRoles(List<Role> realmRoles) {
        this.realmRoles = realmRoles;
    }

    public List<ClientRole> getClientRoles() {
        return clientRoles;
    }

    public void setClientRoles(List<ClientRole> clientRoles) {
        this.clientRoles = clientRoles;
    }

    public List<Composite> getComposites() {
        return composites;
    }

    public void setComposites(List<Composite> composites) {
        this.composites = composites;
    }

    public Retry getRetry() {
        return retry;
    }

    public void setRetry(Retry retry) {
        this.retry = retry;
    }

    public static class Admin {
        @NotBlank
        private String url = "http://localhost:8080";
        @NotBlank
        private String username = "admin";
        @NotBlank
        private String password = "admin";
        @NotBlank
        private String clientId = "admin-cli";

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }
    }

    public static class Realm {
        @NotBlank
        private String name = "school-management";
        private boolean enabled = true;
        private Map<String, String> attributes = new HashMap<>();

        public Realm() {
            attributes.put(DEFAULT_INITIALIZED_FLAG_KEY, DEFAULT_TRUE_VALUE);
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public Map<String, String> getAttributes() {
            return attributes;
        }

        public void setAttributes(Map<String, String> attributes) {
            this.attributes = attributes;
        }
    }

    public static class Client {
        @NotBlank
        private String clientId;
        @NotBlank
        private String protocol = DEFAULT_PROTOCOL;
        private boolean publicClient = false;
        private boolean serviceAccountsEnabled = true;
        private List<String> redirectUris = new ArrayList<>();
        private List<String> webOrigins = new ArrayList<>();
        private Map<String, String> attributes = new HashMap<>();
        private String secret;

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getProtocol() {
            return protocol;
        }

        public void setProtocol(String protocol) {
            this.protocol = protocol;
        }

        public boolean isPublicClient() {
            return publicClient;
        }

        public void setPublicClient(boolean publicClient) {
            this.publicClient = publicClient;
        }

        public boolean isServiceAccountsEnabled() {
            return serviceAccountsEnabled;
        }

        public void setServiceAccountsEnabled(boolean serviceAccountsEnabled) {
            this.serviceAccountsEnabled = serviceAccountsEnabled;
        }

        public List<String> getRedirectUris() {
            return redirectUris;
        }

        public void setRedirectUris(List<String> redirectUris) {
            this.redirectUris = redirectUris;
        }

        public List<String> getWebOrigins() {
            return webOrigins;
        }

        public void setWebOrigins(List<String> webOrigins) {
            this.webOrigins = webOrigins;
        }

        public Map<String, String> getAttributes() {
            return attributes;
        }

        public void setAttributes(Map<String, String> attributes) {
            this.attributes = attributes;
        }

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }
    }

    public static class Role {
        @NotBlank
        private String name;
        private String description;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    public static class ClientRole {
        @NotBlank
        private String clientId;
        @NotBlank
        private String name;
        private String description;

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    public static class Composite {
        @NotBlank
        private String realmRole;
        @NotBlank
        private String clientId;
        @Valid
        private List<String> clientRoles;

        public String getRealmRole() {
            return realmRole;
        }

        public void setRealmRole(String realmRole) {
            this.realmRole = realmRole;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public List<String> getClientRoles() {
            return clientRoles;
        }

        public void setClientRoles(List<String> clientRoles) {
            this.clientRoles = clientRoles;
        }
    }

    public static class Retry {
        @Min(1)
        private int maxAttempts = 5;
        @Min(100)
        private long initialBackoffMillis = 2_000L;
        @DecimalMin("1.0")
        private double multiplier = 1.5;
        private boolean failOnError = false;

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public long getInitialBackoffMillis() {
            return initialBackoffMillis;
        }

        public void setInitialBackoffMillis(long initialBackoffMillis) {
            this.initialBackoffMillis = initialBackoffMillis;
        }

        public double getMultiplier() {
            return multiplier;
        }

        public void setMultiplier(double multiplier) {
            this.multiplier = multiplier;
        }

        public boolean isFailOnError() {
            return failOnError;
        }

        public void setFailOnError(boolean failOnError) {
            this.failOnError = failOnError;
        }
    }
}
