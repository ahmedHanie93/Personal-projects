# **ADR: Representing `simulated_id` in the OAuth2 Authorization Flow**

**Date:** 2025-10-22
**Status:** Proposed
**Authors:** Sentry Architecture Group
**Reviewers:** Identity & Entitlements Working Group
**Context:** Human Identity Service (HIS) Integration

---

## **1. Context**

As part of the Sentry-to-HIS migration, we introduced a **simulation mode** that enables authorized users to act on behalf of another persona for validation or testing.
This requires securely transmitting a `simulated_id` during the `/authorize` flow so downstream services can distinguish between *simulator* and *simulatee* contexts.

Two competing design options emerged:

1. Embed `simulated_id` in the OAuth2 **`scope`** parameter.
2. Send `simulated_id` as a **separate encrypted parameter** alongside standard OAuth2 parameters.

This ADR evaluates both approaches with respect to **security**, **maintainability**, **standards compliance**, and **implementation complexity**.

---

## **2. Options**

### **Option A – `simulated_id` in Scope**

**Example:**

```
scope=openid profile simulate:eyJzaW11bGF0ZWRfaWQiOiAiYWJjMTIzIn0=
```

### **Option B – `simulated_id` as Separate Encrypted Parameter**

**Example:**

```
/authorize?response_type=code
&client_id=abc
&redirect_uri=https://client.example/callback
&scope=openid profile
&simulated_id=eyJzaW11bGF0ZWRfaWQiOiAiYWJjMTIzIn0=
```

---

## **3. Comparison Summary**

| **Dimension**                          | **Option A – In Scope**                                                                                            | **Option B – Separate Parameter**                                                                                    |
| -------------------------------------- | ------------------------------------------------------------------------------------------------------------------ | -------------------------------------------------------------------------------------------------------------------- |
| **Standards Compliance**               | Conforms to RFC6749 §3.3 (scope syntax). Overloading `scope` for non-authorization semantics deviates from intent. | Fully compliant with RFC6749 §4.1.1, which allows unrecognized parameters. Recommended for custom extensions.        |
| **Separation of Concerns**             | Mixes *authorization intent* (what access is requested) with *execution context* (simulation).                     | Clean separation between access scope and context metadata.                                                          |
| **Security**                           | Harder to encrypt/decrypt just one segment of scope. Parsing errors risk partial exposure.                         | Easier to encrypt entire parameter (`AES/GCM`) and handle centrally. Reduces risk of tampering or decoding mistakes. |
| **Implementation Complexity**          | Requires modifying scope parsing logic in multiple components (STS, HIS, Entitlement Adapter).                     | Minimal: add parameter validation and encryption/decryption at gateway.                                              |
| **Downstream Integration**             | Reuses existing Redis cache for scope propagation, but introduces non-semantic tokens.                             | Requires small cache schema update for `simulated_id` passthrough but keeps scopes semantically pure.                |
| **Extensibility**                      | Harder to evolve if additional simulation attributes are needed (e.g., `reason`, `session_type`).                  | Naturally supports future extensibility through namespaced parameters.                                               |
| **Operational Clarity**                | Logs and metrics show opaque scope strings, making troubleshooting harder.                                         | Distinct parameter improves observability and auditing.                                                              |
| **Consistency with Existing Patterns** | Consistent with legacy “alias_id” and “stepup” scopes, though these were exceptions.                               | Aligns with modern OAuth2 extension patterns and identity platform evolution.                                        |

---

## **4. Standards & Security Considerations**

* **RFC 6749 §4.1.1** explicitly states that authorization servers **must ignore unrecognized request parameters**, making the inclusion of `simulated_id` compliant.
* **RFC 6819 (OAuth 2.0 Threat Model)** highlights **query parameter tampering** as a potential attack vector.
  Using **TLS** and **encryption at rest and in transit (AES-GCM)** mitigates man-in-the-middle and replay risks.
* Since `simulated_id` is **AES-encrypted** and **validated server-side**, tampering attempts will be detected through signature verification.
* The **presence of `simulated_id`** automatically infers simulation mode; Sentry does not determine read-only behavior — this remains the **Entitlement Adapter’s responsibility**.

---

## **5. Implementation Notes**

### **For Separate Parameter Approach**

* The `simulated_id` parameter will be AES-GCM encrypted with a per-tenant key.
* During `/authorize`, its presence infers simulation context.
* When forwarded to the Entitlement Adapter, both `simulator_id` and `simulatee_id` are provided; Entitlement determines resulting `scope` (e.g., read-only).
* The parameter is stored ephemerally and never persisted beyond session lifetime.

### **For Scope Approach (Rejected)**

* Would require partial decryption and recomposition of the `scope` string.
* Increases parsing and error-handling complexity across multiple integration points.
* Makes `scope` semantics inconsistent across different request types.

---

## **6. Decision**

We will **send `simulated_id` as a separate encrypted parameter** in the OAuth2 authorization request.

### **Rationale**

* Maintains clear semantic separation between **authorization intent** and **execution context**.
* Complies with **OAuth2 and OIDC extension mechanisms**.
* Simplifies encryption handling and downstream logic.
* Improves observability and reduces coupling with legacy scope-based patterns.

---

## **7. Consequences**

| **Positive**                                | **Negative**                                           |
| ------------------------------------------- | ------------------------------------------------------ |
| Cleaner, standards-aligned contract         | Minor update to cache schema and request validators    |
| Easier debugging and audit visibility       | Requires slight client update to include new parameter |
| Reduced risk of malformed or exposed scopes | Breaks with legacy “alias_id” scope convention         |

---

## **8. Final Recommendation**

Adopt **Option B – Separate Encrypted Parameter (`simulated_id`)**
This approach separates *authorization* from *context*, follows the extension model defined by RFC 6749, and simplifies both security and maintainability.
It also future-proofs the simulation capability for richer use cases (multi-persona testing, hierarchical simulations, etc.) while keeping the Sentry identity contract clean and evolvable.

---

**References:**

* [RFC 6749: The OAuth 2.0 Authorization Framework](https://datatracker.ietf.org/doc/html/rfc6749)
* [RFC 6819: OAuth 2.0 Threat Model and Security Considerations](https://datatracker.ietf.org/doc/html/rfc6819)
* [OpenID Connect Core 1.0](https://openid.net/specs/openid-connect-core-1_0.html)

---