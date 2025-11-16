# Tasks: Keycloak Automation for School Management System

**Input**: Design documents from `/specs/001-school-management-system/`  
**Purpose**: Automate Keycloak realm and client initialization instead of manual configuration  
**Prerequisites**: Keycloak server running, admin credentials available

**Organization**: Tasks are organized by initialization phase (realm → client → roles → permissions → validation)

## Format: `[ID] [P?] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- Include exact file paths in descriptions

## Phase 1: Keycloak Realm Initialization

**Purpose**: Create and configure the school-management realm

- [X] TKC001 Create KeycloakAdminClient utility class for Keycloak Admin REST API interactions in scripts/keycloak/KeycloakAdminClient.kt
- [X] TKC002 Implement getAdminToken method to authenticate with Keycloak master realm in scripts/keycloak/KeycloakAdminClient.kt
- [X] TKC003 Implement createRealm method to create school-management realm with realm configuration in scripts/keycloak/KeycloakAdminClient.kt
- [X] TKC004 Add realm configuration: enabled=true, registrationAllowed=false, loginWithEmailAllowed=true in scripts/keycloak/KeycloakAdminClient.kt
- [X] TKC005 Add email configuration settings (SMTP settings from environment variables) to realm configuration in scripts/keycloak/KeycloakAdminClient.kt
- [X] TKC006 Add token settings: accessTokenLifespan=300, ssoSessionIdleTimeout=1800, ssoSessionMaxLifespan=36000 in scripts/keycloak/KeycloakAdminClient.kt
- [X] TKC007 Implement checkRealmExists method to check if realm already exists (idempotency) in scripts/keycloak/KeycloakAdminClient.kt
- [X] TKC008 Add error handling for realm creation (409 conflict if exists, retry logic) in scripts/keycloak/KeycloakAdminClient.kt

## Phase 2: OAuth2 Client Configuration

**Purpose**: Create and configure the school-management-client OAuth2 client

- [X] TKC009 Implement createClient method to create OAuth2 client in school-management realm in scripts/keycloak/KeycloakAdminClient.kt
- [X] TKC010 Configure client: clientId=school-management-client, enabled=true, clientProtocol=openid-connect in scripts/keycloak/KeycloakAdminClient.kt
- [X] TKC011 Configure client access type: public (or confidential with secret) based on environment in scripts/keycloak/KeycloakAdminClient.kt
- [X] TKC012 Add valid redirect URIs: http://localhost:8080/*, https://api.schoolmanagement.edu/* (configurable) in scripts/keycloak/KeycloakAdminClient.kt
- [X] TKC013 Add web origins: + (allow all origins for development, configurable for production) in scripts/keycloak/KeycloakAdminClient.kt
- [X] TKC014 Enable standard flow, direct access grants, service accounts for client in scripts/keycloak/KeycloakAdminClient.kt
- [X] TKC015 Configure client scopes: openid, profile, email, roles, microprofile-jwt in scripts/keycloak/KeycloakAdminClient.kt
- [X] TKC016 Implement checkClientExists method to check if client already exists (idempotency) in scripts/keycloak/KeycloakAdminClient.kt
- [X] TKC017 Add error handling for client creation (409 conflict if exists, update existing client) in scripts/keycloak/KeycloakAdminClient.kt
- [X] TKC018 Implement getClientSecret method to retrieve and display client secret after creation in scripts/keycloak/KeycloakAdminClient.kt

## Phase 3: Realm Roles Creation

**Purpose**: Create standard roles (ADMINISTRATOR, TEACHER, STUDENT, PARENT)

- [X] TKC019 Implement createRealmRole method to create realm roles in scripts/keycloak/KeycloakAdminClient.kt
- [X] TKC020 Create ADMINISTRATOR role with description "System administrator with full access" in scripts/keycloak/KeycloakAdminClient.kt
- [X] TKC021 Create TEACHER role with description "Teacher with class and grade management permissions" in scripts/keycloak/KeycloakAdminClient.kt
- [X] TKC022 Create STUDENT role with description "Student with read access to own records" in scripts/keycloak/KeycloakAdminClient.kt
- [X] TKC023 Create PARENT role with description "Parent with access to children's records" in scripts/keycloak/KeycloakAdminClient.kt
- [X] TKC024 Implement checkRoleExists method to check if role already exists (idempotency) in scripts/keycloak/KeycloakAdminClient.kt
- [X] TKC025 Add error handling for role creation (skip if exists, update if needed) in scripts/keycloak/KeycloakAdminClient.kt
- [X] TKC026 Implement assignRoleToClient method to assign roles to client for token inclusion in scripts/keycloak/KeycloakAdminClient.kt

