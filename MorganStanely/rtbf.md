# RTBF (Right to Be Forgotten) Implementation: Identity Providers vs. Stripe

## Objective

To compare how major Identity Providers (IDPs) and Stripe implement the Right to Be Forgotten (RTBF), and how lessons from their approaches can guide the implementation in Sentry — a federated identity front door used in JPMorgan.

---

## 1. Summary Table: RTBF Feature Comparison

| Provider     | Soft Delete           | Full Deletion | Anonymization   | Audit Logs Retained | Reinstatement               | Compliance Exemptions Handling          |
| ------------ | --------------------- | ------------- | --------------- | ------------------- | --------------------------- | --------------------------------------- |
| **Auth0**    | No                    | Yes           | No              | Yes (decoupled)     | Re-register only            | Not applicable (delegated to client)    |
| **Okta**     | Yes                   | Yes           | Manual          | Yes                 | Re-activate or re-register  | No built-in, left to integration        |
| **Azure AD** | Yes (30d)             | Yes           | Partial         | Yes (up to 1 year)  | Restore if within retention | No built-in, dependent on app layer     |
| **Google**   | Yes                   | Yes (20d+)    | Yes (in logs)   | Yes (de-identified) | Re-register only            | No built-in, user must reconfigure apps |
| **Stripe**   | No (Pseudonymization) | No (PII only) | Yes (Redaction) | Yes (pseudonymized) | New customer entry          | Full legal compliance embedded          |

---

## 2. Provider-Specific RTBF Workflows

### 2.1 Auth0

* **Deletion API**: `DELETE /api/v2/users/{id}` permanently deletes the user.
* **What’s removed**: All profile data, credentials.
* **What’s retained**: Audit logs (disconnected from user profile).
* **No anonymization**: Deletes PII fully.
* **Reinstatement**: Identity must be fully re-created; no connection to prior identity.

### 2.2 Okta

* **Soft delete**: Deactivate user (revokes sessions and tokens).
* **Full delete**: Via Admin UI or API. Irreversible.
* **Audit logs**: Retained for compliance.
* **Anonymization**: Not automated, can be implemented manually.
* **Reinstatement**: Can restore if soft-deleted, otherwise re-register.

### 2.3 Microsoft Entra ID (Azure AD)

* **Soft delete**: 30-day retention.
* **Hard delete**: After window, identity is purged.
* **Anonymization**: Partial; logs may retain object IDs.
* **Audit logs**: Retained up to 1 year.
* **Reinstatement**: Possible within retention period only.

### 2.4 Google Cloud Identity

* **Soft delete**: Suspension then deletion after 20 days.
* **Anonymization**: Audit logs are de-identified.
* **Data transfer**: Option to transfer ownership of user-generated content before deletion.
* **Reinstatement**: Only via new account creation.

### 2.5 Stripe

* **Hard delete not permitted** due to AML, tax, legal requirements.
* **Pseudonymization**: Redacts email, name, phone; keeps transaction records.
* **Compliance alignment**: Retains required financial data per law.
* **Reinstatement**: Treated as a new customer.

---

## 3. Legal Compliance Considerations

### 3.1 GDPR Article 17 Exceptions

Under GDPR, RTBF does **not** apply when:

* Required by law (e.g., AML, tax laws)
* For legal defense or regulatory audits

### 3.2 Financial PII That Must Be Retained

| Data Type                          | Reason                 | Retain/Anonymize        |
| ---------------------------------- | ---------------------- | ----------------------- |
| Invoices                           | Tax compliance         | ✅ Retain                |
| Wire transfer logs                 | AML regulations        | ✅ Retain                |
| Consent logs                       | GDPR Art. 7(1)         | ✅ Retain                |
| Transaction metadata               | Fraud, chargeback risk | ✅ Retain (pseudonymize) |
| Bank account info used in payments | AML/KYC                | ✅ Retain (mask access)  |

---

## 4. Applying These Lessons to Sentry (JPMorgan Use Case)

### 4.1 Challenges

* Sentry is the front door to multiple protected JPMorgan applications.
* Sentry does not own downstream data (e.g., PII in payment/trade systems).
* Some data is legally exempt from deletion.

### 4.2 Recommended RTBF Architecture

1. **Soft delete user** in Sentry (identity deactivated, tokens revoked).
2. **Trigger RTBF event** to downstream services (e.g., PUPEE, payments, analytics).
3. **Downstream systems respond**:

    * Delete or anonymize user data.
    * Retain exempt data under `GDPR_EXEMPT` flag.
4. **Logs and audits** in Sentry:

    * Mask/redact PII in logs.
    * Retain anonymized traces.
5. **Compliance evidence storage**:

    * Track request, actions taken, exceptions logged.

---

## 5. Summary and Key Takeaways

* RTBF is **not just deletion** — it's revocation + audit + legal balancing.
* Most IDPs support hard deletion but rely on **clients/apps to clean downstream PII**.
* Stripe sets the standard for **pseudonymization** to meet legal obligations.
* For Sentry, the best path is a **hybrid model**:

    * Soft-delete identity
    * Broadcast RTBF
    * Track downstream execution
    * Mask rather than delete where compliance requires

---

## References

* [Auth0 GDPR Guide](https://auth0.com/docs/compliance/gdpr)
* [Okta GDPR Overview](https://www.okta.com/trustandcompliance/gdpr/)
* [Microsoft GDPR Center](https://www.microsoft.com/en-us/trust-center/privacy/gdpr-overview)
* [Google Cloud GDPR](https://cloud.google.com/security/gdpr)
* [Stripe GDPR Overview](https://stripe.com/guides/general-data-protection-regulation)
* [GDPR Article 17 - Right to Erasure](https://gdpr-info.eu/art-17-gdpr/)
