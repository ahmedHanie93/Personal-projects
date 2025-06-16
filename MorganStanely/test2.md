# RFC: Migration to Human Identity Service as Source of Truth

## 1. Introduction

### Goals

- Establish HIS as single source of truth for identity data
- Implement persona-based identity modeling
- Enable seamless migration from ForgeRock (FR)
- Preserve identity linkages across brands
- Ensure zero downtime and data consistency
- Transition password storage to authentication service

### Problem Statement

FR limitations:

- No persona-based identity modeling
- SaaS model limits customization
- Inadequate entitlement isolation
- No unified social identity linking
- Password storage in third-party SaaS

## 2. Technical Design

### 2.1 Core Architecture

```mermaid
sequenceDiagram
    actor User
    participant FR as ForgeRock
    participant HIS
    participant DB as HIS Database
    User ->> FR: Login Request
    activate FR
    FR ->> HIS: Authenticate
    activate HIS
    alt New Identity
        HIS ->> DB: Create Identity + Personas
    else Existing Identity
        HIS ->> DB: Update Persona
    end
    HIS -->> FR: Auth Result
    deactivate HIS
    FR -->> User: Access Token
    deactivate FR
```

### 2.2 Brand Detection & Identity Resolution

- Primary method: `custom_hasCompletedMFA` parameter
- Fallback: Client-provided user lists
- Detection priority: OTP > entitlements > referrer > default

```mermaid
flowchart TD
    Start[Login] --> OTP{OTP?}
    OTP -->|Yes| PDP[PDP]
    OTP -->|No| Entitlements{Corporate?}
    Entitlements -->|Yes| Corporate
    Entitlements -->|No| Referrer{Embedded?}
    Referrer -->|Yes| Embedded
    Referrer -->|No| Payeeweb
    style PDP fill: #E3F2FD, stroke: #0D47A1
    style Corporate fill: #E8F5E9, stroke: #2E7D32
    style Embedded fill: #FFF8E1, stroke: #F57F17
    style Payeeweb fill: #F3E5F5, stroke: #6A1B9A
    Duplicate[Duplicate Emails] --> Merge[Merge Identity]
    Merge --> Persona1[Persona 1]
    Merge --> Persona2[Persona 2]
```

### 2.3 Migration Workflow

```mermaid
sequenceDiagram
    User ->> FR: Login
    FR ->> HIS: Identity Check
    alt Not Found
        HIS ->> FR: Request User Data
        FR -->> HIS: User + aliasList
        loop For each alias
            HIS ->> HIS: createSocialPersona()
        end
        HIS ->> HIS: createIdentity()
    end
    HIS -->> FR: Identity Token
```

### 2.4 Social Account Linking

```mermaid
erDiagram
    IDENTITY ||--o{ PERSONA: contains
    IDENTITY {
        string id PK
        string status
    }
    PERSONA {
        string id PK
        string identity_id FK
        string type "PRIMARY|SOCIAL"
        string provider
        string subject
    }
    FORGEROCK_USER ||--o{ ALIAS_LIST: has
```

### 2.5 Journey Classification

```plantuml
```plantuml
@startuml
left to right direction

!define SOCIAL_COLOR #E1F5FE
!define CUSTOMER_COLOR #F1F8E9

rectangle "Social Login Journeys" as social <<social>> SOCIAL_COLOR
rectangle "Customer Login Journeys" as customer <<customer>> CUSTOMER_COLOR

social -[hidden]-> customer : Future unification

rectangle "Scotia" as scotia
rectangle "PDP" as pdp
rectangle "SSO/Prospect" as sso

rectangle "Payeeweb" as payeeweb
rectangle "Embedded Banking" as embedded
rectangle "Corporate Challenge" as corporate

social --> scotia
social --> pdp
social --> sso
customer --> payeeweb
customer --> embedded
customer --> corporate

note top of social
  <b>Social Login Journeys</b>
  • Scotia, PDP, SSO/Prospect
  • Supports social providers
  • Unified under default_Federation
end note

note bottom of customer
  <b>Customer Login Journeys</b>
  • Payeeweb, Embedded, Corporate
  • Email/password authentication
  • No social login support
end note
@enduml
```

### 2.6 Journey Unification Timeline

```plantuml
@startuml
left to right direction

rectangle "Current State" as current
rectangle "Phase 1" as phase1
rectangle "Phase 2" as phase2
rectangle "Phase 3" as phase3
rectangle "Milestone 1" as m1
rectangle "Milestone 2" as m2
rectangle "Milestone 3" as m3

current --> phase1
phase1 --> phase2
phase2 --> phase3
phase1 --> m1
phase1 --> m2
phase3 --> m3

