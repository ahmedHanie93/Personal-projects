# RFC: Migration Strategy from ForgeRock to Human Identity Service (HIS)

## 📚 Table of Contents

* [Goals](#-goals)
* [Problem Statement](#-problem-statement)
* [High-Level Architecture](#-high-level-architecture)
* [Components](#-components)
* [Migration Phases](#-migration-phases)
* [Success Criteria](#-success-criteria)
* [ADRs Summary](#-adrs-summary)
* [Migration API](#-migration-api)
* [Conflict Resolution](#-conflict-resolution)
* [Migration Checklist](#-migration-checklist)

---

## 📌 Goals

* Establish HIS as the **single source of truth** for all identity and authentication data.
* Provide **persona-based identity modeling** with contextual entitlements.
* Enable **seamless migration** of users from ForgeRock (FR) to HIS.
* Preserve **identity linkages** across client contexts and social providers.
* Ensure **zero downtime**, data consistency, and forward compatibility.

---

## ❗ Problem Statement

ForgeRock is currently the central identity provider, handling user creation, authentication, and storage. However:

* FR lacks the concept of **personas** and contextual entitlement isolation.
* It cannot **intelligently consolidate** users across social providers.
* FR is a SaaS-managed system, limiting **customization and resilience**.
* **HIS** offers deeper identity modeling, control over data, and supports multi-client identity relationships.

---

## 🔧 High-Level Architecture

```plantuml
@startuml
skinparam linetype ortho
actor "Applications" as App
actor "Social IdPs" as IdPs
rectangle "ForgeRock Adapter" as Adapter
rectangle "HIS Service" as HIS {
  database "HIS Database" as DB
}
App --> HIS
IdPs --> Adapter
Adapter <--> HIS
HIS --> DB
@enduml
```

---

## 🧩 Components

| Component         | Description                                         |
| ----------------- | --------------------------------------------------- |
| **Identity**      | Global object per user                              |
| **Persona**       | Context-specific view of user (PDP, Embedded, etc.) |
| **Alias**         | Social identity mapping (Google, Facebook)          |
| **FR Adapter**    | Handles interim syncs from FR to HIS                |
| **Migration API** | Just-in-time and batch user migration               |

---

## 🔄 Migration Phases

1. **Phase 1**: New user registrations go to HIS only.
2. **Phase 2**: On-the-fly migration for existing FR users.
3. **Phase 3**: Scheduled background syncs from FR to HIS.
4. **Phase 4**: All writes/read operations move to HIS.
5. **Phase 5**: FR disabled for identity operations.

---

## ✅ Success Criteria

* All active identities created or migrated to HIS.
* All social identities mapped as separate HIS personas.
* No user or entitlement loss during migration.
* Identity APIs updated across client applications.

---

## 📄 ADRs Summary

### ADR 001: Make HIS the Source of Truth

* **Decision**: All future identity flows move to HIS.
* **Rationale**: FR lacks support for personas, flexibility.

### ADR 002: Represent Each Social Identity as Separate Persona

* **Decision**: Use social provider + subject as unique persona.
* **Rationale**: Ensures identity consolidation, entitlement isolation.

### ADR 003: Migrate FR Users On-the-Fly

* **Decision**: Check FR during login if user missing in HIS.
* **Rationale**: Avoids need for a big bang migration.

### ADR 004: Maintain Legacy FR IDs Temporarily

* **Decision**: Store FR IDs in HIS metadata during transition.
* **Rationale**: Maintains backward compatibility.

### ADR 005: Derive Client Context from Login Metadata

* **Decision**: Detect PDP, Embedded Banking, etc., from request signals.
* **Rationale**: Supports accurate persona creation.

---

## ✅ Migration API

`POST /identities/migrate-from-fr`

```json
{
  "frUserId": "uuid",
  "clientContext": "detected/client",
  "migrationType": "SOCIAL|PRIMARY"
}
```

---

## 🔁 Conflict Resolution

```python
def resolve_conflict(his_data, fr_data):
  core_fields = ['email', 'phone']
  for field in core_fields:
    if his_data.get(field) != fr_data.get(field):
      return his_data[field]  # HIS wins

  session_fields = ['lastLogin', 'loginCount']
  for field in session_fields:
    if fr_data.get(field):
      return fr_data[field]  # FR wins for session
```

---

## 🧠 Persona Mapping Table

| Type    | Source      | Example                                   | HIS Persona Type | Notes                      |
| ------- | ----------- | ----------------------------------------- | ---------------- | -------------------------- |
| Primary | FR Email    | [user@domain.com](mailto:user@domain.com) | PRIMARY          | Core login method          |
| Social  | Google ID   | sub: abc123                               | SOCIAL           | Linked via aliasList       |
| Social  | Facebook ID | sub: fb456                                | SOCIAL           | Linked via aliasList       |
| Client  | PDP OTP     | Derived from metadata                     | PDP Persona      | Has OTP / PDP entitlements |

---

## ✅ Migration Checklist

* [ ] HIS service accepts and stores new user registrations
* [ ] Social identities mapped as distinct personas
* [ ] On-the-fly login migration in place
* [ ] Scheduled sync jobs implemented
* [ ] Logging and metrics dashboard ready
* [ ] Conflict handling logic deployed and verified
* [ ] Rollback plan documented and tested
* [ ] All clients integrated with HIS identity APIs

---

End of RFC
