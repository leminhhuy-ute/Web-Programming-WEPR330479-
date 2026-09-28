# Council, reviewer, grading, and result audit

Audit date: 2026-09-28. Read-only source inspection; no production source or tests changed. Paths below are relative to `D:/HocFrontEnd/Web-Programming-WEPR330479--main/`. This is a module supplement for the complete audit; findings that overlap the student/registration audit must be merged rather than counted twice.

## Scope and verification

Inspected all 14 files in `src/main/java/topicmanagement/council/`, `service/StudentResultService.java`, council/result response DTOs, `static/councils/index.html`, all of `static/js/councils.js`, `static/css/councils.css`, supporting lecturer shell, `CouncilWorkflowTest`, council-related sections of `SystemImprovementTest`, and the relevant security, current-account, user-status, topic-advisor assignment, report, registration cancellation, period update, entity, repository and SQL paths.

The coordinating audit ran a clean Maven verify: all 43 tests pass. Existing `CouncilWorkflowTest` is one passing test method; it is not comprehensive proof of this workflow. A separate disposable instance was started from the freshly built JAR, bound to `127.0.0.1:18081`, with `spring.profiles.active=demo`, `jdbc:h2:mem:councilaudit`, and `ddl-auto=create-drop`. It started successfully on Java 26.0.2.1/Spring Boot 3.5.16. Runtime requests confirmed that both the council state endpoint and assignment endpoint work, and reproduced inactive-reviewer assignment. No persistent application database was used.

## Discovered model and actual workflow

| Concept | Actual representation | Important constraints and implications |
|---|---|---|
| Committee | `Council` → `councils` | ID, version, unique code, name, `LocalDateTime defenseDate`, room, enum status. Status initialized DRAFT, creation sets READY; no code advances to COMPLETED. No direct registration-period FK. |
| Committee membership | `CouncilMember` → `council_members` | Required council and lecturer user FKs; unique `(council_id, lecturer_id)`; one enum role per member: CHAIRPERSON, SECRETARY, REVIEWER, MEMBER. Council owns members with cascade ALL and orphan removal; no HTTP roster editing/deletion endpoint exists. Cascade applies to owned membership records, not users. |
| Evaluation assignment | `Defense` → `defenses` | Required unique group FK; many defenses can share one council; required reviewer user FK; optimistic version; `finalized`, `published`, decimal final score. It does **not** store topic or registration FK. |
| Component grade | `Grade` → `defense_grades` | One score/comment per `(defense_id, evaluator_id)`; required evaluator and defense FKs, score decimal(4,2), comment max 2000, updated timestamp. No separate rubric, supervisor component, or independent review component. |
| Result | `Defense.finalScore/finalized/published` + component `Grade`s | No standalone Result entity. Result topic comes from mutable `Defense.group.topic`. |
| Student result | `StudentResultService.current()` | Looks up current authenticated student membership, then its group's published defense. No caller-supplied student/group/topic ID. Returns score, current topic title, and each evaluator's name/score/comment. |

Actual path: dean creates 3–5-member council → dean assigns APPROVED group plus a reviewer who has REVIEWER role in that council → every council member enters one score → chair finalizes arithmetic mean → dean publishes → group students can view. Reviewer is always also a council member, and all project types use the same path. One committee can evaluate many groups/topics; a group can have only one Defense for its entire persisted lifetime.

`CouncilController` is thin and delegates to transactional services. `CouncilService` is a read facade and mutation facade over `CouncilManagementService`, `DefenseService`, and `GradeService`; there is some duplicate small identity-check logic, but no confirmed bypass from that duplication.

## Permission and endpoint matrix

Security chain: `config/SecurityConfig.java:83–86` permits council pages/APIs only for DEAN, HEAD_OF_DEPT, LECTURER, and `/api/student/**` only for STUDENT. `CurrentAccount.user():14–20` reloads the user and rejects disabled accounts. Every council mutation also checks the live actor role/status in its service. POSTs require CSRF, and `councils.js:8–25` obtains and sends the token.

