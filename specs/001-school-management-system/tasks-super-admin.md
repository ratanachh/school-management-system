# Tasks: Super Admin Role Enhancement

**Input**: Design documents from `/specs/001-school-management-system/`  
**Purpose**: Add SUPER_ADMIN role that can manage ADMINISTRATOR users  
**Prerequisites**: User Story 1 (User Registration and Authentication) must be complete

**Organization**: Tasks are organized by implementation phase (Keycloak → Data Model → Service → API → Authorization)

## Format: `[ID] [P?] [Story?] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Enhancement to User Story 1 (US1-ENH)

## Phase 1: Keycloak Role and Permission Configuration

**Purpose**: Add SUPER_ADMIN role and MANAGE_ADMINISTRATORS permission in Keycloak

- [X] TSA001 Update Keycloak setup script to include SUPER_ADMIN role in scripts/setup-keycloak.sh
- [X] TSA002 Add SUPER_ADMIN to REALM_ROLES array with description "Super administrator with full system access including administrator management" in scripts/setup-keycloak.sh
- [X] TSA003 Create MANAGE_ADMINISTRATORS permission role with description "Permission to create, update, and manage administrator accounts" in scripts/setup-keycloak.sh
- [X] TSA004 Add MANAGE_ADMINISTRATORS to PERMISSION_ROLES array in scripts/setup-keycloak.sh
- [X] TSA005 Configure permission-role mapping: SUPER_ADMIN → MANAGE_ADMINISTRATORS + all existing ADMINISTRATOR permissions in scripts/setup-keycloak.sh
- [X] TSA006 Update permission-role mappings: SUPER_ADMIN gets all permissions that ADMINISTRATOR has, plus MANAGE_ADMINISTRATORS in scripts/setup-keycloak.sh

## Phase 2: Data Model Updates

**Purpose**: Update UserRole enum and database schema to support SUPER_ADMIN

- [X] TSA007 Add SUPER_ADMIN to UserRole enum in services/user-service/src/main/kotlin/com/visor/school/userservice/model/UserRole.kt
- [X] TSA008 Create Flyway migration to update users table role CHECK constraint to include SUPER_ADMIN in services/user-service/src/main/resources/db/migration/V6__add_super_admin_role.sql
- [X] TSA009 Update data-model.md to document SUPER_ADMIN role and its capabilities in specs/001-school-management-system/data-model.md
- [X] TSA010 Add MANAGE_ADMINISTRATORS permission to Permission entity seed data or migration in services/user-service/src/main/resources/db/migration/V7__add_manage_administrators_permission.sql

## Phase 3: Service Layer Updates

**Purpose**: Update UserService to support super admin managing administrators

- [X] TSA011 Add authorization check in UserService.createUser: SUPER_ADMIN can create ADMINISTRATOR users, ADMINISTRATOR cannot create ADMINISTRATOR users in services/user-service/src/main/kotlin/com/visor/school/userservice/service/UserService.kt
- [X] TSA012 Add authorization check in UserService.updateUser: SUPER_ADMIN can update ADMINISTRATOR users, ADMINISTRATOR cannot update ADMINISTRATOR users in services/user-service/src/main/kotlin/com/visor/school/userservice/service/UserService.kt
- [X] TSA013 Add authorization check in UserService.updateAccountStatus: SUPER_ADMIN can update ADMINISTRATOR account status, ADMINISTRATOR cannot update ADMINISTRATOR account status in services/user-service/src/main/kotlin/com/visor/school/userservice/service/UserService.kt
- [X] TSA014 Add method to check if user has permission to manage another user based on roles in services/user-service/src/main/kotlin/com/visor/school/userservice/service/UserService.kt
- [X] TSA015 Add validation: SUPER_ADMIN cannot be created or modified by ADMINISTRATOR (only existing SUPER_ADMIN can manage) in services/user-service/src/main/kotlin/com/visor/school/userservice/service/UserService.kt
- [X] TSA016 Add validation: Prevent role escalation (ADMINISTRATOR cannot change their own role to SUPER_ADMIN) in services/user-service/src/main/kotlin/com/visor/school/userservice/service/UserService.kt

