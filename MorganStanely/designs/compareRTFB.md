# RTBF Market Research & Implementation Comparison

## 1. Purpose

To assess how major identity and access management platforms implement Right to Be Forgotten (RTBF), in order to derive best practices and inform the proposed Sentry RTBF implementation.

## 2. Platforms Analyzed

* Stripe
* Auth0 (Okta)
* Microsoft Azure AD
* Google Cloud Identity
* Internal JPMorgan Systems (PUPEE, PDP)

---

## 3. Comparison Matrix

| Capability                   | Stripe          | Auth0 / Okta   | Azure AD     | Google Cloud | Sentry (Proposed)       |
| ---------------------------- | --------------- | -------------- | ------------ | ------------ | ----------------------- |
| **Pseudonymization**         | ✅ HMAC w/ salt  | ❌              | ❌            | Partial      | ✅ HMAC-SHA256 w/ HSM    |
| **Legal Holds / Exemptions** | ✅ AML filters   | ❌              | ✅ PDP policy | ❌            | ✅ PDP-based filtering   |
| **Plugin Interface**         | ❌               | ✅ Rules Engine | ❌            | ❌            | 🚧 Planned (Phase 3)    |
| **Forget Receipts**          | ❌               | ❌              | ❌            | ❌            | ✅ Logged & Signed       |
| **Audit Trail Safety**       | ✅ Pseudonymized | ❌              | ✅ Partial    | ❌            | ✅ Compliant Retention   |
| **Backup Handling**          | ❌               | ❌              | ❌            | ❌            | ✅ Tombstoning (Planned) |
| **ML Unlearning**            | ❌               | ❌              | ❌            | ❌            | ✅ (Phase 3)             |
| **Downstream Notification**  | Partial         | ✅ Hooks        | ❌            | ❌            | ✅ Kafka/HTTP Notify     |

---

## 4. Platform Notes

### 4.1 Stripe

* Leverages HMAC-based pseudonymization to retain analytics.
* Payment records kept for 7 years under financial regulations.
* No unified "forget receipt" mechanism.
* Strong support for audit preservation.

### 4.2 Auth0 / Okta

* Primarily supports deletion via Rules or Management API.
* No built-in pseudonymization.
* Plugin support through Rules/Actions system.
* No enforcement of downstream log removal.

### 4.3 Microsoft Azure AD

* Legal hold support via PDP evaluation.
* RTBF partially available via PowerShell/API.
* No cryptographic pseudonymization.
* Data retention policies often override deletion.

### 4.4 Google Cloud Identity

* No RTBF API or enforcement model.
* Some data can be removed manually or via DLP tools.
* No receipt logging or downstream integration.

### 4.5 JPMorgan Internal (PUPEE / PDP)

* PUPEE supports entitlement revocation.
* PDP can evaluate legal exceptions.
* Lacks full RTBF support (pseudonymization, receipt logging, audit safety).

---

## 5. Lessons for Sentry

| Insight                                                                     | Impact on Sentry Design                      |
| --------------------------------------------------------------------------- | -------------------------------------------- |
| Cryptographic pseudonymization helps retain audit data while protecting PII | Use HMAC with HSM salt in Phase 1            |
| Legal hold and AML exemptions are critical in financial institutions        | Integrate PDP policy engine early            |
| Forget Receipts improve audit transparency                                  | Define and log JSON receipts with signatures |
| Plugin-like architecture allows scaling to more systems                     | Plan `Forgettable` interface for Phase 3     |
| Notifications to downstream systems are often overlooked                    | Start with Kafka + REST in Phase 1           |

---

## 6. Next Steps

* Use this research to justify the Phase 1 architecture in the Sentry RFC.
* Inform internal stakeholders (Security, Legal, Compliance) of aligned practices.
* Prepare for reviews with enterprise architecture board.

---

## 7. References

* [Stripe GDPR Implementation](https://stripe.com/docs/security/gdpr)
* [Azure AD GDPR Guide](https://learn.microsoft.com/en-us/azure/active-directory/fundamentals/active-directory-gdpr)
* [Okta Rules Documentation](https://auth0.com/docs/rules)
* [PyTorch Unlearning (Captum)](https://pytorch.org/blog/introducing-captum/)
