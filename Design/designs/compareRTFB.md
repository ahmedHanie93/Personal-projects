# RTBF (Right to Be Forgotten) Implementation: Identity Providers vs. Stripe

## Objective

To compare how major Identity Providers (IDPs) and Stripe implement the Right to Be Forgotten (RTBF), and how lessons from their approaches can guide the implementation in Sentry — a federated identity front door used in JPMorgan. This version removes any mention of non-standard concepts such as "forget receipts" and references to machine learning.

---

## 1. Summary Table: RTBF Feature Comparison

| Provider     | Soft Delete           | Full Deletion | Anonymization   | Audit Logs Retained | Reinstatement              | Compliance Exemptions Handling    |
| ------------ | --------------------- | ------------- | --------------- | ------------------- | -------------------------- | --------------------------------- |
| **Auth0**    | No                    | Yes           | No              | Yes (decoupled)     | Re-register only           | Delegated to client systems       |
| **Okta**     | Yes                   | Yes           | Manual          | Yes                 | Re-activate or re-register | Requires downstream support       |
| **Azure AD** | Yes (30d)             | Yes           | Partial         | Yes (up to 1 year)  | Restore within window      | Must be implemented at app layer  |
| **Google**   | Yes                   | Yes (20d+)    | Yes (in logs)   | Yes (de-identified) | Re-register only           | Downstream reconfiguration needed |
| **Stripe**   | No (Pseudonymization) | No (PII only) | Yes (Redaction) | Yes (pseudonymized) | New customer entry         | Built-in for legal compliance     |

---

## 2. Provider-Specific RTBF Workflows

### 2.1 Auth0