| HTTP endpoint | Actual authorization and ownership | Validation/state | Result/error behavior |
|---|---|---|---|
| GET `/api/councils` | Active staff; dean gets all, other staff only councils they belong to and their defenses (`CouncilService:58–72`) | Includes all component grades and report metadata for authorized defenses, regardless publication, as needed by committee | Raw `CouncilStateResponse`, 200; no cross-council response exposure found. Fetch-all then in-memory filtering is a scale concern. |
| GET `/api/councils/page` | Active staff; SQL-level dean/member visibility (`CouncilRepository:14–18`) | Page clamped ≥0; size 1–100 | Bounded council summaries only; not used by current council UI. |
| POST `/api/councils` | Active dean (`CouncilManagementService:26–55`) | Valid DTO; lengths; 3–5 distinct users; exactly one chair/secretary; at least one reviewer; future date; staff eligibility on creation | 200, invalid rules 400, duplicate code 409, null roster item 500 (COUNCIL-002). |
| POST `/api/councils/assignments` | Active dean (`DefenseService:35–68`) | Required IDs; approved group; unique assignment; reviewer in council with REVIEWER role; no member is current topic advisor; KLTN period date matches council date | 200 or domain 400; stale member status bypass COUNCIL-001. |
| POST `/api/councils/defenses/{id}/grade` | Active staff, member of this defense's council, not either actual supervisor ID (`GradeService:34–54`) | Not finalized; selected reviewer's deadline; score [0,10] max 2 decimals; nonblank comment ≤2000; server derives evaluator identity | 200, unauthorized 403, invalid/missing defense 400. Can edit own score until finalization; cannot spoof evaluator. |
| POST `/api/councils/defenses/{id}/finalize` | Only actual CHAIRPERSON of that council (`GradeService:56–69`) | Not already finalized; grade count equals roster size | 200 or 400/403; calculates entirely on server. No request score accepted. |
| POST `/api/councils/defenses/{id}/publish` | Active dean (`GradeService:71–77`) | Must be finalized | 200; transaction locks defense and writes audit record. No unpublish endpoint. Repeated publish allowed and emits a misleading `published=false` old audit value (COUNCIL-003). |
| GET `/api/student/result` | Only live active student, derives own membership (`StudentResultService:30–40`) | Requires `published=true` before exposing any grade data | 200 unpublished DTO contains null score/topic and empty grades. No direct result-ID manipulation surface. Result association is vulnerable to group topic retargeting (student audit cross-reference). |

Chair/secretary/reviewer/supervisor are relationship roles, not account roles. Dean is not implicitly allowed to grade or finalize: they must separately belong to the assigned council, and must be chair to finalize. Head of department has no extra council mutation power. This is consistent with the specified chair responsibility; the specification does not assign council creation/publication to a named role, so dean-only choices require owner acceptance, not a claimed privilege defect.

## Requirement traceability for this module

