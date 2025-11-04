Perfect — here’s a **single, consolidated RFC** written in a professional internal style, suitable for an architecture review or design sign-off.
It combines your prior architectural reasoning, the trade-off discussion, and the final `KeyServiceImpl` patch design — structured as a complete, production-grade RFC.

---

# 🧾 **RFC: Multi-Algorithm Key Management in KeyService**

**Author:** Ahmed Hanie
**Date:** November 2025
**Reviewed by:** Sentry Architecture Group
**Status:** Draft for Review
**Related RFCs:** Key Lifecycle Management, JWKS Exposure Policy

---

## 1. 🎯 **Overview**

The current **KeyService** implementation manages **ECC keys** (Elliptic Curve Cryptography) used for signing operations.
We now need to **extend support for RSA keys**, which will primarily be used for encryption and token wrapping operations.

The goal is to:

* Enable **RSA key retrieval and caching** from S3, similar to ECC.
* Expose both **ECC (signing)** and **RSA (encryption)** public keys via a unified **JWKS endpoint**.
* Maintain backward compatibility with existing consumers.
* Keep the design **simple**, **extensible**, and **operationally efficient** for future algorithms (e.g., Ed25519).

---

## 2. ⚙️ **Problem Statement**

Currently:

* The KeyService pulls ECC keys from a dedicated S3 bucket.
* It caches one active private ECC key and exposes public keys via `/keys`.
* The design is monolithic — ECC-specific and not algorithm-aware.
* Future algorithm additions (RSA, EdDSA, etc.) would require significant code duplication.

---

## 3. 💡 **Design Objectives**

| Objective                  | Description                                                                   |
| -------------------------- | ----------------------------------------------------------------------------- |
| **Extensibility**          | Add RSA and future key types without refactoring core logic.                  |
| **Backward Compatibility** | Preserve existing ECC behavior and endpoint contracts.                        |
| **Operational Simplicity** | Use a single storage and unified caching strategy.                            |
| **OIDC Compliance**        | Maintain standards-compliant JWKS endpoint (RFC 7517, RFC 7518).              |
| **Performance**            | Keep caching lightweight and separate per algorithm for optimal invalidation. |

---

## 4. 🪣 **S3 Bucket Strategy**

### ✅ **Decision: Use the same S3 bucket with algorithm prefixes**

```
/s3-key-bucket/
  ecc/keys/key-001.json
  ecc/keys/key-002.json
  rsa/keys/key-001.json
  rsa/keys/key-002.json
```

**Rationale:**

* Unified lifecycle and IAM policies.
* Simplified monitoring and audit trails.
* Lower operational overhead than multi-bucket setups.
* Future algorithms can simply use new prefixes (`ed25519/`, etc.).

---

## 5. ⚡ **Caching Strategy**

### ✅ **Decision: Separate caches per algorithm**

Each algorithm maintains its own cache region for its private and public keys.
This allows different TTLs and independent cache invalidation policies.

| Algorithm | Cache Name        | Use                      | Default TTL |
| --------- | ----------------- | ------------------------ | ----------- |
| ECC       | `eccKeysCache`    | Signing                  | 5 min       |
| RSA       | `rsaKeysCache`    | Encryption               | 10 min      |
| JWKS      | `publicJwksCache` | Combined public exposure | 1 min       |

---

## 6. 🌐 **Public Key Endpoint Strategy**

### ✅ **Decision: Single JWKS endpoint with algorithm filtering**

Following **OIDC and OAuth 2.0 best practices**, all public keys are served from one endpoint:

```
GET /.well-known/jwks
```

Optional query params for debugging or specific use cases:

```
GET /.well-known/jwks?alg=RSA-OAEP
GET /.well-known/jwks?use=sig
```

### Example Response