## Phase 4: Controller and API Updates

**Purpose**: Update controllers to enforce super admin authorization

- [X] TSA017 Update UserController.updateUser to check SUPER_ADMIN role or MANAGE_ADMINISTRATORS permission before allowing ADMINISTRATOR user updates in services/user-service/src/main/kotlin/com/visor/school/userservice/controller/UserController.kt
- [X] TSA018 Update UserController.updateAccountStatus to check SUPER_ADMIN role or MANAGE_ADMINISTRATORS permission before allowing ADMINISTRATOR account status updates in services/user-service/src/main/kotlin/com/visor/school/userservice/controller/UserController.kt
- [X] TSA019 Update AuthController.register to check SUPER_ADMIN role or MANAGE_ADMINISTRATORS permission when creating ADMINISTRATOR users in services/user-service/src/main/kotlin/com/visor/school/userservice/controller/AuthController.kt
- [X] TSA020 Add @PreAuthorize annotation with hasRole('SUPER_ADMIN') or hasPermission(null, 'MANAGE_ADMINISTRATORS') for administrator management endpoints in services/user-service/src/main/kotlin/com/visor/school/userservice/controller/UserController.kt
- [X] TSA021 Add endpoint to list all administrators (accessible by SUPER_ADMIN only) in services/user-service/src/main/kotlin/com/visor/school/userservice/controller/UserController.kt
- [X] TSA022 Add endpoint to get administrator details by ID (accessible by SUPER_ADMIN only) in services/user-service/src/main/kotlin/com/visor/school/userservice/controller/UserController.kt

## Phase 5: Security Configuration Updates

**Purpose**: Update security configuration to recognize SUPER_ADMIN role

- [X] TSA023 Update SecurityConfig to include SUPER_ADMIN role in authorization checks in services/user-service/src/main/kotlin/com/visor/school/userservice/config/SecurityConfig.kt
- [X] TSA024 Update KeycloakJwtAuthenticationConverter to properly extract SUPER_ADMIN role from JWT token in platform/api-gateway/src/main/kotlin/com/visor/school/gateway/security/KeycloakJwtAuthenticationConverter.kt
- [X] TSA025 Add permission evaluator method to check MANAGE_ADMINISTRATORS permission in services/user-service/src/main/kotlin/com/visor/school/userservice/config/SecurityConfig.kt
- [X] TSA026 Update API Gateway security configuration to allow SUPER_ADMIN role in platform/api-gateway/src/main/kotlin/com/visor/school/gateway/security/SecurityConfig.kt

## Phase 6: Tests

**Purpose**: Add tests for super admin functionality

