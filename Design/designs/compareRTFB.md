# RTBF (Right to Be Forgotten) Implementation: Identity Providers vs. Stripe

## Objective

### Why RTBF Matters

RTBF, under GDPR Article 17, allows users to request deletion of their personal data. While often interpreted as
requiring full deletion, GDPR does not mandate removal of all data—particularly audit or log data necessary for
legitimate business purposes like fraud detection or legal compliance.

GDPR instead emphasizes:

* **Purpose Limitation** *(Art. 5(1)(b))*: Data must only be used for its original, lawful purpose.
* **Data Minimization** *(Art. 5(1)(c))*: Only necessary data should be kept.
* **Storage Limitation** *(Art. 5(1)(e))*: Data should not be retained longer than needed.

> Anonymization or pseudonymization of logs is encouraged where possible but not strictly required unless
> re-identification risk exists.


---

## 1. Summary Table: RTBF Feature Comparison

| Provider      | Soft Delete           | Full Deletion | Anonymization          | Audit Logs Retained                                                                         | Reinstatement              | Compliance Handling                                                   |
|---------------|-----------------------|---------------|------------------------|---------------------------------------------------------------------------------------------|----------------------------|-----------------------------------------------------------------------|
| **Auth0**     | No                    | Yes           | No                     | Yes (logs stored independently from user profile; not automatically linked to deleted user) | Re-register only           | Customer responsible for downstream compliance and retention policies |
| **Okta**      | Yes                   | Yes           | Manual                 | Yes (for audit and regulatory compliance)                                                   | Re-activate or re-register | Customer responsible for downstream compliance and retention policies |
| **Azure AD**  | Yes (30d)             | Yes           | Partial                | Yes (up to 1 year)                                                                          | Restore within window      | Customer responsible for downstream compliance and retention policies |
| **Google**    | Yes                   | Yes (20d+)    | Yes (in logs)          | Yes (de-identified)                                                                         | Re-register only           | Customer responsible for downstream compliance and retention policies |
| **Stripe**    | No (Pseudonymization) | No (PII only) | Yes (Redaction)        | Yes (pseudonymized logs retained for AML and tax laws)                                      | New customer entry         | Built-in regulatory retention for AML, tax, fraud handling            |
| **ForgeRock** | Yes (configurable)    | Yes           | Manual (policy-driven) | Yes (retained by default)                                                                   | Re-register only           | Customer responsible for downstream compliance and retention policies |

---

## 2. Provider-Specific RTBF Workflows

