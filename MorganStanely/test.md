# RFC: Unified Identity Migration from ForgeRock to Human Identity Service (HIS)

## 📚 Table of Contents

* [Goals](#-goals)
* [Problem Statement](#-problem-statement)
* [High-Level Architecture](#-high-level-architecture)
* [Components](#-components)
* [Technical Design](#-technical-design)
* [Migration Phases](#-migration-phases)
* [Success Criteria](#-success-criteria)
* [ADRs Summary](#-adrs-summary)
* [Migration API](#-migration-api)
* [Conflict Resolution](#-conflict-resolution)
* [Migration Checklist](#-migration-checklist)
* [Open Questions](#-open-questions)

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

## 🏗️ Technical Design

### Architecture Overview

```plantuml
@startuml
skinparam BackgroundColor #FFF
skinparam DefaultFontColor #000

component "Applications" as app
component "HIS Service" as his
database "HIS DB" as db
component "FR Sync Adapter" as sync
component "ForgeRock" as fr

app -right-> his : All write operations
his --> db : CRUD operations
his -> sync : Change notifications
sync -> fr : Limited attribute sync
fr --> sync : Read-only migration access

note right of db
  **HIS is source of truth**
  - Full user profiles
  - Identity relationships
  - Entitlement mappings
end note
@enduml
```

### Client Context Detection Logic

```plantuml
@startuml
skinparam BackgroundColor #FFF
skinparam DefaultFontColor #000

start
:Login Request;
if (OTP Enabled?) then (yes)
    :Set context="PDP";
else (no)
    :Query Entitlements;
    if (Corporate Entitlements?) then (yes)
        :Set context="Corporate";
    else (no)
        :Check HTTP Headers;
        if (Referrer =~ /embedded/) then (yes)
            :Set context="EmbeddedBanking";
        else (no)
            :Default to "Payeeweb";
        endif
    endif
endif
:Persist to Persona;
stop
@enduml
```

### Migration Workflow

```plantuml
@startuml
skinparam BackgroundColor #FFF
skinparam DefaultFontColor #000

group "First New-Journey Login"
    User -> App: Attempts login
    App -> HIS: Checks existence
    alt Not Found
        HIS -> FR: fetchUser(frUserId)
        FR --> HIS: User data + aliasList
        HIS -> HIS: createIdentity()
        HIS -> HIS: createPersonas()
        HIS --> App: Identity created
    else Found
        HIS --> App: Existing identity
    end
end
@enduml
```

### Social Account Linking

```plantuml
@startuml
skinparam BackgroundColor #FFF
skinparam DefaultFontColor #000

package "HIS Identity" {
    [Identity\nID:123] as ident
    [Persona\nPrimary] as primary
    [Persona\nGoogle] as google
    [Persona\nFacebook] as fb
}

package "ForgeRock Legacy" {
    [FR User\nID:456] as fr
    [aliasList] as aliases
}

fr --> aliases : Contains
aliases --> google.subject : "google-id"
aliases --> fb.subject : "fb-id"
ident --> primary : Core identity
ident --> google : Social link
ident --> fb : Social link
@enduml
```

---

## 🔄 Migration Phases

```plantuml
@startuml
skinparam BackgroundColor #FFF
skinparam DefaultFontColor #000

User -> "Auth Service": Login request
"Auth Service" -> HIS: findIdentity(email)
alt Identity missing
    HIS -> "FR Adapter": getUserByEmail(email)
    "FR Adapter" -> ForgeRock: LDAP query
    ForgeRock --> "FR Adapter": User object
    "FR Adapter" --> HIS: User data
    HIS -> "Migration Service": migrateFRUser()
    
    group Migration Process
        "Migration Service" -> "Identity Service": createIdentity()
        "Identity Service" --> "Migration Service": ID
        loop for each alias
            "Migration Service" -> "Persona Service": createSocialPersona()
        end
        "Migration Service" -> "Persona Service": createPrimaryPersona()
    end
end
HIS --> "Auth Service": Identity token
"Auth Service" --> User: JWT
@enduml
```

---

## ✅ Success Criteria

* All active identities created or migrated to HIS.
* All social identities mapped as separate HIS personas.
* No user or entitlement loss during migration.
* Identity APIs updated across client applications.

---

## 📄 ADRs Summary

(ADRs remain unchanged)

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

## ❓ Open Questions

### ❗ High Priority

| Question                                                                | Impact                           | Owner         |
| ----------------------------------------------------------------------- | -------------------------------- | ------------- |
| How to detect Corporate vs Embedded Banking users without entitlements? | High - affects persona structure | Product Team  |
| Should we allow HIS→FR writebacks for session attributes?               | Medium - sync complexity         | Architecture  |
| Retention period for FR data post-migration                             | Legal/Compliance                 | Security Team |

### ⏳ Longer-Term

| Question                     | Consideration                 | Timeline |
| ---------------------------- | ----------------------------- | -------- |
| Full FR decommissioning      | Cost savings vs fallback need | Q2 2025  |
| Multi-region persona support | Data residency requirements   | Q3 2025  |

---