- [ ] TSA027 [P] [US1-ENH] Unit test for UserRole enum with SUPER_ADMIN in services/user-service/src/test/kotlin/com/visor/school/userservice/model/UserRoleTest.kt
- [ ] TSA028 [P] [US1-ENH] Unit test for UserService.createUser: SUPER_ADMIN can create ADMINISTRATOR, ADMINISTRATOR cannot create ADMINISTRATOR in services/user-service/src/test/kotlin/com/visor/school/userservice/service/UserServiceTest.kt
- [ ] TSA029 [P] [US1-ENH] Unit test for UserService.updateUser: SUPER_ADMIN can update ADMINISTRATOR, ADMINISTRATOR cannot update ADMINISTRATOR in services/user-service/src/test/kotlin/com/visor/school/userservice/service/UserServiceTest.kt
- [ ] TSA030 [P] [US1-ENH] Unit test for UserService.updateAccountStatus: SUPER_ADMIN can update ADMINISTRATOR status, ADMINISTRATOR cannot in services/user-service/src/test/kotlin/com/visor/school/userservice/service/UserServiceTest.kt
- [ ] TSA031 [P] [US1-ENH] Unit test for role escalation prevention: ADMINISTRATOR cannot change role to SUPER_ADMIN in services/user-service/src/test/kotlin/com/visor/school/userservice/service/UserServiceTest.kt
- [ ] TSA032 [P] [US1-ENH] Contract test for POST /api/v1/auth/register with ADMINISTRATOR role: requires SUPER_ADMIN or MANAGE_ADMINISTRATORS permission in services/user-service/src/test/kotlin/com/visor/school/userservice/contract/AuthControllerContractTest.kt
- [ ] TSA033 [P] [US1-ENH] Contract test for PUT /api/v1/users/:id with ADMINISTRATOR user: requires SUPER_ADMIN or MANAGE_ADMINISTRATORS permission in services/user-service/src/test/kotlin/com/visor/school/userservice/contract/UserControllerContractTest.kt
- [ ] TSA034 [P] [US1-ENH] Contract test for PATCH /api/v1/users/:id/status with ADMINISTRATOR user: requires SUPER_ADMIN or MANAGE_ADMINISTRATORS permission in services/user-service/src/test/kotlin/com/visor/school/userservice/contract/UserControllerContractTest.kt
- [ ] TSA035 [P] [US1-ENH] Integration test for super admin creating administrator user in services/user-service/src/test/kotlin/com/visor/school/userservice/integration/SuperAdminIntegrationTest.kt
- [ ] TSA036 [P] [US1-ENH] Integration test for super admin updating administrator user in services/user-service/src/test/kotlin/com/visor/school/userservice/integration/SuperAdminIntegrationTest.kt
- [ ] TSA037 [P] [US1-ENH] Integration test for administrator attempting to create another administrator (should fail) in services/user-service/src/test/kotlin/com/visor/school/userservice/integration/SuperAdminIntegrationTest.kt
- [ ] TSA038 [P] [US1-ENH] Integration test for role escalation prevention in services/user-service/src/test/kotlin/com/visor/school/userservice/integration/SuperAdminIntegrationTest.kt

## Phase 7: Documentation Updates

**Purpose**: Document super admin role and capabilities

- [ ] TSA039 Update spec.md to document SUPER_ADMIN role and its ability to manage administrators in specs/001-school-management-system/spec.md
- [ ] TSA040 Update research.md to document role hierarchy: SUPER_ADMIN > ADMINISTRATOR > TEACHER/STUDENT/PARENT in specs/001-school-management-system/research.md
- [ ] TSA041 Update data-model.md to include SUPER_ADMIN in User role enum documentation in specs/001-school-management-system/data-model.md
- [ ] TSA042 Add API documentation for super admin endpoints in specs/001-school-management-system/contracts/user-service-api.yaml
- [ ] TSA043 Update quickstart.md to include instructions for creating initial SUPER_ADMIN user in specs/001-school-management-system/quickstart.md

## Summary

**Total Tasks**: 43  
**Parallel Opportunities**: Tests (Phase 6) can run in parallel, some service updates can run in parallel  
**Dependencies**: 
- Phase 1 (Keycloak) must complete before Phase 2 (Data Model)
- Phase 2 (Data Model) must complete before Phase 3 (Service)
- Phase 3 (Service) must complete before Phase 4 (Controller)
- Phase 4 (Controller) must complete before Phase 5 (Security)
- Phase 6 (Tests) depends on Phases 1-5
- Phase 7 (Documentation) can run in parallel with implementation

**MVP Scope**: Phases 1-5 (Core functionality)  
**Extended Scope**: Phases 6-7 (Tests and Documentation)

**Role Hierarchy**:
- SUPER_ADMIN: Can manage all users including ADMINISTRATOR users
- ADMINISTRATOR: Can manage TEACHER, STUDENT, PARENT users (but not other ADMINISTRATOR or SUPER_ADMIN)
- TEACHER/STUDENT/PARENT: Limited to their own data and assigned permissions