note top of current
  <b>Current State (2023 Q4)</b>
  • Separate journeys
  • Social: Scotia, PDP, SSO
  • Customer: Payeeweb, Embedded, Corporate
end note

note top of phase1
  <b>Phase 1 (2024 Q1)</b>
  • Migrate Scotia
  • Migrate PDP
  • Migrate SSO
end note

note top of phase2
  <b>Phase 2 (2024 Q2-Q3)</b>
  • Payeeweb migration
  • Embedded Banking
  • Corporate Challenge
end note

note top of phase3
  <b>Phase 3 (2024 Q4)</b>
  • Single configurable flow
  • Persona-based customization
end note

note bottom of m1
  <b>Milestone: PDP Migrated</b>
  2024-03-01
end note

note bottom of m2
  <b>Milestone: Social Complete</b>
  2024-04-01
end note

note bottom of m3
  <b>Milestone: Full Unification</b>
  2024-12-31
end note
@enduml```

#### Journey Unification Roadmap
```plantuml
@startuml
gantt
  title Journey Unification Timeline
  dateFormat  YYYY-MM-DD
  axisFormat  %b %Y
  
  [Current State] : 2023-11-01, 60d
  section Phase 1
  Social unification : active, after end, 90d
  section Phase 2
  Customer adoption : after Social unification, 90d
  section Future State
  Unified configurable journey : after Customer adoption, 0d
  section Milestones
  PDP migration complete : milestone, 2024-01-31, 0d
  All brands on default_Federation : milestone, 2024-06-30, 0d
  Full unification : milestone, 2024-12-31, 0d
@enduml
```

#### 2.7 Customization Points

```plantuml
@startuml
left to right direction

component "Authentication Service" as auth
database "Persona Configuration" as config

auth --> config : Read persona settings
config --> auth : Apply configurations

package "Runtime Customization" {
  rectangle "Social Provider Toggle" as social
  rectangle "Branding Elements" as branding
  rectangle "Consent Screens" as consent
  rectangle "Entitlement Rules" as entitlements
}

config --> social
config --> branding
config --> consent
config --> entitlements

note right of config
  <b>Configuration Elements:</b>
  • Social provider enablement
  • Consent screen content
  • Branding elements
  • Entitlement rules
end note
@enduml
```

- **Missing UI screens for mandatory social user data**

## 3. Conflict Resolution

### Resolution Principles

1. **HIS is SSOT**: Wins for core identity attributes
2. **FR Priority**: Wins for session-related attributes
3. **Version Control**: Timestamp-based conflict detection
4. **Attribute-specific Rules**: Different resolution per parameter type

```plantuml
@startuml
skinparam {
  state {
    BackgroundColor<<Core>> #FFEBEE
    BackgroundColor<<Session>> #E3F2FD
    BackgroundColor<<Entitlement>> #F1F8E9
    BackgroundColor<<Social>> #FFF8E1
    ArrowColor #444444
  }
  note {
    BackgroundColor #FFFFFF
    BorderColor #CCCCCC
  }
}

left to right direction

state "Conflict Detected" as start #White
state "Identify Attribute Type" as identify
state "Core Identity\n(email, phone, status)" as core <<Core>>
state "Session Data\n(lastLogin, loginCount)" as session <<Session>>
state "Social Links\n(aliasList, connections)" as social <<Social>>
state "Apply Resolution" as resolve
state "Commit to HIS" as commit

[*] --> start
start --> identify

identify --> core : Core
identify --> session : Session
identify --> social : Social

core --> resolve : HIS wins
session --> resolve : FR wins
social --> resolve : Merge unique

resolve --> commit
commit --> [*]

note right of core
  <b>Resolution: HIS value</b>
  <i>Reason: SSOT principle</i>
end note

note right of session
  <b>Resolution: FR value</b>
  <i>Reason: Session integrity</i>
end note


note right of social
  <b>Resolution: Merge unique</b>
  <i>Reason: Preserve all links</i>
end note

note bottom of resolve
  <b>Resolution Applied</b>
  • Audit log created
  • HIS updated as SSOT
end note
@enduml
```

## 4. PDP Integration

### 4.1 Profile Creation

```plantuml
@startuml
!define LIGHTBLUE #E6F2FF
!define LIGHTGREEN #E6FFE6

skinparam defaultFontSize 12
skinparam noteFontSize 11
skinparam noteBackgroundColor #FFFDE7
skinparam noteBorderColor #FFECB3

left to right direction

package "Profile Creation" {
  actor "PDP System" as pdp
  component "HIS" as his
  database "PUPEE" as pupee
  
}

note right of his
  <b>Authoritative Profile Creation</b>
  • HIS becomes single source of truth
  • Sentry ID replaces email as primary key
  • All profiles standardized
end note

