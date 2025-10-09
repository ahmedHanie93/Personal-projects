# **Sentry OIDC `/authorize` API Contract**

## **Endpoint**

```
GET /authorize
```

Initiates the **OIDC Authorization Code Flow** for client applications (registered ForgeRock agents) integrated with the
**Sentry Identity Platform**.
Supports both **standard user authentication** and **admin simulation flows**.

---

## ** Request Parameters**

### **Required Parameters**

| Parameter                 | Required           | Description                                                                                   | Validation                               | Example                                                             |
|---------------------------|--------------------|-----------------------------------------------------------------------------------------------|------------------------------------------|---------------------------------------------------------------------|
| **client_id**             | Yes                | Unique identifier for the client application. Maps to a registered ForgeRock agent in Sentry. | Must match active agent configuration    | `banking-app`                                                       |
| **redirect_uri**          | Yes                | Callback URL to which the response will be sent after authorization.                          | Must exactly match pre-registered URIs   | `https://banking-app.jpmorgan.com/callback`                         |
| **response_type**         | Yes                | OAuth 2.0 response type. Must be `code` for Authorization Code Flow.                          | Only `code` supported                    | `code`                                                              |
| **scope**                 | Yes                | Space-separated list of requested permissions and access contexts.                            | Must be subset of agent's allowed scopes | `openid profile email jpmc:uri:prod:payments:access sentry:aliasId` |
| **state**                 | Yes                | Opaque value maintained by the client for CSRF protection.                                    | Returned unmodified in callback          | `xyz123`                                                            |
| **code_challenge**        | Yes (PKCE clients) | PKCE challenge — base64url-encoded SHA256 hash of code_verifier.                              | Required for all public clients          | `E9Melhoa20wvFrEMTJguCHaoeK18URWbuGJSstw-cM`                        |
| **code_challenge_method** | Yes (PKCE clients) | Method used to derive the code_challenge. Only `S256` supported.                              | Must be `S256`                           | `S256`                                                              |

---

### **Optional & Recommended Parameters**

| Parameter        | Required    | Description                                                                                              | Example               |
|------------------|-------------|----------------------------------------------------------------------------------------------------------|-----------------------|
| **nonce**        | Recommended | OIDC replay protection. Included in ID token.                                                            | `abc456`              |
| **simulated_id** | Optional    | Indicates the request is a **simulation flow**. Represents the Sentry ID of the target (simulatee) user. | `sarah-sentry-id-123` |
| **prompt**       | Optional    | Controls authentication and consent behavior: `none`, `login`, `consent`.                                | `login`               |
| **login_hint**   | Optional    | Suggests which IDA account should authenticate.                                                          | `bobby@jpmc.com`      |
| **max_age**      | Optional    | Maximum allowed age (in seconds) of the current authentication. Forces re-authentication if exceeded.    | `3600`                |
| **acr_values**   | Optional    | Specifies required authentication context class references (e.g., MFA).                                  | `urn:mfa:required`    |

---

## ** Request Processing Logic**

### **1. Client & Request Validation**

* Resolve ForgeRock agent by `client_id`.
* Validate `redirect_uri` matches registered URIs.
* Validate `scope` subset and PKCE parameters.
* Enforce CSRF protection via `state`.

---

### **2. Authentication via IDA**

* Sentry redirects to **IDA** for authentication.
* IDA authenticates the user (admin or standard).
* Sentry extracts the user’s **Sentry ID** from IDA ID Token.

---

### **3. Simulation Flow Inference**

If `simulated_id` is **present**, Sentry infers **simulation mode**:

1. The authenticated user (from IDA) becomes the **simulator**.
2. The `simulated_id` parameter identifies the **simulatee**.
3. Sentry calls the **Entitlement Adapter** to verify simulation rights and determine access boundaries.

Example entitlement call:

```json
{
  "actor": "bobby-sentry-id-321",
  "target": "sarah-sentry-id-123",
  "context": "simulation"
}
```

**Entitlement Adapter Response** (authoritative decision):

```json
{
  "authorized": true,
  "effective_scope": [
    "openid",
    "profile",
    "email",
    "readonly"
  ],
  "entitlements": [
    "view_transactions",
    "read_customer_data"
  ],
  "pii_masking": true
}
```

Sentry:

* Proceeds with the flow **only if `authorized = true`**.
* Does **not inject or enforce `readonly`** — it accepts whatever scope the entitlement layer dictates.

---

### **4. Authorization Code Issue**

On success:

* Sentry issues an authorization code.
* Associates the code with:

    * Client (ForgeRock agent)
    * Authenticated user (simulator or normal)
    * Simulated user
* Redirects to `redirect_uri` with `code` and `state` parameters.

Example redirect:

```http
HTTP/1.1 302 Found
Location: https://client-app.jpmorgan.com/callback?
  code=AUTH_CODE_123XYZ&
  state=xyz123
```

---

## ** Simulation Flow Overview**

### **Behavior**

| Area                     | Description                                                                       |
|--------------------------|-----------------------------------------------------------------------------------|
| **Access Context**       | Simulator acts *as* target user but within restricted, entitlement-enforced scope |
| **Readonly Enforcement** | Applied downstream by Entitlement Adapter (not hardcoded)                         |

---

## ** Example Tokens**

### **Standard Authentication**

```json
{
  "sub": "bobby-sentry-id-321",
  "scope": "openid profile email"
}
```

### **Simulation Mode**

```json
{
  "sub": "bobby-sentry-id-321",
  "simulated": "sarah-sentry-id-123",
  "scope": "openid profile email readonly",
  "entitlements": [
    "view_transactions",
    "read_customer_data"
  ]
}
```

---

## ** Error Responses**

| Error                     | HTTP | Description                                          |
|---------------------------|------|------------------------------------------------------|
| `invalid_request`         | 400  | Missing or malformed parameters                      |
| `invalid_client`          | 400  | Unrecognized or inactive `client_id`                 |
| `invalid_scope`           | 400  | Scope not allowed for agent                          |
| `unauthorized_client`     | 403  | Client not authorized for OIDC flow                  |
| `simulation_unauthorized` | 403  | Authenticated user not authorized to simulate target |
| `target_user_not_found`   | 404  | Provided `simulated_id` not found                    |
| `access_denied`           | 403  | User declined consent or policy restriction          |

Error redirect example:

```http
HTTP/1.1 302 Found
Location: https://client-app/callback?
  error=simulation_unauthorized&
  error_description=User+not+authorized+to+simulate+target&
  state=xyz123
```

---

## ** Example Requests**

### **Standard Authentication**

```http
GET /authorize?
  client_id=banking-app&
  redirect_uri=https://banking-app.jpmorgan.com/callback&
  response_type=code&
  scope=openid%20profile%20email&
  state=xyz123&
  nonce=abc456&
  code_challenge=E9Melhoa20wvFrEMTJguCHaoeK18URWbuGJSstw-cM&
  code_challenge_method=S256
```

### **Admin Simulation**

```http
GET /authorize?
  client_id=admin-support-tool&
  redirect_uri=https://admin-tool.jpmorgan.com/simulation-callback&
  response_type=code&
  scope=openid%20profile%20email&
  state=admin123&
  nonce=sim456&
  code_challenge=F8Nelhoa30wvGrEMTJguCHbpeK19VRXcvGJSstw-dN&
  code_challenge_method=S256&
  simulated_id=sarah-sentry-id-123&
  acr_values=urn:mfa:required
```

---