* **Soft Delete**: Implicit — deleted users retained for 30 days.
* **Full Deletion**: `DELETE /users/{id}` via Microsoft Graph API.
* **Anonymization**: Partial; object IDs may remain in logs.
* **Audit Logs**: Retained up to 1 year.
* **Reinstatement**: Allowed within 30-day window.
* **Compliance Handling**: Must be handled at application level.
* **API Reference**: [Microsoft Graph Delete User](https://learn.microsoft.com/en-us/graph/api/user-delete)

### 2.4 Google Cloud Identity

* **Soft Delete**: Suspend user via Admin SDK.
* **Full Deletion**: Use Admin SDK `delete()` method after suspension.
* **Anonymization**: Logs are de-identified.
* **Audit Logs**: Retained with pseudonymization.
* **Reinstatement**: Must re-register.
* **Compliance Handling**: Retention policies managed through Admin SDK config.
* **API Reference
  **: [Google Admin SDK - Directory API](https://developers.google.com/admin-sdk/directory/reference/rest/v1/users/delete)

### 2.5 Stripe

* **Soft Delete**: Not supported.
* **Full Deletion**: `DELETE /v1/customers/{id}` — pseudonymizes PII.
* **Anonymization**: Applies to name, email, phone, and address.
* **Audit Logs**: Retained with redacted user data.
* **Reinstatement**: New customer creation.
* **Compliance Handling**: Native support for Anti-Money Laundering (AML), tax retention.
* **API Reference**: [Stripe Delete Customer](https://stripe.com/docs/api/customers/delete)

### 2.6 ForgeRock

* **Soft Delete**: Configurable via IDM policies or workflows.
* **Full Deletion**: Executed through IDM REST API or policy triggers.
* **Anonymization**: Manual or policy-driven depending on deployment; supports scripted transforms.
* **Audit Logs**: Retained by default unless purged by admin; masking can be configured.
* **Reinstatement**: User must re-register.
* **Compliance Handling**: Requires customer-side implementation and downstream integrations.
* **API Reference**: [ForgeRock IDM Docs](https://backstage.forgerock.com/docs/idm/latest/)

---

## 3. Legal Compliance Considerations

### 3.1 GDPR Article 17 Exceptions

Erasure does not apply when data processing is:

* Required by legal obligations (e.g., tax, Anti-Money Laundering regulations)
* Necessary for defense of legal claims

### 3.2 Examples of PII That Must Be Retained

| Data Type            | Reason                            | Retain/Anonymize                |
|----------------------|-----------------------------------|---------------------------------|
| Invoices             | Tax compliance                    | ✅ Retain                        |
| Wire transfers       | Anti-Money Laundering regulations | ✅ Retain                        |
| Consent logs         | Legal requirement                 | ✅ Retain                        |
| Transaction metadata | Fraud analysis                    | ✅ Retain (pseudonymize)         |
| Bank account numbers | AML/KYC checks                    | ✅ Retain (mask or limit access) |

---

## 4. Applying These Lessons to PAID (JPMorgan Use Case)

### Definitions

* **Anonymization**: Permanently removes all identifiable information from data so that it can never be traced back to an individual. Used when data is no longer needed for any user-specific purpose.
* **Pseudonymization**: Replaces identifiers with artificial ones (e.g., random strings or tokens). The original data can be recovered under strict controls. Useful for use cases like fraud investigation or audits where traceability is still needed.
* **Redaction**: Selectively hides or masks parts of personal data (e.g., hiding name/email in logs). Typically used when logs must be retained but direct identifiers must be concealed.

### When to Use Each

| Technique            | Use Case Example                         | Re-identifiable? | Typical Purpose                              |
| -------------------- | ---------------------------------------- | ---------------- | -------------------------------------------- |
| **Redaction**        | Audit logs, support logs                 | No               | Obscure identifiers without removing context |
| **Anonymization**    | Deleted users with no regulatory linkage | No               | Privacy preservation, analytics              |
| **Pseudonymization** | AML, audit trails, fraud detection       | Yes (controlled) | Retain functionality with limited risk       |

---

## 5. Key Takeaways

* RTBF is more than just deletion: it includes soft delete, anonymization, token revocation, and documentation.
* Most IDPs rely on client systems to handle PII deletion or masking.
* Stripe offers a mature pseudonymization model under legal constraints.
* For PAID, the best model includes soft deletion + federated notification + compliance tracking.

---

## 6. Frequently Asked Questions (FAQ)

### Q1: Why not enforce full deletion of user identities in PAID?

**A:** Full deletion would impair our ability to comply with legal and audit obligations. Soft deletion allows us to revoke user access while retaining minimal metadata (e.g. user ID, timestamps) required for audit trails. This approach also aligns with how Azure AD and Google implement deletion retention.

### Q2: How do we balance GDPR erasure with Anti-Money Laundering (AML) requirements?

**A:** Our model applies classification-based handling: PII not subject to compliance obligations is removed or redacted. Data required for AML, tax, or legal obligations is retained in pseudonymized form, in line with GDPR Article 17 exemptions.

### Q3: How will downstream applications know what data to remove?

**A:** PAID emits RTBF events to registered systems with a contract defining the user identity, required actions (delete/redact), and compliance traceability ID. Consumers are onboarded with integration contracts and a common RTBF schema.

### Q4: Do audit logs containing PII get deleted?

**A:** No. Deleting logs undermines forensic accountability. Instead, we redact PII fields (e.g. names/emails) to preserve context without exposing identity. This follows a standard practice used by Google and Stripe.

### Q5: Isn’t pseudonymization risky since it’s reversible?

**A:** Pseudonymization is only used when necessary (e.g. audit and fraud systems). Re-identification requires controlled access to the mapping keys. We encrypt these mappings and limit access to privileged roles.

### Q6: Why does Auth0 not anonymize PII in logs — isn’t that non-compliant?

**A:** Auth0 retains logs separately from live identity records. These logs capture static values (e.g. email, user ID) at the time of the event and are not dynamically linked to the current user object. GDPR compliance is addressed through **purpose limitation** and **data minimization** (Articles 5(1)(b) and 5(1)(c)), ensuring logs are accessed only for specific, lawful uses and do not contain more data than necessary.

The GDPR permits retaining log data if access is restricted, used for legitimate purposes (e.g. security or compliance), and cannot easily re-identify users. Anonymization or pseudonymization is encouraged, but not strictly required. Auth0 enables compliant use by keeping logs independent of active user identity and recommending masking/redaction during log access.
### Q7: Will this introduce latency to the user deletion flow? 

**A:** No. Soft deletion in PAID is immediate. RTBF propagation is asynchronous and tracked via job status logs. This
ensures fast response with eventual downstream consistency.

### Q8: Do we have an example of DPA in JPMC?

**A:** TBD.


---

## 7. Applicability to CCPA (California Consumer Privacy Act)

While this document focuses on GDPR, PAID’s RTBF model also supports CCPA compliance:

* **Right to Delete**: CCPA gives consumers the right to request deletion of personal information collected.
* **Retention Exceptions**: CCPA allows businesses to retain data for legal obligations, security, or fraud prevention —
  similar to GDPR exemptions.
* **Anonymization vs. Deletion**: CCPA considers data that is anonymized or aggregated as exempt from deletion
  obligations.
* **Scope**: CCPA focuses on consumer data collected/sold/shared, and requires transparency in handling.

---

## 8. Downstream System Responsibilities (GDPR vs. CCPA)

PAID operates as a federated identity front door and does not directly control PII within downstream applications.
However, GDPR and CCPA have different expectations for how upstream systems like PAID interact with integrated
consumers:

### Responsibility Model

| Obligation                            | GDPR Requirement      | CCPA Requirement          |
|---------------------------------------|-----------------------|---------------------------|
| Handle user identity deletion         | ✅ Required (Art. 17)  | ✅ Required (§1798.105)    |
| Notify downstream systems             | ✅ Required (Art. 19)  | ✅ Best practice           |
| Data Processing Agreements (DPAs)     | ✅ Mandatory (Art. 28) | ❌ Not explicitly required |
| Provide RTBF APIs or events           | ✅ Recommended         | ✅ Recommended             |
| Own audit log/data retention policies | ✅ Required            | ✅ Required                |
| Track downstream compliance           | ✅ Recommended         | ❌ Not mandatory           |

###  What PAID Should Do

* Emit **RTBF events** to all connected applications with structured schema.
* Maintain **integration contracts** that define each consumer’s responsibilities.
* Log and trace downstream deletion acknowledgments.
* Ensure **DPAs** exist with critical systems.
* Provide **interfaces for monitoring** propagation and audit status.
* Classify PII vs compliance-critical metadata for each consumer.

---

## References

* [Auth0 GDPR Guide](https://auth0.com/docs/compliance/gdpr)
* [Okta GDPR Overview](https://www.okta.com/trustandcompliance/gdpr/)
* [Microsoft GDPR Center](https://www.microsoft.com/en-us/trust-center/privacy/gdpr-overview)
* [Google Cloud GDPR](https://cloud.google.com/security/gdpr)
* [Stripe GDPR Overview](https://stripe.com/guides/general-data-protection-regulation)
* [GDPR Article 17 - Right to Erasure](https://gdpr-info.eu/art-17-gdpr/)
* [GDPR Article 5 - Principles Relating to Personal Data Processing](https://gdpr-info.eu/art-5-gdpr/)