| ID | Requirement | Implemented at | Status | Evidence | Problem |
|---|---|---|---|---|---|
| C-01 | Reviewer is a lecturer; may evaluate multiple topics | `DefenseService:43–57`; `CouncilMember`; `Defense.council` | PARTIAL | Same council/member may participate in many defenses; active staff checked at roster creation | Existing members are not revalidated after user role/status changes, COUNCIL-001. |
| C-02 | No supervisor evaluates own topic | `DefenseService:48–49`; `GradeService:37–38`; `DepartmentTopicService:98–103` | PARTIAL | Compare actual IDs against advisor1/2; topic lock and forbid reassignment once a defense exists | Normal path protected; changing group topic through cancellation/re-registration bypasses immutable evaluation association. See student audit. |
| C-03 | Committee 3–5 lecturers | `CreateInput:29`; `CouncilManagementService:28–29,42–45` | PARTIAL | Creation enforces size and active staff | User mutation can leave roster containing inactive/nonstaff user; no repair route. |
| C-04 | Duplicate committee members prohibited | `CouncilManagementService:30–31`; `CouncilMember:7–9`; SQL `schema:219` | PASS | Both service distinct IDs and DB unique pair; one person cannot be chair+secretary by duplicate entry | Missing boundary-specific regression tests. |
| C-05 | Exactly one chair and secretary; both are members and distinct | `CouncilManagementService:32–34`; `CouncilMember.role` | PASS | Role lives on the member row; exactly one of each; one enum per distinct lecturer | Not directly tested except valid case and non-chair finalization. |
| C-06 | Council changes/deletion after grading cannot invalidate scores | `CouncilController:14–22` | PASS | No HTTP council edit/delete or reassignment route exists | Availability/lifecycle policy incomplete: unable to replace departed or disabled member, reschedule, or correct assignment; assess as missing operational policy rather than unrestricted-edit exploit. |
| C-07 | One council can evaluate multiple topics | `Defense:10`; `DefenseService.assign` | REQUIREMENT AMBIGUITY | Many-to-one council, distinct group defenses | Specification does not say whether this is allowed, capacity, period mixing, or schedule conflicts. |
| C-08 | Reviewer submission deadline | `GradeService:40–42` | PARTIAL | Deadline checked in backend for selected `Defense.reviewer` | Additional roster members with REVIEWER role are not covered unless selected; definition needs owner decision. UI ignores deadline (COUNCIL-004). |
| C-09 | All required grades before aggregate | `GradeService:63–65`; grade unique pair | PARTIAL | Count must equal member count; current immutable roster + membership-only grading gives one grade/member | Counts, not identity set; status/topic retargeting invalidates assumptions. Normal path is protected; no grade-missing finalize bypass found. |
| C-10 | Arithmetic mean calculation | `GradeService:66–67` | PASS | `BigDecimal` sum divided by grade count, 2 decimals HALF_UP | No integer division; precision/rounding rule unspecified and should be documented. |
| C-11 | Define component grade | `Grade`, `GradeService`, `councils.js:298` | REQUIREMENT AMBIGUITY | Exactly one scalar score per council member; designated reviewer contributes one ordinary member score | No rubric, separate written/oral/reviewer components, or weighting model. Owner must confirm this interpretation. |
| C-12 | Score range/null/duplicates and own-score editing | `GradeService:43–53`; `Grade:6,11–12` | PASS | Reject null, negative, >10, >2 decimals, empty/long comment; upsert by server-derived evaluator; DB unique pair | No DTO comment length/score bounds, but service enforces them. |
| C-13 | Chairman aggregates; ordinary members cannot override final score | `GradeService:59–67`; `CouncilController:20` | PASS | Chairman identity checked and no final-score input parameter | Supervisor-chair bypass through retargeted topic belongs to cross-module issue. |
| C-14 | Scores cannot change after finalization/publication | `GradeService:39`; `publish:74` | PARTIAL | Finalization prevents all component edits; publication requires finalized | Stored scores are locked, but result topic can change via group mutation. No correction/reopen/unpublish workflow. |
| C-15 | Publish after evaluation completion, atomically | `GradeService:71–77`; `locked:80–83`; `@Transactional` | PARTIAL | Finalized required, lock serializes grade/finalize/publish | Report prerequisite/presentation timing are not encoded; specification must define enforced milestones. Repeated publication audit wrong. |
| C-16 | Students only see own published result | `StudentResultService:30–40`; `SecurityConfig:85–86` | PARTIAL | Identity-derived group and publication filter prevent ordinary IDOR/unpublished leakage | Current group topic supplies title, causing old result to be relabeled after re-registration. No multi-period result history. |
| C-17 | Lecturer result/report scope | `CouncilService:60–66`; `ReportService:90–120,160–164` | PASS | Dean or assigned council member only; outsiders excluded | Unpublished committee grades visible to authorized peers; anonymity/independent marking policy unspecified. |
| C-18 | Frontend corresponds to backend | `councils.js`; `CouncilStateResponse` | PARTIAL | UI receives canGrade/canFinalize; hides dean-only actions; escaped text; CSRF present | Deadline mismatch, arbitrary name substitutions, unbounded state loading. |
| C-19 | Negative input and coherent errors | `CreateInput`; `GlobalExceptionHandler` | PARTIAL | Most malformed enums, dates, IDs, bounds become 400/403/409 | Null roster items become 500. Missing defense ID uses 400 instead of 404; not a security bypass. |
| C-20 | Assignment/grading concurrency and FK integrity | `DefenseService:37–42`; `GradeService:80–83`; DB unique keys | PARTIAL | Defense writes serialized; group duplicate assignment locked and unique; advisor reassignment locks same topic | User role/status updates and cancellation/topic retargeting do not share all relevant aggregate locks. No concurrent regression tests. |