## Phase 4: Custom Permissions Configuration

**Purpose**: Configure fine-grained permissions for access control

- [X] TKC027 Research Keycloak permission model (realm roles vs client roles vs custom attributes) for permissions in scripts/keycloak/KeycloakAdminClient.kt
- [X] TKC028 Implement createClientRole method to create client-scoped roles for permissions in scripts/keycloak/KeycloakAdminClient.kt
- [X] TKC029 Create COLLECT_ATTENDANCE permission role with description "Permission to collect attendance as class leader" in scripts/keycloak/KeycloakAdminClient.kt
- [X] TKC030 Create MANAGE_ATTENDANCE permission role with description "Permission to manage attendance records" in scripts/keycloak/KeycloakAdminClient.kt
- [X] TKC031 Create COLLECT_EXAM_RESULTS permission role with description "Permission to collect exam results (class teachers grades 7-12)" in scripts/keycloak/KeycloakAdminClient.kt
- [X] TKC032 Create SUBMIT_REPORTS permission role with description "Permission to submit reports to school administration" in scripts/keycloak/KeycloakAdminClient.kt
- [X] TKC033 Create VIEW_ALL_STUDENTS permission role with description "Permission to view all student records" in scripts/keycloak/KeycloakAdminClient.kt
- [X] TKC034 Create MANAGE_GRADES permission role with description "Permission to record and modify grades" in scripts/keycloak/KeycloakAdminClient.kt
- [X] TKC035 Create MANAGE_HOMEROOM permission role with description "Permission to manage homeroom class (grades 1-6)" in scripts/keycloak/KeycloakAdminClient.kt
- [X] TKC036 Implement assignPermissionToRole method to map permissions to realm roles (e.g., ADMINISTRATOR gets all permissions) in scripts/keycloak/KeycloakAdminClient.kt
- [X] TKC037 Configure permission-role mappings: ADMINISTRATOR → all permissions, TEACHER → MANAGE_ATTENDANCE, MANAGE_GRADES in scripts/keycloak/KeycloakAdminClient.kt
- [X] TKC038 Add error handling for permission role creation (skip if exists, update if needed) in scripts/keycloak/KeycloakAdminClient.kt

## Phase 5: Main Initialization Script

**Purpose**: Create main script that orchestrates all initialization steps

- [X] TKC039 Create main Keycloak initialization script scripts/setup-keycloak.sh with orchestration logic
- [X] TKC040 Add environment variable validation: KEYCLOAK_URL, KEYCLOAK_ADMIN, KEYCLOAK_ADMIN_PASSWORD in scripts/setup-keycloak.sh
- [X] TKC041 Add Keycloak health check: wait for Keycloak to be ready before initialization in scripts/setup-keycloak.sh
- [X] TKC042 Integrate realm creation step in main initialization flow in scripts/setup-keycloak.sh
- [X] TKC043 Integrate client creation step in main initialization flow in scripts/setup-keycloak.sh
- [X] TKC044 Integrate realm roles creation step in main initialization flow in scripts/setup-keycloak.sh
- [X] TKC045 Integrate permission roles creation step in main initialization flow in scripts/setup-keycloak.sh
- [X] TKC046 Add idempotency check: skip steps if already configured (check before create) in scripts/setup-keycloak.sh
- [X] TKC047 Add progress logging: log each step completion with success/failure status in scripts/setup-keycloak.sh
- [X] TKC048 Add error handling: rollback on failure or continue with warnings based on step criticality in scripts/setup-keycloak.sh
- [X] TKC049 Add summary output: display created realm, client, roles, and permissions at end of script in scripts/setup-keycloak.sh
- [X] TKC050 Add client secret output: display client secret for configuration in services in scripts/setup-keycloak.sh

## Phase 6: Kotlin Implementation (Alternative to Shell Script)

**Purpose**: Create Kotlin-based initialization for better type safety and error handling