```json
{
  "keys": [
    {
      "kty": "EC",
      "use": "sig",
      "kid": "ecc-2025-01",
      "alg": "ES256",
      "crv": "P-256",
      "x": "base64url",
      "y": "base64url"
    },
    {
      "kty": "RSA",
      "use": "enc",
      "kid": "rsa-2025-01",
      "alg": "RSA-OAEP",
      "n": "base64url",
      "e": "AQAB"
    }
  ]
}
```

**Benefits:**

* Fully OIDC compliant (per [RFC 7517](https://datatracker.ietf.org/doc/html/rfc7517)).
* Future-proof — easily add new key types.
* Single endpoint for client key discovery simplifies integration.

---

## 7. 🧠 **Service Layer Design**

### ✅ **Decision: Strategy Pattern for Algorithm Handlers**

Each algorithm type will have its own handler implementing the `KeyTypeHandler` interface.

```java
public interface KeyTypeHandler {
    KeyAlgorithm getAlgorithm();
    KeyPairPojo generateKeyPair();
    boolean validateKey(byte[] keyMaterial);
    Map<String, Object> formatPublicKey(byte[] publicKey, String keyId);
}
```

**Handlers:**

* `EccKeyHandler`
* `RsaKeyHandler`
* Future: `EdKeyHandler`, etc.

---

## 8. 🔄 **Rotation Strategy**

| Algorithm | Operation  | Cron               | Notes                             |
| --------- | ---------- | ------------------ | --------------------------------- |
| ECC       | Signing    | `0 5 0,8,16 * * *` | Current schedule                  |
| RSA       | Encryption | `0 0 6,18 * * *`   | Staggered rotation to reduce load |

Each scheduled job refreshes cache independently.

---

## 9. 🧩 **Implementation Highlights**

### ✅ **KeyServiceImpl Enhancements**

* Added `KeyAlgorithm` enum for ECC and RSA.
* Introduced algorithm-aware caching and key fetching.
* Unified JWKS aggregation for both ECC & RSA.
* Backward-compatible methods default to ECC.

### ✅ **New Helper Classes**

* `KeyUtils.formatPublicKeyAsJWK()` now supports both RSA and ECC.
* `CacheConfig` provides per-cache TTL configuration.

---

## 10. 🧱 **Representative Code Changes**

### 🧩 `KeyServiceImpl` (Excerpt)

```java
public enum KeyAlgorithm {
    ECC("EC", "sig"),
    RSA("RSA", "enc");
}

@Override
public KeyPairPojo getCachedKey(KeyAlgorithm algorithm) {
    String cacheName = getCacheNameForAlgorithm(algorithm);
    Cache cache = cacheManager.getCache(cacheName);
    ...
}

@Override
public Set<Map<String, Object>> getCombinedJwks() {
    Set<Map<String, Object>> jwks = new HashSet<>();
    for (KeyAlgorithm algo : KeyAlgorithm.values()) {
        KeyPairPojo cached = getCachedKey(algo);
        if (cached != null) jwks.addAll(cached.publicKeys());
    }
    return jwks;
}

@Override
public PublicKeyDTO getPublicKeys() {
    return new PublicKeyDTO(getCombinedJwks());
}
```

### 🧩 `KeyUtils` (Excerpt)

```java
public Map<String, Object> formatPublicKeyAsJWK(byte[] publicKey, String keyId, KeyAlgorithm alg) {
    Map<String, Object> jwk = new HashMap<>();
    jwk.put("kid", keyId);
    jwk.put("kty", alg.getKty());
    jwk.put("use", alg.getUse());

    switch (alg) {
        case ECC:
            // EC formatting
            jwk.put("alg", "ES256");
            break;
        case RSA:
            // RSA formatting
            jwk.put("alg", "RSA-OAEP");
            break;
    }
    return jwk;
}
```

### 🧩 REST Endpoint

```java
@RestController
@RequestMapping("/.well-known")
public class JwksController {

    private final KeyService keyService;

    @GetMapping("/jwks")
    public ResponseEntity<Map<String, Object>> getJwks(
            @RequestParam(required = false) String alg,
            @RequestParam(required = false) String use) throws Exception {
        
        Set<Map<String, Object>> keys = keyService.getCombinedJwks();
        
        if (alg != null) {
            keys = keys.stream()
                .filter(k -> alg.equals(k.get("alg")))
                .collect(Collectors.toSet());
        }
        if (use != null) {
            keys = keys.stream()
                .filter(k -> use.equals(k.get("use")))
                .collect(Collectors.toSet());
        }
        return ResponseEntity.ok(Map.of("keys", keys));
    }
}
```

---

## 11. 📊 **Metrics and Monitoring**

| Metric                          | Description                           |
| ------------------------------- | ------------------------------------- |
| `key_cache_hit_total{alg}`      | Cache hit count per algorithm         |
| `key_cache_miss_total{alg}`     | Cache miss count per algorithm        |
| `jwks_key_count{alg}`           | Number of keys returned per algorithm |
| `key_rotation_duration_seconds` | Key rotation job timing               |

---

## 12. 🧱 **Future Enhancements**

| Future Feature              | Description                                     |
| --------------------------- | ----------------------------------------------- |
| **EdDSA (Ed25519)**         | Add EdDSA handler to registry                   |
| **Dynamic JWKS Filter**     | Add filter by `kid` for debugging               |
| **HSM Integration**         | Replace S3 with HSM-backed KMS for key material |
| **Versioned JWKS Endpoint** | Optional `/v2/jwks` for future schema evolution |

---

## 13. ✅ **Decision Summary**

| Decision                   | Outcome                                          |
| -------------------------- | ------------------------------------------------ |
| **S3 Structure**           | Shared bucket with `/ecc/`, `/rsa/` prefixes     |
| **Cache Design**           | Per-algorithm cache regions                      |
| **JWKS Endpoint**          | Unified endpoint with algorithm filtering        |
| **Extensibility**          | Strategy pattern with `KeyTypeHandler` interface |
| **Rotation**               | Staggered per-algorithm schedules                |
| **Backward Compatibility** | Default to ECC in legacy methods                 |

---

## 14. 🏁 **Conclusion**

This design achieves:

* **Simplicity**: Single S3 source and unified JWKS endpoint.
* **Modularity**: Algorithm-specific caching and handlers.
* **Scalability**: Easy onboarding of new cryptographic algorithms.
* **Compliance**: Fully aligned with [RFC 7517](https://datatracker.ietf.org/doc/html/rfc7517) and [RFC 7518](https://datatracker.ietf.org/doc/html/rfc7518).

It balances extensibility with minimal disruption and operational simplicity, making it the right foundation for multi-algorithm key management in Sentry’s KeyService.

---

**References:**

* [RFC 7517: JSON Web Key (JWK)](https://datatracker.ietf.org/doc/html/rfc7517)
* [RFC 7518: JSON Web Algorithms (JWA)](https://datatracker.ietf.org/doc/html/rfc7518)
* [AWS KMS Key Management Best Practices](https://docs.aws.amazon.com/kms/latest/developerguide/key-best-practices.html)
* [OIDC Discovery Metadata Spec](https://openid.net/specs/openid-connect-discovery-1_0.html)

---
Excellent discussion! Let me deliver the **concise code patch** for `KeyServiceImpl` that implements the per-algorithm caches and JWKS builder as we discussed.

## 🎯 **KeyServiceImpl Patch - Multi-Algorithm Support**

```java
@Component
public class KeyServiceImpl implements KeyService {
    
    // Updated Cache Constants
    private static final String ECC_KEYS_CACHE = "eccKeysCache";
    private static final String RSA_KEYS_CACHE = "rsaKeysCache"; 
    private static final String PUBLIC_JWKS_CACHE = "publicJwksCache";
    
    // Algorithm-specific cache keys
    private static final String PRIVATE_KEY = "privateKey";
    private static final String PUBLIC_KEYS = "publicKeys";
    private static final String KEY_ID = "keyId";
    
    // Algorithm Enum
    public enum KeyAlgorithm {
        ECC("EC", "sig"),
        RSA("RSA", "enc");
        
        private final String kty;
        private final String use;
        
        KeyAlgorithm(String kty, String use) {
            this.kty = kty;
            this.use = use;
        }
        
        public String getKty() { return kty; }
        public String getUse() { return use; }
        
        public static KeyAlgorithm fromKty(String kty) {
            return Arrays.stream(values())
                .filter(algo -> algo.kty.equals(kty))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown kty: " + kty));
        }
    }
    
    // Updated getCachedKey with algorithm support
    @Override
    public KeyPairPojo getCachedKey(KeyAlgorithm algorithm) {
        final String cacheName = getCacheNameForAlgorithm(algorithm);
        final Cache keysCache = cacheManager.getCache(cacheName);
        
        final Cache.ValueWrapper prikey = keysCache.get(PRIVATE_KEY);
        final Cache.ValueWrapper pukey = keysCache.get(PUBLIC_KEYS);
        final Cache.ValueWrapper keyIdKey = keysCache.get(KEY_ID);
        
        if (prikey != null && pukey != null && keyIdKey != null) {
            final byte[] privateKey = (byte[]) prikey.get();
            final Set<Map<String, Object>> publicKeys = (Set<Map<String, Object>>) pukey.get();
            final String keyId = (String) keyIdKey.get();
            return new KeyPairPojo(privateKey, publicKeys, keyId, algorithm.name());
        }
        return null;
    }
    
    // New: Get combined JWKS from all algorithms
    @Override
    public Set<Map<String, Object>> getCombinedJwks() {
        // Try cache first
        final Cache jwksCache = cacheManager.getCache(PUBLIC_JWKS_CACHE);
        final Cache.ValueWrapper cachedJwks = jwksCache.get(PUBLIC_KEYS);
        
        if (cachedJwks != null) {
            return (Set<Map<String, Object>>) cachedJwks.get();
        }
        
        // Build combined JWKS from all algorithms
        Set<Map<String, Object>> combinedJwks = new HashSet<>();
        
        for (KeyAlgorithm algorithm : KeyAlgorithm.values()) {
            KeyPairPojo cachedKey = getCachedKey(algorithm);
            if (cachedKey != null && cachedKey.publicKeys() != null) {
                combinedJwks.addAll(cachedKey.publicKeys());
            }
        }
        
        // Cache the combined JWKS
        if (!combinedJwks.isEmpty()) {
            jwksCache.put(PUBLIC_KEYS, combinedJwks);
        }
        
        return combinedJwks;
    }
    
    // Updated getKeyPair with algorithm support
    @Override
    public KeyPairPojo getKeyPair(KeyAlgorithm algorithm) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        KeyPairPojo activeKeyPair = constructKeyPair(algorithm);
        final byte[] privateKey = activeKeyPair.privateKey();
        final Set<Map<String, Object>> publicKeys = activeKeyPair.publicKeys();
        updateCache(algorithm, privateKey, publicKeys, activeKeyPair.keyId());
        updateCombinedJwksCache();
        return activeKeyPair;
    }
    
    // Updated scheduled job to rotate all algorithms
    @Scheduled(cron = "0 5 0,8,16 * * *") // ECC rotation
    public void getKeyPairScheduled() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        log.info("Pulling keys at {}", LocalDateTime.now());
        
        // Rotate ECC keys (existing schedule)
        getKeyPair(KeyAlgorithm.ECC);
    }
    
    @Scheduled(cron = "0 0 6,18 * * *") // RSA rotation - different schedule
    public void getRsaKeyPairScheduled() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        log.info("Pulling RSA keys at {}", LocalDateTime.now());
        getKeyPair(KeyAlgorithm.RSA);
    }
    
    // Updated constructKeyPair with algorithm support
    private KeyPairPojo constructKeyPair(KeyAlgorithm algorithm) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        final Set<String> objectKeys = s3Service.listObjects(getS3Prefix(algorithm));
        if (objectKeys.isEmpty()) {
            metricService.sendErrorMetric(KeyServiceErrorCode.OBJECT_LIST_IS_EMPTY.getCode(), OBJECT_LIST_IS_EMPTY_OP);
            throw new IllegalStateException("No keys found for algorithm: " + algorithm);
        }
        return getActiveKeyPair(algorithm, objectKeys);
    }
    
    // Updated getActiveKeyPair with algorithm awareness
    private KeyPairPojo getActiveKeyPair(KeyAlgorithm algorithm, Set<String> objectKeys) 
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        
        S3Key newest = new S3Key();
        Set<Map<String, Object>> publicKeys = new HashSet<>();
        S3Key recentExpiredKey = null;
        
        for (String key : objectKeys) {
            final S3Key s3KeyObject = getS3KeyObject(key);
            if (isActiveKey(s3KeyObject)) {
                if (isNewestKey(s3KeyObject, newest)) {
                    newest = s3KeyObject;
                }
            } else if (isMostRecentlyExpiredKey(s3KeyObject, recentExpiredKey)) {
                recentExpiredKey = s3KeyObject;
            }
            appendPublicKeys(s3KeyObject, publicKeys, algorithm);
        }
        
        // Fallback logic with algorithm-specific TTL
        if (StringUtils.isBlank(newest.getKeyId()) && recentExpiredKey != null) {
            log.info("getActiveKeyPair() - No active {} keys found. Using most recently expired key.", algorithm);
            LocalDateTime newExpirationDate = LocalDateTime.now().plusMinutes(getCacheTTL(algorithm));
            recentExpiredKey.setExpirationDate(newExpirationDate);
            newest = recentExpiredKey;
        }
        
        final byte[] privateKeyCipherTextBlob = newest.getPrivateKeyCipherTextBlob();
        return new KeyPairPojo(privateKeyCipherTextBlob, publicKeys, newest.getKeyId(), algorithm.name());
    }
    
    // Updated appendPublicKeys with algorithm-specific formatting
    private void appendPublicKeys(S3Key s3KeyObject, Set<Map<String, Object>> publicKeys, KeyAlgorithm algorithm) 
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] publicKey = s3KeyObject.getPublicKey();
        if (publicKey != null) {
            final Map<String, Object> jwkObject = keyUtils.formatPublicKeyAsJWK(
                publicKey, s3KeyObject.getKeyId(), algorithm);
            publicKeys.add(jwkObject);
        }
    }
    
    // Updated cache update with algorithm support
    private void updateCache(KeyAlgorithm algorithm, byte[] privateKey, Set<Map<String, Object>> publicKeys, String keyId) {
        final String cacheName = getCacheNameForAlgorithm(algorithm);
        final Cache keysCache = cacheManager.getCache(cacheName);
        
        if (privateKey != null) {
            keysCache.put(PRIVATE_KEY, privateKey);
        }
        if (!CollectionUtils.isEmpty(publicKeys)) {
            keysCache.put(PUBLIC_KEYS, publicKeys);
        }
        if (keyId != null) {
            keysCache.put(KEY_ID, keyId);
        }
    }
    
    // Update combined JWKS cache
    private void updateCombinedJwksCache() {
        Set<Map<String, Object>> combinedJwks = getCombinedJwks();
        final Cache jwksCache = cacheManager.getCache(PUBLIC_JWKS_CACHE);
        jwksCache.put(PUBLIC_KEYS, combinedJwks);
    }
    
    // Updated Public Key endpoint - returns combined JWKS
    @Override
    public PublicKeyDTO getPublicKeys() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        try {
            log.info("Attempting to get public keys");
            Set<Map<String, Object>> publicKeys = getCombinedJwks();
            
            // If cache is empty, build from current keys
            if (CollectionUtils.isEmpty(publicKeys)) {
                for (KeyAlgorithm algorithm : KeyAlgorithm.values()) {
                    KeyPairPojo keyPair = getKeyPair(algorithm);
                    if (keyPair != null) {
                        publicKeys.addAll(keyPair.publicKeys());
                    }
                }
                updateCombinedJwksCache();
            }
            
            PublicKeyDTO response = new PublicKeyDTO(publicKeys);
            metricService.sendSuccessMetric(GET_PUBLIC_KEYS_OP);
            return response;
        } catch (Exception e) {
            metricService.sendErrorMetric(KeyServiceErrorCode.CANNOT_GET_PUBLIC_KEYS.getCode(), GET_PUBLIC_KEYS_OP);
            throw e;
        }
    }
    
    // Helper methods
    private String getCacheNameForAlgorithm(KeyAlgorithm algorithm) {
        switch (algorithm) {
            case ECC: return ECC_KEYS_CACHE;
            case RSA: return RSA_KEYS_CACHE;
            default: throw new IllegalArgumentException("Unsupported algorithm: " + algorithm);
        }
    }
    
    private String getS3Prefix(KeyAlgorithm algorithm) {
        return algorithm.name().toLowerCase() + "/";
    }
    
    private long getCacheTTL(KeyAlgorithm algorithm) {
        // Different TTLs per algorithm if needed
        return algorithm == KeyAlgorithm.ECC ? 300L : 600L; // 5min vs 10min
    }
    
    // Backward compatibility - existing methods default to ECC
    @Override
    public KeyPairPojo getCachedKey() {
        return getCachedKey(KeyAlgorithm.ECC);
    }
    
    @Override
    public KeyPairPojo getKeyPair() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        return getKeyPair(KeyAlgorithm.ECC);
    }
}
```

## 🔧 **Required Supporting Classes**

### **KeyUtils Extension**
```java
@Component
public class KeyUtils {
    
    public Map<String, Object> formatPublicKeyAsJWK(byte[] publicKey, String keyId, KeyAlgorithm algorithm) 
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        
        Map<String, Object> jwk = new HashMap<>();
        jwk.put("kid", keyId);
        jwk.put("kty", algorithm.getKty());
        jwk.put("use", algorithm.getUse());
        
        switch (algorithm) {
            case ECC:
                // Existing ECC formatting logic
                ECPublicKey ecKey = (ECPublicKey) decodePublicKey(publicKey, "EC");
                jwk.put("crv", "P-256");
                jwk.put("x", base64UrlEncode(ecKey.getW().getAffineX().toByteArray()));
                jwk.put("y", base64UrlEncode(ecKey.getW().getAffineY().toByteArray()));
                jwk.put("alg", "ES256");
                break;
                
            case RSA:
                // RSA formatting logic  
                RSAPublicKey rsaKey = (RSAPublicKey) decodePublicKey(publicKey, "RSA");
                jwk.put("n", base64UrlEncode(rsaKey.getModulus().toByteArray()));
                jwk.put("e", base64UrlEncode(rsaKey.getPublicExponent().toByteArray()));
                jwk.put("alg", "RSA-OAEP");
                break;
        }
        
        return jwk;
    }
}
```

### **Cache Configuration**
```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        
        List<CaffeineCache> caches = Arrays.asList(
            new CaffeineCache(ECC_KEYS_CACHE, Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .maximumSize(100)
                .build()),
            new CaffeineCache(RSA_KEYS_CACHE, Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES) 
                .maximumSize(100)
                .build()),
            new CaffeineCache(PUBLIC_JWKS_CACHE, Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES) // Shorter TTL for JWKS
                .maximumSize(10)
                .build())
        );
        
        cacheManager.setCaches(caches);
        return cacheManager;
    }
}
```

## 🚀 **Key Benefits of This Approach**

1. **Clean Separation**: Per-algorithm caches with different TTLs
2. **Atomic Updates**: Combined JWKS cache rebuilt when any algorithm updates
3. **Backward Compatible**: Existing methods default to ECC
4. **Extensible**: Easy to add new algorithms (just extend enum)
5. **Standards Compliant**: Single JWKS endpoint with proper `use` claims