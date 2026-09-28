# REST API Documentation: HCM-UTE Student Topic Management System

## 1. Overview & Conventions

The system exposes a secure RESTful API under the `/api/**` prefix. All endpoints communicate using standard JSON formats and standard HTTP status codes.

### 1.1 Base URL & Security Headers
- **Base URL**: `http://localhost:8080/api` (or configured host/port)
- **Content-Type**: `application/json` (or `multipart/form-data` for file uploads)
- **CSRF Token**: Non-GET state-modifying requests require a CSRF token passed either as an HTTP header (`X-XSRF-TOKEN`) or form parameter.
- **Session Cookie**: `JSESSIONID` (HttpOnly, SameSite=Lax).

### 1.2 Unified Response Format
```json
{
  "success": true,
  "message": "Operation description / user message",
  "data": { ... }
}
```

### 1.3 Standard Error Codes
| HTTP Status | Meaning | Typical Trigger |
| :--- | :--- | :--- |
| `200 OK` | Request succeeded | Successful GET, PUT, POST |
| `201 Created` | Resource created | Successful entity creation |
| `400 Bad Request` | Validation failure | Missing fields, invalid date sequences, malformed payloads |
| `401 Unauthorized` | Unauthenticated | Missing or expired session cookie |
| `403 Forbidden` | Access denied | Insufficient role permissions or department mismatch |
| `404 Not Found` | Resource not found | Invalid entity ID or URI |
| `409 Conflict` | Domain rule violation | Duplicate username/email, deleting period with topics, cancelling active registration |
| `500 Internal Error` | Server error | Unexpected exception |

---

## 2. Authentication & Session APIs (`/api/auth`)

### 2.1 Login
- **Endpoint**: `POST /api/auth/login`
- **Access**: Public
- **Request Body**:
  ```json
  {
    "username": "dean01",
    "password": "Demo@12345"
  }
  ```
- **Response** (`200 OK`):
  ```json
  {
    "success": true,
    "message": "Đăng nhập thành công.",
    "data": {
      "id": 1,
      "userCode": "GV0001",
      "username": "dean01",
      "fullName": "PGS. TS. Nguyễn Văn Thành",
      "email": "dean01@hcmute.edu.vn",
      "role": "DEAN",
      "departmentId": 1
    }
  }
  ```

### 2.2 Logout
- **Endpoint**: `POST /api/auth/logout`
- **Access**: Authenticated users
- **Response** (`200 OK`):
  ```json
  {
    "success": true,
    "message": "Đã đăng xuất.",
    "data": null
  }
  ```

### 2.3 Current User Info
- **Endpoint**: `GET /api/auth/me`
- **Access**: Authenticated users
- **Response** (`200 OK`): Returns `CurrentUser` details.

### 2.4 CSRF Token Initialization
- **Endpoint**: `GET /api/auth/csrf`
- **Access**: Public / Authenticated

---

## 3. User Administration APIs (`/api/users`)

*Authorization: `ROLE_DEAN` only.*

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/users` | List users with optional `keyword`, `role`, `departmentId`, `status`. |
| `GET` | `/api/users/{id}` | Retrieve specific user account details. |
| `POST` | `/api/users` | Create user account. Validates case-insensitive uniqueness and role-department constraints. |
| `PUT` | `/api/users/{id}` | Update profile details (full name, email, department). |
| `PUT` | `/api/users/{id}/status` | Activate, deactivate, or lock user account. |
| `PUT` | `/api/users/{id}/password` | Reset user password (8–72 characters). Updates `password_changed_at`. |

---

## 4. Registration Period APIs (`/api/admin/registration-periods`)

*Authorization: `ROLE_DEAN` only.*

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/admin/registration-periods` | List periods with optional `keyword` and `type` filter. |
| `GET` | `/api/admin/registration-periods/{id}` | Get registration period by ID. |
| `POST` | `/api/admin/registration-periods` | Create new period. Validates 4-point temporal window. |
| `PUT` | `/api/admin/registration-periods/{id}` | Update period details. Rejects type mutation if topics exist. |
| `DELETE` | `/api/admin/registration-periods/{id}` | Delete period. Rejects with `409 Conflict` if topics or registrations exist. |

