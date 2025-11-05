Absolutely — here’s a concise **RFC** that reflects this mature, alias-driven evolution and the key clarifications you’ve made.

---

# 🧾 **RFC: Alias-Based Key Management and Retrieval**

## **1. Context & Problem**

Currently, keys are retrieved and cached based on **algorithm type (ECC)** from S3. As we introduce **RSA keys** (for encryption) and potentially future algorithms, the system risks complexity creep:

* Code branches by algorithm
* Multiple endpoints or query params
* Harder backward compatibility management

We need a **future-proof model** that abstracts *how* keys are implemented and focuses on *why* they’re used.

---

## **2. Proposed Solution: Alias-Centric Model**

### **Concept**

Each **alias** represents a *business or functional use case* (e.g. `signing`, `partner-encryption`, `embedded-banking`).
Aliases map to one or more underlying keys in S3, independent of the algorithm.

| Alias              | Algorithm | Use | Active Private | Active Public   | Notes                   |
| ------------------ | --------- | --- | -------------- | --------------- | ----------------------- |
| signing            | ES256     | sig | ✅              | ✅ (n-1 overlap) | For signing tokens      |
| partner-encryption | RSA-OAEP  | enc | ✅              | ✅ (n-1 overlap) | For encrypting payloads |

---

## **3. Design Overview**

### **S3 Object Schema**

All keys remain in the **same bucket**, differentiated by prefix and metadata.

```
s3://key-bucket/
 ├── ecc/keys/key-2025-01.json
 ├── rsa/keys/key-2025-02.json
```

Each object includes alias metadata (added on generation):

```json
{
  "keyId": "key-2025-01",
  "algorithm": "ES256",
  "alias": "signing",
  "use": "sig",
  "created": "2025-10-01T00:00:00Z"
}
```

> **Backward compatibility:** Old keys without `alias` default to inferred alias (`signing`).

---

## **4. Caching & Key Selection**

### **Cache Model**

Separate caches for public and private keys using Caffeine:

```java
private final Cache<String, KeyMaterial> privateKeyCache; // key = alias
private final Cache<String, List<KeyMaterial>> publicKeyCache; // key = alias
```

* **Private key:** Exactly one *active* per alias
* **Public keys:** Up to 2 during rotation overlap

### **Key Resolution Logic**

```java
public KeyMaterial getKeyByAlias(String alias, boolean includePublicOnly) {
    if (includePublicOnly) return publicKeyCache.getIfPresent(alias);
    return privateKeyCache.getIfPresent(alias);
}
```

---

## **5. REST Endpoints**

### **Public Keys (JWKS-style)**

```
GET /.well-known/keys?alias={aliasId}
```

**Behavior:**

* Returns all public keys for alias
* If no alias provided → returns all aliases (OIDC-style JWKS)

**Example Response**

```json
{
  "keys": [
    {
      "kid": "ecc-2025-01",
      "alg": "ES256",
      "use": "sig",
      "alias": "signing",
      "kty": "EC",
      "crv": "P-256",
      "x": "base64url",
      "y": "base64url"
    }
  ]
}
```

### **Private Key Retrieval**

```
GET /internal/keys/private?alias={aliasId}
```

* Requires internal service authentication
* Returns only **one** active private key for alias

---

## **6. Backward Compatibility**

| Area                   | Strategy                                                 |
| ---------------------- | -------------------------------------------------------- |
| **Existing ECC logic** | Defaults alias=`signing`                                 |
| **Existing endpoint**  | Continues to return ECC keys only                        |
| **Clients**            | Gradually migrate to alias-based usage                   |
| **S3 objects**         | New objects include `alias`; old ones handled gracefully |

---

## **7. Implementation Notes**

* Alias registry may be externalized later (e.g. YAML or DB)
* Alias is immutable once assigned
* Rotation logic operates per alias
* Metric tagging (cache hits, alias usage, rotation lag)

---

## **8. Benefits**

| Aspect                 | Improvement                                         |
| ---------------------- | --------------------------------------------------- |
| **Extensibility**      | Add new algorithms with no endpoint or cache change |
| **Security**           | Key isolation per use case (no reuse)               |
| **Maintainability**    | Simplified key resolution logic                     |
| **Business alignment** | Keys represent *intent*, not *crypto detail*        |

---

## **9. Next Steps**

**Phase 1 (Now)**

* Extend S3 schema to include `alias`
* Implement alias filtering in cache and service logic
* Update `/keys` endpoint to support `alias` param

**Phase 2 (Later)**

* Introduce alias registry with access control
* Deprecate algorithm-based endpoints

---
Perfect — here’s a **production-ready, concise implementation** excerpt for `KeyServiceImpl` aligned with the alias-centric RFC above.
It focuses on clarity, correctness, and backward compatibility.

---

# 🧩 **Code Snippets: Alias-Based Key Handling**

## **1. KeyServiceImpl Core Logic**