PASS denotes source-traced behavior for the stated normal model, not exhaustive runtime validation or absence of other module defects.

## Confirmed defects

### COUNCIL-001 — New evaluation assignment accepts an inactive or nonlecturer roster member

Severity: HIGH. Category: Business Logic / Validation / Data Integrity. **CONFIRMED (runtime for inactive reviewer, source for changed role).**

Affected files: `src/main/java/topicmanagement/council/DefenseService.java:35–57`; `src/main/java/topicmanagement/council/CouncilManagementService.java:42–45`; `src/main/java/topicmanagement/service/UserService.java:59–71,99–100`; `src/main/java/topicmanagement/council/GradeService.java:35,63–65`; `src/main/java/topicmanagement/council/CouncilController.java:14–22`.

Affected symbols: `DefenseService.assign`, `CouncilManagementService.create`, `UserService.update/setStatus`, `GradeService.finalizeScore`.

Evidence: Council creation checks `TopicPolicy.staff(lecturer)`. Later user updates can change role/status without checking council membership. Assignment only checks that reviewer ID appears in a REVIEWER membership and nobody supervises the selected topic. It never verifies live roles/statuses of the reviewer or other members. The group is permanently linked to that Defense by a unique group FK. Grading rejects inactive/students, but finalization still requires every roster member's score, and there is no member replacement/defense reassignment endpoint.

Expected: Every new committee/reviewer assignment must reference eligible active lecturers; changes that make assigned members ineligible need a deliberate replacement/cancellation workflow preserving completed evaluations.

Actual: A valid dean can assign new work to an inactive reviewer (or a roster containing a now-STUDENT user). An unsubmitted disabled member's required grade prevents completion. If already submitted, their score remains counted without any policy check.

Reproduction, executed on disposable H2 instance:

1. Start the demo profile on a separate in-memory database and log in as dean with CSRF.
2. GET `/api/councils`: observe valid seeded council `id=1` containing REVIEWER `lecturer05`, `id=7`, and approved group `id=1`.
3. PATCH `/api/admin/users/7/status` with `{"status":"INACTIVE"}` → HTTP 200.
4. POST `/api/councils/assignments` with `{"groupId":1,"councilId":1,"reviewerId":7}` → **HTTP 200**, `{"message":"Đã lưu thay đổi."}`.
5. GET `/api/councils` returns persisted defense `id=1` with this reviewer, `finalized=false`, `published=false`, no grades.

Impact: Invalid committee membership, assignment to a blocked account, and evaluation workflow that cannot finish without reactivating the account or direct database repair.

Smallest correct fix: In the assignment transaction, revalidate all council members' live role/status and reviewer eligibility. Add an explicit policy for role/status changes involving unfinished evaluations, preserving ability to disable compromised accounts while requiring administrative reassignment rather than preventing security disablement. Coordinate account and assignment locks/version checks if simultaneous changes must be prevented. Add a safe roster/reviewer replacement workflow, with existing grade invalidation rules decided explicitly.

### COUNCIL-002 — Null council member passes request validation and becomes HTTP 500