---

## 5. Topic Management APIs (`/api/topics` & `/api/department/topics`)

| Method | Endpoint | Authorization | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/topics` | Authenticated | List topics filtered by period, department, type, status. |
| `GET` | `/api/topics/{id}` | Authenticated | Get detailed topic proposal. |
| `POST` | `/api/topics` | `LECTURER`, `DEAN` | Propose new topic. Dean can propose across departments. |
| `PUT` | `/api/topics/{id}` | `LECTURER`, `DEAN` | Update topic within lecturer proposal window. |
| `DELETE` | `/api/topics/{id}` | `LECTURER`, `DEAN` | Delete topic proposal before approval. |
| `POST` | `/api/department/topics/{id}/approve` | `HEAD_OF_DEPT` | Approve topic, assigning primary & secondary advisors. |
| `POST` | `/api/department/topics/{id}/reject` | `HEAD_OF_DEPT` | Reject topic with reason note. |

---

## 6. Student Group & Registration APIs (`/api/student`)

*Authorization: `ROLE_STUDENT` only.*

### 6.1 Group Formation & Management
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/student/me` | Fetch active state (student profile, current group, current registration, invitations). |
| `POST` | `/api/student/groups` | Create student group. Caller becomes Leader (`LEADER`). Body: `{"name": "Team Alpha"}` |
| `POST` | `/api/student/groups/invitations` | Invite peer by student code. Body: `{"studentId": "22110002"}` |
| `POST` | `/api/student/invitations/{id}/response` | Accept or decline invitation. Body: `{"accept": true}` |
| `POST` | `/api/student/groups/leader` | Transfer leadership to another member. Body: `{"studentId": "22110002"}` |
| `POST` | `/api/student/groups/leave` | Member leaves group (allowed only if no topic registered). |
| `POST` | `/api/student/groups/members/remove` | Leader removes member (allowed only if no topic registered). Body: `{"studentId": "22110002"}` |
| `POST` | `/api/student/groups/disband` | Leader disbands group (allowed only if no topic registered). |

### 6.2 Topic Registration & Deliverables
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/student/topics` | Browse published topics eligible for student registration. |
| `GET` | `/api/student/topics/page` | Paginated catalog search with query, department, type filters. |
| `POST` | `/api/student/registrations` | Leader registers topic. Body: `{"topicId": "1"}` |
| `POST` | `/api/student/registrations/cancel` | Cancel registration. Blocked (`409`) if reports or defenses exist. Body: `{"note": "Reason"}` |
| `POST` | `/api/student/reports` | Multipart upload for progress report (`stage`, `note`, `file`). |
| `GET` | `/api/student/reports/{id}/download` | Download report file attachment. |
| `GET` | `/api/student/result?periodId={id}` | View defense evaluation score and feedback. Supports multi-period resolution. |

---

## 7. Council Management & Defense APIs (`/api/councils`)

*Authorization: `ROLE_DEAN`, `ROLE_HEAD_OF_DEPT`, `ROLE_LECTURER`.*

| Method | Endpoint | Authorization | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/councils` | Faculty | List councils, assigned defenses, and eligible groups. |
| `GET` | `/api/councils/page` | Faculty | Paginated list of councils. |
| `POST` | `/api/councils` | `DEAN` | Create council with 3–5 members (Chairperson, Secretary, Reviewer, Member). |
| `PUT` | `/api/councils/{id}` | `DEAN` | In-place update of council details and member roles. |
| `DELETE` | `/api/councils/{id}` | `DEAN` | Delete council. Blocked (`409`) if assigned defenses exist. |
| `POST` | `/api/councils/assignments` | `DEAN` | Assign council and reviewer to student group. Blocks if conflict of interest exists. |
| `POST` | `/api/councils/defenses/{id}/grade` | Council Member | Submit individual score (0.00–10.00) and comment. |
| `POST` | `/api/councils/defenses/{id}/finalize` | `CHAIRPERSON` | Synthesize arithmetic average when all members submitted. |
| `POST` | `/api/councils/defenses/{id}/publish` | `DEAN` | Officially publish defense results for students. |
