# System improvement delivery — 2026-09-26

## Preserved environment

- Java 21, Spring Boot 3.5.16, Spring MVC, Spring Security and Spring Data JPA/Hibernate.
- Maven build, monolithic executable JAR and embedded Tomcat runtime.
- H2 demo database and MySQL production profile.
- Existing static HTML/CSS/JavaScript and Thymeleaf dependency.
- Existing demo seed, usernames, password `Demo@12345`, and login autofill controls.
- No framework, language, database, architecture or major dependency upgrade was introduced.

## Delivered improvements

- Added `TopicRegistration` and immutable `RegistrationStatusHistory` records, including `CANCELLED` and student/dean cancellation without deleting history.
- Added transactional `AdvisorQuota` enforcement for registration approval and advisor reassignment.
- Added advisor and council report metadata/list/download authorization. Every download is audited.
- Added report size, SHA-256 checksum, sanitized filename and PDF/DOCX content validation. List endpoints use DTO projections and do not load the BLOB.
- Added audit logging for topic approval/rejection, advisor assignment, report upload/download and result publication.
- Added a 30-minute session timeout and one-current-session policy while preserving BCrypt and CSRF.
- Added typed response DTOs in place of the main `Map<String,Object>` response structures.
- Merged `UserServiceV2` logic into `UserService`.
- Split report and result responsibilities into `ReportService` and `StudentResultService`; split council mutations into `CouncilManagementService`, `DefenseService` and `GradeService`, retaining `CouncilService` as a compatibility facade.
- Added page endpoints for topics, users/students, groups, reports, councils and announcements/notifications. Student catalog now uses live pagination.
- Added entity graphs, batch grade/report queries, aggregate group counts and database count queries for the audited N+1/find-all hotspots.
- Group response member count is derived from membership, and workflows verify that the leader belongs to `group_members`.
- Topic descriptions are non-null and request validation requires non-blank content.
- Added student registration history/cancellation UI, advisor group report UI and council defense report download UI.

## Database migrations

Apply in order to an existing MySQL schema after taking a backup:

1. `database/migration/V2__add_topic_registration.sql`
2. `database/migration/V3__add_registration_history.sql`
3. `database/migration/V4__add_quota_audit_report_metadata.sql`

The demo profile keeps `ddl-auto=update`; the MySQL profile keeps `ddl-auto=validate`. Flyway was not added because that would change the dependency/runtime behavior of the current project.

## New API surface

- `GET /api/lecturer/groups/{groupId}/reports`
- `GET /api/lecturer/groups/{groupId}/reports/page`
- `GET /api/lecturer/reports/{id}`
- `GET /api/lecturer/reports/{id}/download`
- `GET /api/council/defenses/{defenseId}/reports`
- `GET /api/council/defenses/{defenseId}/reports/page`
- `GET /api/council/reports/{id}`
- `GET /api/council/reports/{id}/download`
- `POST /api/student/registrations/cancel`
- `POST /api/admin/topic-registrations/{id}/cancel`
- `GET|POST /api/admin/advisor-quotas`
- `GET /api/lecturer/topics/page`
- `GET /api/student/topics/page`
- `GET /api/lecturer/student-groups/page`
- `GET /api/admin/users/page`
- `GET /api/admin/announcements/page`
- `GET /api/councils/page`

All page endpoints accept zero-based `page` and bounded `size` parameters.

## Changed-file inventory

- Configuration/security: `application.properties`, `SecurityConfig`, `ApiAuthController`, `CurrentUser`.
- Domain model: `Topic`, `Report`, `TopicRegistration`, `RegistrationStatusHistory`, `AdvisorQuota`, `AuditLog`, `RegistrationStatus`.
- Repositories: topic, group/member, user, announcement, registration/history/quota/audit, report, council/defense/grade and registration-period repositories.
- Services: `TopicRegistrationService`, `AdvisorQuotaService`, `AuditLogService`, `ReportService`, `StudentResultService`, `StudentService`, `StudentGroupService`, `TopicService`, `DepartmentTopicService`, `UserService`, `CouncilService`, `CouncilManagementService`, `DefenseService`, `GradeService`.
- Controllers: report access, registration cancellation, quota, student, topic, group, user, announcement, dashboard and council controllers.
- DTOs: page, report, registration/history, quota, student state/result, council state/summary, dashboard and message response DTOs; topic request validation.
- Frontend: `static/js/app.js`, `static/js/councils.js`, `static/assets/js/lecturer-crud.js`, `static/lecturer/topics/student-groups.html`.
- Database/docs: `database/schema.sql`, migrations V2–V4, audit report and this delivery report.
- Tests: `SystemImprovementTest`, plus compatibility updates in `StudentWorkflowTest`, `CouncilWorkflowTest` and the merged user-service test.
- Removed: the duplicate production class `UserServiceV2`; all behavior is now in `UserService`.

## Verification

- `mvn -B clean test`: **BUILD SUCCESS**, 43 tests, 0 failures, 0 errors.
- `mvn -B -DskipTests package`: executable JAR created successfully.
- JavaScript syntax checks passed for the student, lecturer and council bundles.
- Packaged demo JAR was started on port 18080; `/` and `/api/auth/csrf` both returned HTTP 200, followed by a graceful shutdown.

## Run

Demo mode:

```powershell
mvn spring-boot:run
```

or:

```powershell
java -jar target/topic-management-1.0.0.jar
```

MySQL mode (after applying migrations and setting `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`):

```powershell
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```
