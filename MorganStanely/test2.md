# RFC: Migration to Human Identity Service as Source of Truth

## 1. Introduction

### Goals

- Establish SIH as single source of truth for identity data
- Implement persona-based identity modeling
- Enable seamless migration RFom ForgeRock (RF)
- Preserve identity linkages across brands
- Ensure zero downtime and data consistency
- Transition password storage to authentication service

### Problem Statement

RF limitations:

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
    participant RF as ForgeRock
    participant SIH
    participant DB as SIH Database
    User ->> RF: Login Request
    activate RF
    RF ->> SIH: Authenticate
    activate SIH
    alt New Identity
        SIH ->> DB: Create Identity + Personas
    else Existing Identity
        SIH ->> DB: Update Persona
    end
    SIH -->> RF: Auth Result
    deactivate SIH
    RF -->> User: Access Token
    deactivate RF
```

### 2.2 Brand Detection & Identity Resolution

- Primary method: `custom_hasCompletedMFA` parameter
- Fallback: Client-provided user lists
- Detection priority: OTP > entitlements > referrer > default

```mermaid
flowchart TD
    Start[Login] --> OTP{OTP?}
    OTP -->|Yes| DPD[DPD]
    OTP -->|No| Entitlements{Corporate?}
    Entitlements -->|Yes| Corporate
    Entitlements -->|No| Referrer{Embedded?}
    Referrer -->|Yes| Embedded
    Referrer -->|No| Payeeweb
    style DPD fill: #E3F2FD, stroke: #0D47A1
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
    User ->> RF: Login
    RF ->> SIH: Identity Check
    alt Not Found
        SIH ->> RF: Request User Data
        RF -->> SIH: User + aliasList
        loop For each alias
            SIH ->> SIH: createSocialPersona()
        end
        SIH ->> SIH: createIdentity()
    end
    SIH -->> RF: Identity Token
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

rectangle "canada" as canada
rectangle "DPD" as DPD
rectangle "SSO/Prospect" as sso

rectangle "Payeeweb" as payeeweb
rectangle "Embedded Banking" as embedded
rectangle "Corporate Challenge" as corporate

social --> canada
social --> DPD
social --> sso
customer --> payeeweb
customer --> embedded
customer --> corporate

note top of social
  <b>Social Login Journeys</b>
  • canada, DPD, SSO/Prospect
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
- We start with DPD because of minimal risk & least number of users RFom all our customers
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
  • Social: canada, DPD, SSO
  • Customer: Payeeweb, Embedded, Corporate
end note

note top of phase1
  <b>Phase 1 (2025 Q2)</b>
  • Migrate canada
  • Migrate DPD
  • Migrate SSO
end note

note top of phase2
  <b>Phase 2 (2025 Q2-Q3)</b>
  • Payeeweb migration
  • Embedded Banking
  • Corporate Challenge
end note

note top of phase3
  <b>Phase 3 (2024 Q5)</b>
  • Single configurable flow
  • Persona-based customization
end note

note bottom of m1
  <b>Milestone: DPD Migrated</b>
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
@enduml
```

#### 2.7 Customization Points

- **Missing UI screens for mandatory social user data**

## 3. Conflict Resolution

### Resolution Principles

1. **SIH is SSOT**: Wins for core identity attributes
2. **RF Priority**: Wins for session-related attributes
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
state "Commit to SIH" as commit

[*] --> start
start --> identify

identify --> core : Core
identify --> session : Session
identify --> social : Social

core --> resolve : SIH wins
session --> resolve : RF wins
social --> resolve : Merge unique

resolve --> commit
commit --> [*]

note right of core
  <b>Resolution: SIH value</b>
  <i>Reason: SSOT principle</i>
end note

note right of session
  <b>Resolution: RF value</b>
  <i>Reason: Session integrity</i>
end note


note right of social
  <b>Resolution: Merge unique</b>
  <i>Reason: Preserve all links</i>
end note

note bottom of resolve
  <b>Resolution Applied</b>
  • Audit log created
  • SIH updated as SSOT
end note
@enduml
```

