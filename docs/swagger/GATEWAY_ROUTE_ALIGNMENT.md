# Gateway route alignment (mismatch triage)

## Gateway → service path rule

[`platform/api-gateway/src/main/resources/application.yml`](../../platform/api-gateway/src/main/resources/application.yml) routes use:

```yaml
filters:
  - StripPrefix=1
```

So the first segment **`api`** is stripped.

| Client (gateway) path | Forwarded to service |
|----------------------|----------------------|
| `/api/v1/students` | `/v1/students` |
| `/api/v1/academic/classes/homeroom` | `/v1/academic/classes/homeroom` |

Controllers must use **`/v1/...`** (after strip), **not** `/api/v1/...`, or the gateway will return **404**.

## Fixes applied

1. **Resource services** (academic, attendance, assessment, search, notification, audit): `@RequestMapping` prefixes were updated from `/api/v1/...` to `/v1/...` so they match `StripPrefix=1`.
2. **Academic class management vs assessment “class reports”**: both used `/v1/classes` internally, but the gateway sends **all** `/api/v1/classes/**` to **academic-assessment-service** only. Academic **class CRUD and class leaders** were moved under **`/v1/academic/classes`** so they match the existing gateway predicate `Path=/api/v1/academic/**` → **academic-service**.
3. **user-service**: `ParentController` lives at `/v1/parents` but was missing from the gateway; **`/api/v1/parents/**`** was added to the user-service route predicate.

## Route map (high level)

| Gateway predicate (prefix) | Target service |
|----------------------------|----------------|
| `/api/v1/users/**`, `/api/v1/auth/**`, `/api/v1/permissions/**`, `/api/v1/parents/**` | user-service |
| `/api/v1/academic/**`, `/api/v1/students/**`, `/api/v1/academic-records/**` | academic-service |
| `/api/v1/attendance/**`, `/api/v1/reports/**` | attendance-service |
| `/api/v1/assessments/**`, `/api/v1/grades/**`, `/api/v1/gradebooks/**`, `/api/v1/classes/**` | academic-assessment-service |
| `/api/v1/search/**` | search-service |
| `/api/v1/audit/**` | audit-service |
| `/api/v1/notifications/**` | notification-service |

## Client-facing URL changes (breaking)

If anything called the academic **class** or **class leaders** APIs as `/api/v1/classes/...` on the academic service, it was incorrectly routed to assessment before. The correct academic URLs are now under **`/api/v1/academic/classes/...`**.

Assessment endpoints under **`/api/v1/classes/{classId}/exam-results`** etc. are unchanged.
