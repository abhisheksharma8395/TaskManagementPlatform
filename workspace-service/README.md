# workspace-service

**FlowBoard** – Workspace & Membership Microservice  
Port: **8082** | Spring Boot 4.0.5 | Java 17 | PostgreSQL

---

## Responsibilities

Manages the top-level organisational unit of the FlowBoard platform.

| Domain          | Capabilities                                                          |
|-----------------|-----------------------------------------------------------------------|
| Workspace CRUD  | Create, read, update, delete workspaces with PUBLIC/PRIVATE visibility |
| Membership      | Add / remove members, promote / demote roles (ADMIN / MEMBER)         |
| Access control  | Only workspace owner or ADMIN members can modify; PRIVATE workspaces are member-only |
| User validation | Verifies userId against **auth-service** via Feign before adding member |

---

## REST API Endpoints

### Workspaces
| Method | Path                        | Description                   | Auth Required |
|--------|-----------------------------|-------------------------------|---------------|
| POST   | `/workspaces`               | Create a new workspace         | ✅ JWT        |
| GET    | `/workspaces/{id}`          | Get workspace by ID            | ✅ JWT        |
| GET    | `/workspaces/owner/{id}`    | All workspaces owned by user   | ✅ JWT        |
| GET    | `/workspaces/member/{id}`   | All workspaces user belongs to | ✅ JWT        |
| GET    | `/workspaces/public`        | List all PUBLIC workspaces     | ✅ JWT        |
| PUT    | `/workspaces/{id}`          | Update workspace details       | ✅ JWT (Admin/Owner) |
| DELETE | `/workspaces/{id}`          | Delete workspace               | ✅ JWT (Owner only)  |

### Members
| Method | Path                                        | Description               | Auth Required         |
|--------|---------------------------------------------|---------------------------|-----------------------|
| POST   | `/workspaces/{id}/members`                  | Add a member              | ✅ JWT (Admin/Owner)  |
| DELETE | `/workspaces/{id}/members/{userId}`         | Remove a member           | ✅ JWT (Admin/Owner)  |
| PUT    | `/workspaces/{id}/members/{userId}/role`    | Update member role        | ✅ JWT (Admin/Owner)  |
| GET    | `/workspaces/{id}/members`                  | List all members          | ✅ JWT (any member)   |

---

## Architecture

```
WorkspaceController
      │
      ▼
WorkspaceService (interface)
      │
      ▼
WorkspaceServiceImpl ──────────────────────────────────────────┐
      │                                                         │
      ├── WorkspaceRepository (JPA)                            │
      ├── WorkspaceMemberRepository (JPA)                      │
      └── AuthServiceClient (Feign → auth-service:8081)       │
                                                               │
Entities: Workspace, WorkspaceMember                           │
Database: PostgreSQL (schema auto-created via ddl-auto=update) ┘
```

---

## Running Locally

### Prerequisites
- PostgreSQL running on port `5433`, database `TaskManager`
- **service-registry** (Eureka) running on port `8761`
- **auth-service** running on port `8081`

### Start
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### Swagger UI
```
http://localhost:8082/swagger-ui/index.html
```

---

## JWT Token Requirements

This service validates JWT tokens issued by **auth-service**.  
The token must contain:
- `sub` — username  
- `role` — user role (e.g. `USER`, `ADMIN`)  
- `userId` — the user's database ID (**added in auth-service update**)

Send token in every request header:
```
Authorization: Bearer <token>
```

---

## Auth-Service Changes Required

The following files in **auth-service** were updated to embed `userId` in the JWT:

| File | Change |
|------|--------|
| `JWTUtil.java` | Added overloaded `generateToken(username, role, userId)` |
| `AuthServiceImpl.java` | `login()` now calls `generateToken` with `userId` |
| `OAuth2LoginSuccessHandler.java` | Also passes `userId` to `generateToken` |

---

## Environment Variables (Production)

| Variable       | Description                        |
|----------------|------------------------------------|
| `DB_URL`       | JDBC URL for PostgreSQL             |
| `DB_USERNAME`  | Database username                   |
| `DB_PASSWORD`  | Database password                   |
| `JWT_SECRET`   | Base64 JWT signing secret (same as auth-service) |
| `EUREKA_URL`   | Eureka server URL                   |
| `AUTH_SERVICE_URL` | auth-service base URL (fallback if Eureka unavailable) |