Severity: MEDIUM. Category: Validation / Error Handling. **CONFIRMED by complete source trace; coordinator also running HTTP probe.**

Affected files: `src/main/java/topicmanagement/council/CouncilService.java:26–29`; `src/main/java/topicmanagement/council/CouncilManagementService.java:28–34`; `src/main/java/topicmanagement/council/CouncilController.java:17`; `src/main/java/topicmanagement/exception/GlobalExceptionHandler.java:81–85`.

Affected symbols: `CreateInput.members`, `CouncilManagementService.create`.

Evidence: The list is `@NotNull @Size(min=3,max=5) List<@Valid MemberInput>`; its elements have no `@NotNull`. Cascaded validation skips null elements. The service dereferences elements with `MemberInput::userId`, producing NPE before a domain exception. Catch-all error handler returns 500.

Expected: Invalid roster input should be rejected with HTTP 400 and useful field validation; no attempted persistence.

Actual: Valid JSON with a null member causes an internal server error.

Reproduction:

1. Authenticate as dean, retrieve CSRF token.
2. POST `/api/councils` with valid code/name/room/future defenseDate and `"members":[null,null,null]`.
3. Observe HTTP 500 instead of validation 400.

Impact: A malformed but parseable request becomes an application failure; faulty clients can fill error logs. This is not claimed as an unauthenticated denial of service or persistence exploit.

Smallest correct fix: Use `List<@NotNull @Valid MemberInput>` and guard null members at the service/domain boundary; add one HTTP regression test.

### COUNCIL-003 — Repeated result publication writes false old state to audit log

Severity: LOW. Category: Data Integrity / Audit. **CONFIRMED by source.**

Affected files: `src/main/java/topicmanagement/council/GradeService.java:71–77`; `src/main/resources/static/js/councils.js:158`.

Affected symbol: `GradeService.publish`.

Evidence: The UI hides publication after publication, but direct POST remains allowed. The service checks only `finalized`, sets published true again, and always logs old value `published=false`.

Expected: Republish should be an idempotent no-op or a clearly recorded repeat action; audit history must reflect actual prior state.

Actual: Repeated successful requests produce additional apparent false→true transitions even when the result was already public.

Reproduction: Publish a finalized defense, then repeat POST `/api/councils/defenses/{id}/publish`; both succeed and audit old state is false for both.

Impact: Inaccurate publication history and avoidable duplicate audit events; grades themselves do not change.

Smallest correct fix: Check already-published under the existing lock and return no-op or conflict; if repeat publication has meaning, log the actual old state.

### COUNCIL-004 — Reviewer grading action stays enabled after submission deadline

Severity: LOW. Category: UI. **CONFIRMED by source.**

Affected files: `src/main/java/topicmanagement/council/CouncilService.java:51–56`; `src/main/java/topicmanagement/council/GradeService.java:40–42`; `src/main/java/topicmanagement/dto/response/CouncilStateResponse.java:17–19`; `src/main/resources/static/js/councils.js:155–157,277–290`.

Affected symbols: `CouncilService.defenseView`, `GradeService.grade`, grade dialog.

Evidence: `canGrade` checks only not-finalized, membership and supervisor conflict; it does not apply reviewer deadline. Deadline is not present in the response to let the UI explain it. Backend correctly rejects the selected reviewer after deadline.

Expected: UI eligibility and explanatory state match server eligibility; expired reviewer should see deadline information instead of an enabled edit workflow that cannot succeed.

Actual: Reviewer can open and complete the form after cutoff, only learning at submission that grading is forbidden.

Reproduction: In a TLCN/KLTN defense whose reviewDeadline passed and is not finalized, load council view as designated reviewer; `canGrade=true`; submit a valid score and receive 400 deadline error.

Impact: Misleading action/state and wasted input; no deadline bypass for the designated reviewer.

Smallest correct fix: Centralize grading eligibility (and reason) so view and mutation use the same deadline rule, return deadline information, preserve server enforcement.

