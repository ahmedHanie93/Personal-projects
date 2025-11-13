Perfect — this is a **clean architecture problem**: you want to **branch key retrieval and caching logic by useCaseId**, but **without polluting your service with `if-else` blocks** or duplicated logic. Let’s design this properly.

---

## 🎯 **Goal**

Refactor `KeyServiceImpl` so that:

* Each **`useCaseId`** (e.g. `ECC_SIGNING`, `SIMULATION_ENCRYPTION`, `LOGIN_ENCRYPTION`) has its own cache and key-handling strategy.
* The **current implementation** remains the **default (ECC_SIGNING)** path.
* Code remains **clean, testable, and open for extension but closed for modification** (SOLID principle).

---

## 🧱 **High-Level Design**

We’ll introduce a **Strategy pattern** + **Cache isolation** per use case.

### 1️⃣ Define an Enum for `UseCaseId`

```java
public enum UseCaseId {
    ECC_SIGNING,
    SIMULATION_ENCRYPTION,
    LOGIN_ENCRYPTION,
    UNKNOWN;

    public static UseCaseId fromString(String value) {
        try {
            return UseCaseId.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            return UNKNOWN;
        }
    }
}
```

---

### 2️⃣ Create a Strategy Interface

Each use case will have its own key-handling logic.

```java
public interface KeyHandlerStrategy {
    KeyPairPojo getActiveKeyPair(Set<String> objectKeys) throws Exception;
    void updateCache(KeyPairPojo keyPair);
    String getCacheName();
}
```

---

### 3️⃣ Implement Strategies

#### ✅ Default: ECC_SIGNING (existing logic)

```java
@Component
public class EccSigningKeyHandler implements KeyHandlerStrategy {

    private final CacheManager cacheManager;
    private static final String CACHE_NAME = "ECC_SIGNING_KEYS";

    public EccSigningKeyHandler(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public KeyPairPojo getActiveKeyPair(Set<String> objectKeys) throws Exception {
        // reuse your existing logic
        S3Key newest = new S3Key();
        Set<Map<String, Object>> publicKeys = new HashSet<>();
        S3Key recentExpiredKey = null;

        for (String key : objectKeys) {
            S3Key s3Key = getS3KeyObject(key);
            if (s3Key.isActiveKey()) {
                if (isNewestKey(s3Key, newest)) newest = s3Key;
            } else if (isMostRecentlyExpiredKey(s3Key, recentExpiredKey)) {
                recentExpiredKey = s3Key;
                appendPublicKeys(s3Key, publicKeys);
            }
        }

        if (StringUtils.isBlank(newest.getKeyId()) && recentExpiredKey != null) {
            recentExpiredKey.setExpirationDate(LocalDateTime.now().plusMinutes(getCacheTTL()));
            newest = recentExpiredKey;
        }

        return new KeyPairPojo(
                newest.getPrivateKeyCipherTextBlob(),
                publicKeys,
                newest.getKeyId(),
                newest.getKeyPairSpec()
        );
    }

    @Override
    public void updateCache(KeyPairPojo keyPair) {
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache != null) {
            cache.put("PRIVATE_KEY", keyPair.privateKey());
            cache.put("PUBLIC_KEYS", keyPair.publicKeys());
            cache.put("KEY_ID", keyPair.keyId());
        }
    }

    @Override
    public String getCacheName() {
        return CACHE_NAME;
    }

    // include helper methods like isNewestKey(), appendPublicKeys(), etc.
}
```

#### 🧩 Example for Another Use Case (Simulation Encryption)

```java
@Component
public class SimulationEncryptionKeyHandler implements KeyHandlerStrategy {

    private final CacheManager cacheManager;
    private static final String CACHE_NAME = "SIMULATION_ENCRYPTION_KEYS";

    public SimulationEncryptionKeyHandler(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public KeyPairPojo getActiveKeyPair(Set<String> objectKeys) throws Exception {
        // Custom logic for simulation encryption keys
        // Example: only return RSA keys or certain prefixes
        return filterKeysByAlgorithm(objectKeys, "RSA");
    }

    @Override
    public void updateCache(KeyPairPojo keyPair) {
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache != null) {
            cache.put("PRIVATE_KEY", keyPair.privateKey());
            cache.put("PUBLIC_KEYS", keyPair.publicKeys());
            cache.put("KEY_ID", keyPair.keyId());
        }
    }

    @Override
    public String getCacheName() {
        return CACHE_NAME;
    }

    private KeyPairPojo filterKeysByAlgorithm(Set<String> objectKeys, String algorithm) throws Exception {
        // simplified logic
        for (String key : objectKeys) {
            S3Key s3Key = getS3KeyObject(key);
            if (algorithm.equalsIgnoreCase(s3Key.getAlgorithm())) {
                return new KeyPairPojo(s3Key.getPrivateKeyCipherTextBlob(),
                        Set.of(Map.of("alg", algorithm, "kid", s3Key.getKeyId())),
                        s3Key.getKeyId(), s3Key.getKeyPairSpec());
            }
        }
        throw new IllegalStateException("No matching key found for algorithm " + algorithm);
    }
}
```

---

### 4️⃣ Create a Factory to Select Strategy

```java
@Component
public class KeyHandlerFactory {

    private final Map<UseCaseId, KeyHandlerStrategy> strategyMap;

    public KeyHandlerFactory(List<KeyHandlerStrategy> strategies) {
        this.strategyMap = strategies.stream()
            .collect(Collectors.toMap(
                s -> UseCaseId.fromString(s.getCacheName().replace("_KEYS", "")),
                s -> s
            ));
    }

    public KeyHandlerStrategy getHandler(UseCaseId useCaseId) {
        return strategyMap.getOrDefault(useCaseId, strategyMap.get(UseCaseId.ECC_SIGNING));
    }
}
```

---

### 5️⃣ Refactor `KeyServiceImpl`

Now the core service stays clean — no if/else branching.

```java
@Service
public class KeyServiceImpl implements KeyService {

    private final KeyHandlerFactory handlerFactory;
    private final S3Service s3Service;

    public KeyServiceImpl(KeyHandlerFactory handlerFactory, S3Service s3Service) {
        this.handlerFactory = handlerFactory;
        this.s3Service = s3Service;
    }

    @Override
    public KeyPairPojo getActiveKeyPair(Set<String> objectKeys) throws Exception {
        // Read useCaseId from S3Key metadata
        Optional<String> useCaseValue = objectKeys.stream()
                .map(key -> {
                    try {
                        return s3Service.createS3Key(key).getUseCaseId();
                    } catch (IOException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .findFirst();

        UseCaseId useCaseId = UseCaseId.fromString(useCaseValue.orElse("ECC_SIGNING"));
        KeyHandlerStrategy handler = handlerFactory.getHandler(useCaseId);
        return handler.getActiveKeyPair(objectKeys);
    }
}
```

---

## 🧠 Summary of Benefits

| Benefit                 | Description                                                                        |
| ----------------------- | ---------------------------------------------------------------------------------- |
| **Clean Separation**    | Each use case encapsulates its own logic and cache handling.                       |
| **Extensible**          | Adding new use cases (like `DATA_EXPORT_ENCRYPTION`) only requires a new strategy. |
| **Backward Compatible** | Default (`ECC_SIGNING`) logic remains intact for existing keys.                    |
| **Testable**            | Each handler can be unit-tested independently.                                     |

---

Would you like me to include the refactored **`getCachedKey()`** and **`updateCache()`** methods that support this per-useCase caching too?