note right of pupee
  <b>PUPEE Changes</b>
  • Sentry ID becomes primary key
  • Email becomes secondary identifier
  • Backward compatibility maintained
end note

note top of pdp
  <b>PDP Migration</b>
  1. Update to use Sentry ID
  2. Retire email-based operations
  3. New API endpoints
end note
@enduml
```

### 4.2 PCI Migration

PDP should be agnostic to that, we should avoid having to do a migration twice from FR-SaaS to authentication service on
Non-PCI and then from Non-PCI to PCI.

```plantuml
@startuml
!define LIGHTBLUE #E6F2FF
!define LIGHTPURPLE #EDE7F6

skinparam defaultFontSize 12
skinparam rectangle {
  BackgroundColor<<PCI>> #FFEBEE
  BackgroundColor<<NonPCI>> #E8F5E9
}

rectangle "PCI Environment" <<PCI>> {
  component "Authentication Service" as auth
  database "PCI Database" as db
}

rectangle "Non-PCI Environment" <<NonPCI>> {
  actor "PDP System" as pdp
  component "HIS" as his
}

auth --> db : Passwords Secure storage

note top of auth
  <b>Zero PDP Impact Guarantee</b>
  • Identical API contracts
  • Same endpoint URLs
  • Unchanged request/response formats
end note

note right of pdp
  <b>No Changes Required</b>
  • Agnostic to infrastructure
  • Unaware of PCI migration
  • Seamless transition
end note

note bottom of db
  <b>Security Benefits</b>
  • PCI-compliant storage
  • Enhanced encryption
  • Regular audits
end note
@enduml
```

[//]: # (### Workflow)

[//]: # ()

[//]: # (1. Transition period: HIS and PDP both create profiles)

[//]: # (2. HIS becomes exclusive creator)

[//]: # (3. Final state: PDP uses HIS-created profiles exclusively)

## 5. Migration Strategy

### 5.1 Phased Approach

[//]: # (```mermaid)

[//]: # (gantt)

[//]: # (    title Migration Timeline)

[//]: # (    dateFormat YYYY-MM-DD)

[//]: # (    section Phase 1)

[//]: # (        Live Migration: active, p1, 2024-01-01, 30d)

[//]: # (    section Phase 2)

[//]: # (        New Users: p2, after p1, 30d)

[//]: # (    section Phase 3)

[//]: # (        Background Sync: p3, after p2, 30d)

[//]: # (```)

| Phase                  | Activities                             | Brand Specific |
|------------------------|----------------------------------------|----------------|
| **1: Live Migration**  | Migrate users during login (PDP first) | Yes            |
| **2: New Users**       | Direct new users to HIS                | Yes            |
| **3: Background Sync** | Scheduled FR→HIS sync                  | Yes            |
| **4: HIS as SSOT**     | Disable FR writes                      | No             |
| **5: Decommission**    | Archive FR data                        | No             |

### 5.2 Key Workflows

#### Password Migration

- On-demand reset during login
- New passwords stored in authentication service
  ```mermaid
  journey
   title Password Transition
   section User
   Login: 5
   Reset Prompt: 5
   Set Password: 5
   section System
   Store in Auth Service: 5
   Delete from FR: 5
  ```

#### Failure Handling:

```mermaid
flowchart TB
    Failure --> Step1[Maintain FR Access]
    Failure --> Step2[Recreate from Backup]
    Failure --> Step3[Manual Intervention]
```

- **FR passwords: Delete immediately post-migration**

## 6. Decision FAQ

| Question                                                                  | Decision                                                      | Rationale                                                                                                      | Implementation                                                                 |
|---------------------------------------------------------------------------|---------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------|
| **How to handle users with multiple FR accounts sharing the same email?** | Merge into single HIS identity with multiple personas         | Prevents identity fragmentation<br>Preserves all entitlements<br>Maintains access across all original accounts | Automated merge during migration<br>Admin notification for manual verification |
| **Should we allow HIS → FR writebacks for session attributes?**           | No writebacks                                                 | Enables safe rollback<br>Prevents synchronization conflicts<br>Maintains FR as read-only during transition     | Block all write operations to FR<br>Audit any attempted writebacks             |
| **Do we need to delete FR data post-migration?**                          | Delete passwords immediately<br>Purge aliasList after 30 days | Security compliance (passwords in SaaS)<br>Reduce attack surface<br>Privacy regulations (PII minimization)     | Automated scrubbing jobs<br>Verification audits<br>Compliance documentation    |

## 7. Follow-Up ADRs

### ADR-001: HIS as Source of Truth

- All identity writes route to HIS
- FR becomes read-only during transition

### ADR-002: Persona-Based Identity

- Social identities as separate personas
- Single identity for consolidated management

### ADR-003: Password Migration

- On-demand reset during login
- No bulk password transfers