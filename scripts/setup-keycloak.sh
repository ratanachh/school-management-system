#!/bin/bash
# Keycloak Setup Wrapper (delegates to Kotlin initializer)
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

cat <<'BANNER'
==========================================
Keycloak Setup - Kotlin Initializer Wrapper
==========================================
BANNER

echo "The bash implementation has been replaced by the Kotlin-based initializer that runs with user-service startup." 
echo "This wrapper triggers the shared initializer module in manual mode using the 'bootstrap' profile." 
echo ""

export KEYCLOAK_INITIALIZER_ENABLED="${KEYCLOAK_INITIALIZER_ENABLED:=true}"

"$PROJECT_ROOT/mvnw" -pl shared/keycloak-initializer-core -am spring-boot:run \
  -Dspring-boot.run.profiles=bootstrap \
  -Dspring-boot.run.main-class=com.visor.school.keycloak.cli.ManualInitializerApplication \
  "$@"

echo ""
echo "Done. Review the logs above to confirm whether the blueprint was applied or skipped."
