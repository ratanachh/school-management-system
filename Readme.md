`identity-service` (if you want internal user data), but generally defer auth to Keycloak

`directory-service` (people & profiles: Student/Parent/Teacher/Staff, parent_linking)

`academic-service` (grades 1–12, sections/classes, subjects, curriculum, homeroom)

`timetable-service` (periods, sessions, teacher allocation, conflict checks)

`attendance-service` (daily/session-based, history log, approvals)

`assessment-service` (Homework/Quiz/Assignment/Midterm/Final, submissions, scoring, approvals)

`gradebook-service` (aggregation, term/final grades, reports)

`notification-service` (email/SMS/push via RabbitMQ events)

`payment-service` (optional; fees/invoices, Bakong/Visa/MC integrations)

`reporting-service` (analytics, exports PDF/Excel; read models)

`cms-service` (optional if you don’t use Strapi; school news/events/docs)

`file-service` (MinIO proxy, signed URLs)

`search-service` (syncs denormalized read models to Meilisearch)

```| Each service: its own repo & database schema. Communications: REST + Feign for sync, events for async.```

Platform services
1) Config Server `config-server/application.yml`
```properties
server:
port: 8888
spring:
cloud:
config:
server:
git:
uri: https://your.git/config-repo.git
search-paths: config
```

2) Eureka Server
discovery-server/application.yml
```properties
server.port: 8761
eureka:
client:
register-with-eureka: false
fetch-registry: false
```

3) API Gateway

gateway/application.yml
```properties
server.port: 8080
spring:
    cloud:
        gateway:
            default-filters:
                - TokenRelay
            routes:
                - id: attendance
                uri: http://attendance-service:8081
                predicates: [ Path=/attendance/** ]
                - id: academic
                uri: http://academic-service:8082
                predicates: [ Path=/academic/** ]
    security:
        oauth2:
            resourceserver:
                jwt:
                    issuer-uri: http://keycloak:8080/realms/school
```
Keycloak integration (per service)

attendance-service/application.yml
```properties
server.port: 8081
spring:
    application.name: attendance-service
    datasource:
    url: jdbc:postgresql://postgres:5432/attendance
    username: attendance
    password: attendance
    jpa:
    hibernate.ddl-auto: validate
    flyway:
    locations: classpath:db/migration

eureka.client.service-url.defaultZone: http://discovery-server:8761/eureka/

spring.security.oauth2.resourceserver.jwt.issuer-uri: http://keycloak:8080/realms/school
```