### COUNCIL-005 — Display code substitutes invented personal names for real evaluator names

Severity: LOW. Category: UI / Data Accuracy. **CONFIRMED by source.**

Affected files: `src/main/resources/static/js/councils.js:58–63,92,125,147`; related `src/main/resources/static/assets/js/lecturer-shell.js:2–15`; student result view also calls its own `cleanName` (`static/js/app.js:269`).

Affected symbol: `cleanCouncilName`.

Evidence: This function strips `demo`, `test`, `sample` and maps a stored full name equal to `admin`, certain damaged Vietnamese text, or a stripped empty name to `Nguyễn Văn Minh`. Names are otherwise valid request data. Council member identities, selected reviewer and evaluator score attribution all use this function.

Expected: Display exact stored full name (escaped) or an honest missing-value placeholder; do not invent a different identity.

Actual: Stored user fullName `Admin` displays as an unrelated Vietnamese name in the council and grade list.

Reproduction: Create eligible lecturer with fullName `Admin`, assign to valid council; inspect member and evaluation names.

Impact: Misidentified grader/chair in a system where evaluation attribution matters. Authorization still uses IDs and is unaffected.

Smallest correct fix: Remove demo-name substitution from production rendering; correct seed data at its source if needed and display validated full names with escaping.

## Cross-module confirmed defect to merge with student audit

**Retargeted group rewrites result identity and bypasses evaluation invariants.** `TopicRegistrationService.cancelByStudent/cancelByDean:56–74,126–130` permits approved cancellation despite existing Defense/grades/results. `syncLegacy:145–155` and later registration mutate `group.topic`. `Defense:9` and report records retain only group identity. `StudentResultService:35–40` combines retained old defense scores with the current topic title. `GradeService.finalizeScore:59–68` does not recheck topic supervisor conflicts or approved registration; `publish:74–77` only checks finalized. A group changed to a topic supervised by the existing chair can therefore retain that chair's already-entered score and be finalized by that chair, despite the otherwise-correct conflict checks on new grade submissions. `DefenseService.assign:42` then prevents a fresh defense because the group already has one.

The student audit owns full runtime reproduction, issue ID/severity and fix proposal. Correction requires preserving immutable registration/topic evaluation identity and defining post-evaluation cancellation/transfer policy, not merely hiding a cancellation button. Locking only Defense cannot prevent a parallel group/topic change.

## Requirement ambiguities and operational gaps

1. **Meaning of a component grade.** Current formula is equal mean over all council members, including the selected reviewer exactly once. Possible interpretations include one lecturer vote, separate written/oral/report grades, or reviewer plus committee components. Decide component schema and rounding precision; do not add weights contrary to the arithmetic-mean rule.
2. **When councils are applicable.** All COURSE/NCKH/TLCN/KLTN evaluations require a council with a REVIEWER role; there is no independent reviewer-assignment/grading path. The specification says committee “where applicable” and only KLTN needs a presentation date. Decide which types need council versus standalone reviewer evaluation. Current mandatory reviewer role is stricter than the stated committee composition rules.
3. **Several reviewers in one roster.** Create permits multiple REVIEWER members, but Defense selects exactly one reviewer and only that person's grade is deadline-limited (`GradeService:41`). Decide whether REVIEWER is a pool role whose member becomes a per-defense reviewer only when selected, or every REVIEWER-role member is subject to deadline. Do not classify the second interpretation as a confirmed bypass before that decision.
4. **Report and presentation prerequisites.** Grade/finalize/publish do not require any submitted report, completed presentation, or reaching council defenseDate. Existing passing test performs full evaluation with zero reports and future defense date (`CouncilWorkflowTest:67–85`). Runtime assigned defense similarly had zero reports and future date. This behavior is confirmed; whether all such sequencing must be blocked is not expressly defined. Specify mandatory report stage and distinction between pre-defense reviewer submission and post-presentation committee votes before implementing timing gates.
5. **Committee scope/capacity.** Many defenses may share a council; no period FK, topic capacity, room collision, or lecturer schedule collision checks. Determine whether mixed periods/types and overlapping meetings are allowed. Do not invent one-topic-per-council requirement.
6. **Correction/reassignment/reopening.** No council edit/delete, assignment replacement, score reopening, unpublish or result correction endpoint exists. Current immutability prevents unauthorized updates but leaves operational corrections undefined. Decide allowed actors, milestones, grade invalidation and audit behavior.
7. **Council status lifecycle.** DRAFT and COMPLETED exist, but normal creation immediately READY and no completion logic. `/api/councils/page` exposes stored status. Define when a council is “complete” if further groups may be assigned; do not present unused enum as evidence completion is implemented.
8. **Peer evaluation visibility.** All council members can see each other's unfinalized scores/comments. Decide whether independent/blind grading is required; the supplied specification does not forbid peer visibility.
9. **Historical results.** Result API selects first global membership and returns one defense. Global unique student membership currently makes selection unambiguous, but there is no period/history endpoint. If student participation is corrected to one group per process/period, result lookup must change with it.

