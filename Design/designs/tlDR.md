### **Document Title: Resolving Terraform EventBridge Rule Conflicts**

**Status:** Approved
**Last Updated:** [Date]

---

#### **Problem Description**
When attempting to destroy Terraform workspaces, the operation fails with:
```
Error: deleting EventBridge Rule (login-ui-login-service-repave): 
ValidationException: Rule can't be deleted since it has targets.
```

**Root Cause:** The EventBridge rule naming convention (`{cluster_name}-{app_name}-repave`) caused multiple workspaces to reference the same AWS resource. While the actual resource was managed by one workspace (`login-service-terraform-region`), a stale workspace still claimed ownership in its state file.

#### **Solution Summary**
Remove the incorrect EventBridge rule reference from the state file of the workspace being destroyed.

#### **Step-by-Step Resolution**

1.  **Identify Ownership**
    Verify which workspace actually manages the rule (in this case, `login-service-terraform-region`).

2.  **Secure the State File**
    ```bash
    terraform state lock          # Prevent concurrent access
    terraform state pull > state.json  # Download current state
    cp state.json state.json.backup    # Create backup
    ```

3.  **Edit the State File**
  - Locate and remove the `aws_cloudwatch_event_rule` resource block
  - Increment the `serial` value by 1

4.  **Apply Changes**
    ```bash
    terraform state push state.json  # Upload modified state
    terraform state unlock           # Restore access
    ```

5.  **Complete Destruction**
    ```bash
    terraform destroy -var-file="variables/sandbox-uat.tfvars"
    ```


#### **Key Points**
- **Cause:** Static naming convention (`{cluster_name}-{app_name}-repave`) caused state conflicts
- **Solution:** State surgery to remove incorrect references
- **Prevention:** Ensure only one workspace manages each unique rule
- **Caution:** Always backup state files before manual edits

```plantuml
@startuml
!theme vibrant

skinparam {
BackgroundColor #F8F9FA
ArrowColor #0D6EFD
ActorBorderColor #6C757D
ActorBackgroundColor #FFFFFF
ParticipantBackgroundColor #FFFFFF
ParticipantBorderColor #0D6EFD
ParticipantFontColor #212529
DatabaseBorderColor #0D6EFD
DatabaseBackgroundColor #FFFFFF
NoteBackgroundColor #FFF3CD
NoteBorderColor #FFC107
GroupBorderColor #0D6EFD
GroupBackgroundColor #E7F1FF
}

title <b>Terraform ECS Workspace Conflict Resolution</b>

actor "<color:#0D6EFD>🏗️ DevOps Engineer" as User

participant "<color:#0D6EFD>🗂️ Workspace A" as WA
participant "<color:#0D6EFD>🗂️ Workspace B" as WB
database "<color:#0D6EFD>📊 EventBridge Rule\n{cluster}-{app}-repave" as Rule

WA -> Rule: Creates reference
WB -> Rule: Creates reference

note right of Rule
<b>Naming Conflict:</b>
Multiple workspaces referencing
the same EventBridge rule
due to static naming convention
end note

User -> WA: Attempts destroy
WA -> Rule: Requests deletion
Rule --> WA: <color:red><b>❌ Error: Rule has targets</b></color>

group <b>Resolution Process</b>
User -> User: Investigates ownership
User -> WB: Confirms active management
User -> WA: <color:green>Removes rule reference from state</color>
User -> WA: Executes destroy
WA --> User: <color:green><b>✅ Success</b></color>
end

note right of User
<b>Resolution Steps:</b>
1. Identifying true owner
2. Removing stale reference
3. Completing destroy operation
   end note

@enduml
```