* **Soft Delete**: Not supported.
* **Full Deletion**: Supported via `DELETE /api/v2/users/{id}`.
* **Anonymization**: Not applied.
* **Audit Logs**: Retained independently from the user record.
* **Reinstatement**: Requires full re-registration.
* **Compliance Handling**: Left to client implementations.
* **API Reference**: [Auth0 Delete User](https://auth0.com/docs/api/management/v2#!/Users/delete_users_by_id)

### 2.2 Okta

* **Soft Delete**: Supported via `POST /api/v1/users/{id}/lifecycle/deactivate`.
* **Full Deletion**: `DELETE /api/v1/users/{id}` (must deactivate first).
* **Anonymization**: Manual implementation.
* **Audit Logs**: Retained for compliance.
* **Reinstatement**: Possible if deactivated; requires re-registration otherwise.
* **Compliance Handling**: Delegated to integrated apps.
* **API Reference**: [Okta Users API](https://developer.okta.com/docs/reference/api/users/)

### 2.3 Microsoft Entra ID (Azure AD)

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
* **Compliance Handling**: Delegated to app configuration.
* **API Reference**: [Google Admin SDK - Directory API](https://developers.google.com/admin-sdk/directory/reference/rest/v1/users/delete)

### 2.5 Stripe

* **Soft Delete**: Not supported.
* **Full Deletion**: `DELETE /v1/customers/{id}` — pseudonymizes PII.
* **Anonymization**: Applies to name, email, phone, and address.
* **Audit Logs**: Retained with redacted user data.
* **Reinstatement**: New customer creation.
* **Compliance Handling**: Native support for Anti-Money Laundering (AML), tax retention.
* **API Reference**: [Stripe Delete Customer](https://stripe.com/docs/api/customers/delete)

---

## 3. Legal Compliance Considerations

### 3.1 GDPR Article 17 Exceptions

Erasure does not apply when data processing is:

* Required by legal obligations (e.g., tax, Anti-Money Laundering regulations)
* Necessary for defense of legal claims

### 3.2 Examples of PII That Must Be Retained

| Data Type            | Reason                            | Retain/Anonymize                |
| -------------------- | --------------------------------- | ------------------------------- |
| Invoices             | Tax compliance                    | ✅ Retain                        |
| Wire transfers       | Anti-Money Laundering regulations | ✅ Retain                        |
| Consent logs         | Legal requirement                 | ✅ Retain                        |
| Transaction metadata | Fraud analysis                    | ✅ Retain (pseudonymize)         |
| Bank account numbers | AML/KYC checks                    | ✅ Retain (mask or limit access) |

---

## 4. Applying These Lessons to Sentry (JPMorgan Use Case)

### 4.1 Context and Challenges

* Sentry is a federated identity gateway — it does not own downstream application data.
* RTBF must propagate to all integrated systems.
* Some downstream data is exempt from deletion.

### 4.2 Recommended RTBF Architecture

1. **Soft delete user** in Sentry (mark as inactive, revoke access).
2. **Notify downstream systems** to remove or anonymize user data.
3. **Track downstream responses** and document compliance reasons if data is retained.
4. **Redact user PII in audit logs** — rather than deleting audit records, redact or replace PII fields to retain forensic and compliance trail without exposing sensitive data.
5. **Ensure auditability** of actions taken per request.

### 4.3 Why Redact PII Instead of Deleting or Anonymizing?

* **Audit logs** serve legal and forensic purposes. Deleting them may violate audit integrity.
* **Redaction** allows PII (e.g., names, emails) to be replaced with placeholders while retaining context.
* **Anonymization** often removes links to the original identity irreversibly. Redaction is reversible only by design, depending on compliance rules.

### 4.4 Pseudonymization vs. Anonymization

* **Anonymization**: Irreversible removal of identifiers such that data cannot be traced back to the individual.
* **Pseudonymization**: Replaces identifiers with artificial ones (e.g., tokenized strings), but allows re-linking under strict controls — useful for audit and fraud prevention use cases.

---

## 5. Key Takeaways

* RTBF is more than just deletion: it includes soft delete, anonymization, token revocation, and documentation.
* Most IDPs rely on client systems to handle PII deletion or masking.
* Stripe offers a mature pseudonymization model under legal constraints.
* For Sentry, the best model includes soft deletion + federated notification + compliance tracking.

---

## 6. Frequently Asked Questions (FAQ)

### Q1: Why not enforce full deletion of user identities in Sentry?

**A:** Full deletion would impair our ability to comply with legal and audit obligations. Soft deletion allows us to revoke user access while retaining minimal metadata (e.g. user ID, timestamps) required for audit trails. This approach also aligns with how Azure AD and Google implement deletion retention.

### Q2: How do we balance GDPR erasure with Anti-Money Laundering (AML) requirements?

**A:** Our model applies classification-based handling: PII not subject to compliance obligations is removed or redacted. Data required for AML, tax, or legal obligations is retained in pseudonymized form, in line with GDPR Article 17 exemptions.

### Q3: How will downstream applications know what data to remove?

**A:** Sentry emits RTBF events to registered systems with a contract defining the user identity, required actions (delete/redact), and compliance traceability ID. Consumers are onboarded with integration contracts and a common RTBF schema.

### Q4: Do audit logs containing PII get deleted?

**A:** No. Deleting logs undermines forensic accountability. Instead, we redact PII fields (e.g. names/emails) to preserve context without exposing identity. This follows a standard practice used by Google and Stripe.

### Q5: Isn’t pseudonymization risky since it’s reversible?

**A:** Pseudonymization is only used when necessary (e.g. audit and fraud systems). Re-identification requires controlled access to the mapping keys. We encrypt these mappings and limit access to privileged roles.

### Q6: Will this introduce latency to the user deletion flow?

**A:** No. Soft deletion in Sentry is immediate. RTBF propagation is asynchronous and tracked via job status logs. This ensures fast response with eventual downstream consistency.

### Q7: What prevents a user from being re-provisioned after deletion?

**A:** A "tombstone" record is retained to prevent re-activation. If the same identity attempts to re-register, the system flags it for review or blocks it outright depending on policy.

### Q8: Is this model scalable for the 100+ systems integrated with Sentry?

**A:** Yes. Sentry acts as a coordinator, not a controller. We provide contracts, event formats, and audit interfaces, while each consumer implements deletion locally. Priority is given to systems with regulatory risk.

---

## References

* [Auth0 GDPR Guide](https://auth0.com/docs/compliance/gdpr)
* [Okta GDPR Overview](https://www.okta.com/trustandcompliance/gdpr/)
* [Microsoft GDPR Center](https://www.microsoft.com/en-us/trust-center/privacy/gdpr-overview)
* [Google Cloud GDPR](https://cloud.google.com/security/gdpr)
* [Stripe GDPR Overview](https://stripe.com/guides/general-data-protection-regulation)
* [GDPR Article 17 - Right to Erasure](https://gdpr-info.eu/art-17-gdpr/)