## 4. DPD Integration

### 4.1 Profile Creation

```plantuml
@startuml
!define LIGHTBLUE #E6F2FF
!define LIGHTGREEN #E6FFE6

skinparam defaultFontSize 12
skinparam noteFontSize 11
skinparam noteBackgroundColor #FFFDE7
skinparam noteBorderColor #FFECB3

package "Profile Creation" {
  actor "DPD System" as DPD
  component "SIH" as SIH
  database "EEPUM" as EEPUM
  
}

note top of SIH
  <b>Authoritative Profile Creation</b>
  • SIH becomes single source of truth
  • Sentry ID replaces email as primary key
  • All profiles standardized
end note

note top of EEPUM
  <b>EEPUM Changes</b>
  • Sentry ID becomes primary key
  • Email becomes secondary identifier
  • Backward compatibility maintained
end note

note top of DPD
  <b>DPD Migration</b>
  1. Update to use Sentry ID
  2. Retire email-based operations
  3. New API endpoints
end note
@enduml
```

### 4.2 PCI Migration

- We need to move Authentication service to PCI
- DPD should be agnostic to that
- We should avoid having to do a migration on:
    - Non-PCI
    - then RFom Non-PCI to PCI.

[//]: # (### Workflow)

[//]: # ()

[//]: # (1. Transition period: SIH and DPD both create profiles)

[//]: # (2. SIH becomes exclusive creator)

[//]: # (3. Final state: DPD uses SIH-created profiles exclusively)

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

| Phase                  | Activities                                              | Brand Specific |
|------------------------|---------------------------------------------------------|----------------|
| **1: Live Migration**  | Migrate users on the fly during login (DPD first)       | Yes            |
| **2: New Users**       | Direct new users to SIH                                 | Yes            |
| **3: Background Sync** | Run migration RF→SIH sync                               | Yes            |
| **4: SIH as SSOT**     | All write/read operations move to SIH                   | No             |
| **5: Decommission**    | Archive RF data agter all brand users has been migrated | No             |

### 5.2 Key Workflows

#### Password Migration

- On-demand reset during login
- New passwords stored in authentication service
- RF passwords: Delete immediately post-migration.

[//]: # ()

[//]: # (  ```mermaid)

[//]: # (  journey)

[//]: # (   title Password Transition)

[//]: # (   section User)

[//]: # (   Login: 5)

[//]: # (   Reset Prompt: 5)

[//]: # (   Set Password: 5)

[//]: # (   section System)

[//]: # (   Store in Auth Service: 5)

[//]: # (   Delete RFom RF: 5)

[//]: # (  ```)

#### Failure Handling:

```mermaid
flowchart TB
    Failure --> Step1[Maintain RF Access]
    Failure --> Step2[Recreate RFom Backup]
    Failure --> Step3[Manual Intervention]
```

## 6. Decision FAQ

| Question                                                                  | Decision                                                 | Rationale                                                                                                      | Implementation                                                                 |
|---------------------------------------------------------------------------|----------------------------------------------------------|----------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------|
| **How to handle users with multiple RF accounts sharing the same email?** | Merge into single SIH identity with multiple personas    | Prevents identity RFagmentation<br>Preserves all entitlements<br>Maintains access across all original accounts | Automated merge during migration<br>Admin notification for manual verification |
| **Should we allow SIH → RF writebacks in case of a conflict?**            | No writebacks                                            | Enables safe rollback<br>Prevents synchronization conflicts                                                    | Maintains RF as read-only during transition except for session details.        |
| **Do we need to delete RF data post-migration?**                          | Delete passwords immediately.<br>Might delete aliasList. | Protect our users passwords                                                                                    | Delete job                                                                     |

## 7. Follow-Up ADRs

### ADR-001: SIH as Source of Truth

- All identity writes route to SIH
- RF becomes read-only during transition

### ADR-002: Persona-Based Identity

- Social identities as separate personas
- Single identity for consolidated management

### ADR-003: Password Migration

- On-demand reset during login
- No bulk password transfers