- [ ] TKC051 Create Kotlin project structure for Keycloak initialization in scripts/keycloak/build.gradle.kts
- [ ] TKC052 Add Keycloak Admin Client dependency (org.keycloak:keycloak-admin-client) in scripts/keycloak/build.gradle.kts
- [ ] TKC053 Create KeycloakInitializer main class in scripts/keycloak/src/main/kotlin/com/visor/school/keycloak/KeycloakInitializer.kt
- [ ] TKC054 Implement realm initialization logic using KeycloakAdminClient in scripts/keycloak/src/main/kotlin/com/visor/school/keycloak/KeycloakInitializer.kt
- [ ] TKC055 Implement client initialization logic using KeycloakAdminClient in scripts/keycloak/src/main/kotlin/com/visor/school/keycloak/KeycloakInitializer.kt
- [ ] TKC056 Implement roles initialization logic using KeycloakAdminClient in scripts/keycloak/src/main/kotlin/com/visor/school/keycloak/KeycloakInitializer.kt
- [ ] TKC057 Implement permissions initialization logic using KeycloakAdminClient in scripts/keycloak/src/main/kotlin/com/visor/school/keycloak/KeycloakInitializer.kt
- [ ] TKC058 Add configuration file support (application.yml or application.properties) for Keycloak settings in scripts/keycloak/src/main/resources/application.yml
- [ ] TKC059 Add command-line argument parsing for Keycloak URL, admin credentials in scripts/keycloak/src/main/kotlin/com/visor/school/keycloak/KeycloakInitializer.kt
- [ ] TKC060 Add idempotency checks: verify existing configuration before creating in scripts/keycloak/src/main/kotlin/com/visor/school/keycloak/KeycloakInitializer.kt

## Phase 7: Validation and Testing

**Purpose**: Verify Keycloak initialization and add tests

- [ ] TKC061 Create validation script to verify realm exists and is configured correctly in scripts/keycloak/validate-keycloak.sh
- [ ] TKC062 Create validation script to verify client exists and has correct OAuth2 settings in scripts/keycloak/validate-keycloak.sh
- [ ] TKC063 Create validation script to verify all roles exist in realm in scripts/keycloak/validate-keycloak.sh
- [ ] TKC064 Create validation script to verify all permission roles exist and are mapped correctly in scripts/keycloak/validate-keycloak.sh
- [ ] TKC065 Create integration test: test realm creation with Testcontainers Keycloak in scripts/keycloak/src/test/kotlin/com/visor/school/keycloak/KeycloakInitializationTest.kt
- [ ] TKC066 Create integration test: test client creation and OAuth2 token generation in scripts/keycloak/src/test/kotlin/com/visor/school/keycloak/KeycloakInitializationTest.kt
- [ ] TKC067 Create integration test: test roles creation and role assignment in scripts/keycloak/src/test/kotlin/com/visor/school/keycloak/KeycloakInitializationTest.kt
- [ ] TKC068 Create integration test: test permissions creation and permission-role mapping in scripts/keycloak/src/test/kotlin/com/visor/school/keycloak/KeycloakInitializationTest.kt
- [ ] TKC069 Create integration test: test idempotency (running script multiple times produces same result) in scripts/keycloak/src/test/kotlin/com/visor/school/keycloak/KeycloakInitializationTest.kt
- [ ] TKC070 Add unit tests for KeycloakAdminClient methods (mocked Keycloak Admin API) in scripts/keycloak/src/test/kotlin/com/visor/school/keycloak/KeycloakAdminClientTest.kt

## Phase 8: Documentation

**Purpose**: Document Keycloak initialization process

- [ ] TKC071 Create README.md for Keycloak initialization in scripts/keycloak/README.md
- [ ] TKC072 Document environment variables required for initialization in scripts/keycloak/README.md
- [ ] TKC073 Document realm configuration settings and their purposes in scripts/keycloak/README.md
- [ ] TKC074 Document client configuration and OAuth2 flow in scripts/keycloak/README.md
- [ ] TKC075 Document roles and their intended usage in scripts/keycloak/README.md
- [ ] TKC076 Document permissions and their mapping to roles in scripts/keycloak/README.md
- [ ] TKC077 Add troubleshooting guide for common Keycloak initialization errors in scripts/keycloak/README.md
- [ ] TKC078 Add examples: how to run initialization script, how to verify setup in scripts/keycloak/README.md
- [ ] TKC079 Update main setup.sh script to include Keycloak initialization step in scripts/setup.sh
- [ ] TKC080 Update docker-compose.yml documentation to include Keycloak initialization in docker/README.md

## Summary

**Total Tasks**: 80  
**Parallel Opportunities**: Tasks within each phase can run in parallel where they don't depend on each other  
**Dependencies**: 
- Phase 1 (Realm) must complete before Phase 2 (Client)
- Phase 2 (Client) must complete before Phase 3 (Roles)
- Phase 3 (Roles) must complete before Phase 4 (Permissions)
- Phase 5 (Main Script) depends on Phases 1-4
- Phase 6 (Kotlin Implementation) is alternative to shell script approach
- Phase 7 (Testing) depends on Phases 1-5 or 6
- Phase 8 (Documentation) can run in parallel with implementation

**MVP Scope**: Phases 1-5 (Shell script approach) for initial automation  
**Extended Scope**: Phase 6 (Kotlin implementation) for better maintainability and type safety