```java
@Service
@RequiredArgsConstructor
public class KeyServiceImpl implements KeyService {

    private final S3KeyProvider s3KeyProvider;
    private final Cache<String, KeyMaterial> privateKeyCache;          // alias -> private key
    private final Cache<String, List<KeyMaterial>> publicKeyCache;     // alias -> list of public keys

    @PostConstruct
    public void init() {
        loadKeysFromS3();
    }

    @Scheduled(fixedDelayString = "${keys.refresh.interval.ms:600000}")
    public void refreshKeys() {
        loadKeysFromS3();
    }

    @Override
    public KeyMaterial getPrivateKeyByAlias(String alias) {
        return privateKeyCache.getIfPresent(alias);
    }

    @Override
    public List<KeyMaterial> getPublicKeysByAlias(String alias) {
        if (alias == null) { // Return all
            return publicKeyCache.asMap().values()
                                 .stream()
                                 .flatMap(List::stream)
                                 .collect(Collectors.toList());
        }
        return publicKeyCache.getIfPresent(alias);
    }

    private void loadKeysFromS3() {
        List<KeyMaterial> allKeys = s3KeyProvider.fetchAllKeys();

        Map<String, List<KeyMaterial>> groupedByAlias = allKeys.stream()
                .collect(Collectors.groupingBy(k -> Optional.ofNullable(k.getAlias()).orElse("signing")));

        groupedByAlias.forEach((alias, keys) -> {
            List<KeyMaterial> sorted = keys.stream()
                    .sorted(Comparator.comparing(KeyMaterial::getCreated).reversed())
                    .collect(Collectors.toList());

            // 1 active private key
            privateKeyCache.put(alias, sorted.get(0));

            // 2 active public keys (rotation overlap)
            publicKeyCache.put(alias, sorted.stream()
                                            .filter(KeyMaterial::isPublic)
                                            .limit(2)
                                            .collect(Collectors.toList()));
        });
    }
}
```

---

## **2. S3KeyProvider Example**

Handles both RSA and ECC keys, with alias-awareness and backward compatibility.

```java
@Component
@RequiredArgsConstructor
public class S3KeyProvider {

    private final AmazonS3 s3Client;
    @Value("${keys.bucket.name}") private String bucketName;

    public List<KeyMaterial> fetchAllKeys() {
        ListObjectsV2Result result = s3Client.listObjectsV2(bucketName);
        List<S3ObjectSummary> objects = result.getObjectSummaries();

        return objects.stream()
                .filter(o -> o.getKey().endsWith(".json"))
                .map(this::readKeyObject)
                .collect(Collectors.toList());
    }

    private KeyMaterial readKeyObject(S3ObjectSummary summary) {
        S3Object object = s3Client.getObject(summary.getBucketName(), summary.getKey());
        try (InputStream in = object.getObjectContent()) {
            ObjectMapper mapper = new ObjectMapper();
            KeyMaterial key = mapper.readValue(in, KeyMaterial.class);

            // Backward compatibility: derive alias if missing
            if (key.getAlias() == null) {
                key.setAlias(summary.getKey().contains("rsa") ? "partner-encryption" : "signing");
            }

            return key;
        } catch (IOException e) {
            throw new KeyLoadException("Failed to load key: " + summary.getKey(), e);
        }
    }
}
```

---

## **3. KeyMaterial POJO**

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KeyMaterial {
    private String keyId;
    private String algorithm;
    private String alias;
    private String use;         // sig | enc
    private String publicKey;
    private String privateKey;  // null for public-only entries
    private boolean isPublic;
    private Instant created;
}
```

---

## **4. REST Endpoints**

### **Public Keys Endpoint (JWKS style)**

```java
@RestController
@RequestMapping("/.well-known")
@RequiredArgsConstructor
public class PublicKeyController {

    private final KeyService keyService;

    @GetMapping("/keys")
    public Map<String, Object> getPublicKeys(@RequestParam(required = false) String alias) {
        List<KeyMaterial> keys = keyService.getPublicKeysByAlias(alias);

        List<Map<String, Object>> jwks = keys.stream()
            .map(this::toJwk)
            .collect(Collectors.toList());

        return Map.of("keys", jwks);
    }

    private Map<String, Object> toJwk(KeyMaterial key) {
        return Map.of(
            "kid", key.getKeyId(),
            "kty", key.getAlgorithm().startsWith("ES") ? "EC" : "RSA",
            "alg", key.getAlgorithm(),
            "use", key.getUse(),
            "alias", key.getAlias(),
            "n", key.getPublicKey(), // RSA modulus
            "e", "AQAB"              // RSA exponent (example)
        );
    }
}
```

---

### **Private Key Endpoint (Internal Use)**

```java
@RestController
@RequestMapping("/internal/keys")
@RequiredArgsConstructor
@PreAuthorize("hasRole('INTERNAL_SERVICE')")
public class PrivateKeyController {

    private final KeyService keyService;

    @GetMapping("/private")
    public ResponseEntity<KeyMaterial> getPrivateKey(@RequestParam String alias) {
        KeyMaterial key = keyService.getPrivateKeyByAlias(alias);
        if (key == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(key);
    }
}
```

---

## ✅ **Advantages of This Implementation**

| Category                 | Benefit                                                |
| ------------------------ | ------------------------------------------------------ |
| **Backwards compatible** | Old ECC keys still work; alias auto-derived            |
| **Alias-based access**   | Simple, business-driven use case mapping               |
| **Clear separation**     | Private/public caches isolated per alias               |
| **Future-proof**         | Supports ECC, RSA, EdDSA, etc. without endpoint change |
| **Rotation-friendly**    | Public keys overlap allowed, private is single active  |

---

## 🧩 **Next Steps**

1. Add alias metadata in S3 on key generation.
2. Update rotation jobs to rotate **per alias**.
3. Add alias validation layer for internal access control (Phase 2).

---