## Database and transaction assessment

Reliable controls: council member unique pair, defense unique group, grade unique evaluator/defense, required FKs, decimal scores; SQL includes score range checks (`schema.sql:225–251`). No cascade to users. Council member cascade deletion and grade cascade deletion are appropriate for owned rows, but runtime JPA schema and hand-maintained SQL differ in coverage; parent audit owns deployment drift assessment.

Service/domain controls are appropriate for 3–5 roster size, exactly one chairman/secretary, active lecturer eligibility and supervisor exclusion, because these are cross-row/relationship invariants. DB unique `(council,lecturer)` and `(defense,evaluator)` are appropriate. A composite DB FK or relationship model can strengthen “evaluator is council member,” but should not be bolted on without deciding committee replacement/history behavior. Optional SQL checks `published => finalized`, `finalized => final_score not null`, and valid score range are safe local row invariants; their absence is defense-in-depth, not independently demonstrated API corruption.

`DefenseService.assign` locks StudentGroup then Topic and refreshes Topic before conflict check. Same group assignment cannot race into two rows (DB unique is backstop). `DepartmentTopicService.assignAdvisors` locks Topic and disallows change after any linked defense, sharing protection against ordinary concurrent advisor assignment. Every grade edit/finalize/publish locks the same Defense row in a transaction, so duplicate first grade, lost parallel component edit versus finalization, and ordinary publication/grade races are serialized. Different lecturers' grades are intentionally serialized per defense. Parent grade unique constraint protects against accidental direct inserts.

Remaining concerns: user status/role changes are neither coordinated with council assignment nor evaluated at assignment; group cancellation/topic changes are outside the evaluation aggregate and create an actual integrity bug; current result lookup relies on mutable group topic. No concurrent integration tests verify these locks under the configured MySQL deployment. H2 passing tests alone do not prove MySQL concurrency correctness.

Performance observation, **POTENTIAL / NEEDS VOLUME VERIFICATION**, not a confirmed outage: `CouncilService.state:60–71` loads all councils and all defenses before permission filtering, then loads all visible grade/report metadata; `councils.js:66` uses this unbounded endpoint despite the new council summary page endpoint. `DefenseRepository` uses an entity graph, and grade/report queries are batched, which helps but does not bound total rows. Runtime state endpoint passed on demo data. Apply query-level visibility, separate detail requests and pagination if realistic dataset sizes justify it; measure before assigning severity.

## Test assessment and targeted regressions

Existing passing `CouncilWorkflowTest.gradesNeedEligibleMembersChairFinalizationAndDeanPublication` checks: non-dean council create denial; 2-member council rejection; one valid 3-person council; assignment reviewer not in roster rejected; advisor grading denied (advisor is also outsider, so this does not isolate advisor-vs-member conflict); finalize without all grades rejected; >10 rejected; non-chair finalization denied; scores 8/9/7 mean 8.00; post-finalize edit rejected; result hidden pre-publication; published own result visible and unrelated student sees unpublished.

Existing passing `SystemImprovementTest.councilMemberCanViewOnlyAssignedDefenseReport` checks council report metadata ownership versus an unrelated lecturer. It constructs a one-member invalid council directly in the repository, so it does not validate valid committee creation or full production workflow. Pagination test merely expects `/api/councils/page` 200 for a lecturer; it does not assert council visibility content. All 43 existing tests passed in the clean coordinating build; no existing failing council test identified.

Missing tests:

- 0/1/2/6-member councils, duplicate lecturer, missing/two chairmen, missing/two secretaries, same lecturer in chair/secretary entries, null member element, null enum/IDs, past date, invalid text lengths, duplicate code HTTP behavior.
- Existing inactive/STUDENT member at assignment; status/role change during assignment; deactivation after assignment before required grade; safe authorized replacement once implemented.
- Explicit committee member who is advisor1/2 rejected for assignment and grading; advisor reassignment race; topic retargeting after grades/finalization/publication.
- KLTN defense-day equality and period date edits after assignment; selected reviewer exactly-before/at/after deadline; additional REVIEWER-role semantics after clarified; UI canGrade at deadline.
- Scores 0 and 10 accepted; negative/null/>10/>2 decimals rejected; whitespace/long/null comments rejected; same evaluator edit leaves exactly one row; nonexistent defense consistent response.
- 3/4/5-member means with fractional totals and rounding boundary (e.g. 0,0,0.01 → 0.00 at two decimals, half-up case); missing any individual grade rejected; exact evaluator identity set once roster mutation is supported.
- Unrelated lecturer's grade/finalize POST denied; dean outside roster cannot grade/finalize; unrelated chair cannot finalize; student HTTP mutation blocked with valid CSRF; mutation with no CSRF denied.
- Post-publish score edit rejected; repeated publish idempotent or truthful audit; direct result endpoint cannot reveal another group's result; result still points to original registration after later participation.
- Two simultaneous first grades by same lecturer, grade update vs finalize, last required grade vs finalize, publish vs edit, two same-group assignments, cancellation/retarget vs grade/finalize, and status change vs assignment under MySQL.
- Browser/runtime checks for truthful evaluator names, expired reviewer action, form errors, and paginated load behavior after fixes.

## Recommended module repair order and files

| Priority | File | Reason | Related issues |
|---|---|---|---|
| 1 | `entity/TopicRegistration.java`, `council/Defense.java`, `service/TopicRegistrationService.java`, `service/StudentResultService.java`, `council/GradeService.java`, relevant SQL | Preserve evaluation's immutable registration/topic identity; control cancellation/transfer after downstream work | Cross-module student issue |
| 2 | `council/DefenseService.java`, `service/UserService.java`, `council/CouncilManagementService.java` | Revalidate live member eligibility; define safe user disablement/reassignment | COUNCIL-001 |
| 3 | `council/CouncilService.java`, `council/CouncilManagementService.java` | Reject null list elements cleanly | COUNCIL-002 |
| 4 | `council/GradeService.java` | Correct repeat-publication audit/idempotence | COUNCIL-003 |
| 5 | `council/CouncilService.java`, `dto/response/CouncilStateResponse.java`, `static/js/councils.js` | Shared grading eligibility and deadline feedback | COUNCIL-004 |
| 6 | `static/js/councils.js`, shell/student name rendering | Show truthful account/evaluator identity | COUNCIL-005 |
| Alongside each fix | `src/test/java/topicmanagement/CouncilWorkflowTest.java` and focused HTTP/MySQL integration tests | Capture negative inputs, conflict rules, identity preservation and concurrency | All |

Confirm evaluator components, type-specific committee applicability, milestone timing and corrective workflows with the project owner before extending domain behavior. No source repair has been performed by this audit.
