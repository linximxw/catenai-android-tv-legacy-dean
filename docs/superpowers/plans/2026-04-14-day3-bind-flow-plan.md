# Day3 Bind Flow Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the smallest end-to-end bind flow for the Android 4.4 project: show the bind page when no token exists, call `/api/device/bind/activate`, persist the returned session, and keep release builds green.

**Architecture:** Keep the Day3 slice narrow. Use a small `PrefsStore` for local session persistence, a focused `AuthRepository` to compose request payloads and persist successful bind responses, and a minimal `BindActivity` that delegates business logic instead of owning it. `SplashActivity` only decides whether to route to bind or stay put.

**Tech Stack:** Java, Android XML, SharedPreferences, OkHttp 3.12, Gson, JUnit 4, build-server verification

---

### Task 1: Local Session Persistence

**Files:**
- Create: `app/src/main/java/com/catenai/hotelos/legacy/data/local/PrefsStore.java`
- Create: `app/src/test/java/com/catenai/hotelos/legacy/data/local/PrefsStoreTest.java`

- [ ] **Step 1: Write the failing test**

Cover:
- save token/hotel/device fields
- read them back
- save/read config hash
- clear session removes token-oriented fields

- [ ] **Step 2: Run test to verify it fails**

Run:
`./gradlew testDebugUnitTest --tests '*PrefsStoreTest' --no-daemon`

Expected:
FAIL because `PrefsStore` does not exist yet

- [ ] **Step 3: Write minimal implementation**

Implement:
- internal key-value adapter for testability
- production constructor backed by `SharedPreferences`
- `readSession()`
- `saveAuthorizedSession(...)`
- `saveConfigHash(...)`
- `clearSession()`
- `hasToken()`

- [ ] **Step 4: Run test to verify it passes**

Run:
`./gradlew testDebugUnitTest --tests '*PrefsStoreTest' --no-daemon`

Expected:
PASS

- [ ] **Step 5: Commit**

Commit:
`git commit -m "feat: add local session store"`

### Task 2: Bind Activation Repository

**Files:**
- Create: `app/src/main/java/com/catenai/hotelos/legacy/data/model/DeviceInfo.java`
- Create: `app/src/main/java/com/catenai/hotelos/legacy/data/model/BindActivateRequest.java`
- Create: `app/src/main/java/com/catenai/hotelos/legacy/data/model/BindActivateResponse.java`
- Create: `app/src/main/java/com/catenai/hotelos/legacy/data/repo/AuthRepository.java`
- Modify: `app/src/main/java/com/catenai/hotelos/legacy/device/DeviceInfoProvider.java`
- Modify: `app/src/main/java/com/catenai/hotelos/legacy/data/net/ApiClient.java`
- Create: `app/src/test/java/com/catenai/hotelos/legacy/data/repo/AuthRepositoryTest.java`

- [ ] **Step 1: Write the failing test**

Cover:
- request contains bind code, fingerprint, and required device info
- `BIND_ACTIVATED` persists token/hotel/device/name
- `ALREADY_ACTIVATED` also persists as success

- [ ] **Step 2: Run test to verify it fails**

Run:
`./gradlew testDebugUnitTest --tests '*AuthRepositoryTest' --no-daemon`

Expected:
FAIL because repository/model classes do not exist yet

- [ ] **Step 3: Write minimal implementation**

Implement:
- request/response DTOs matching the guide
- small repository interface boundary for bind HTTP call
- repository logic that persists successful responses through `PrefsStore`

- [ ] **Step 4: Run test to verify it passes**

Run:
`./gradlew testDebugUnitTest --tests '*AuthRepositoryTest' --no-daemon`

Expected:
PASS

- [ ] **Step 5: Commit**

Commit:
`git commit -m "feat: add bind activation repository"`

### Task 3: Bind UI and Launch Routing

**Files:**
- Create: `app/src/main/java/com/catenai/hotelos/legacy/bind/BindActivity.java`
- Create: `app/src/main/res/layout/activity_bind.xml`
- Modify: `app/src/main/java/com/catenai/hotelos/legacy/SplashActivity.java`
- Modify: `app/src/main/java/com/catenai/hotelos/legacy/App.java`
- Modify: `app/src/main/AndroidManifest.xml`
- Modify: `app/src/main/res/values/strings.xml`
- Optional Modify: `app/src/main/res/values/colors.xml`
- Create: `app/src/test/java/com/catenai/hotelos/legacy/bind/BindCodeValidatorTest.java`
- Create: `app/src/main/java/com/catenai/hotelos/legacy/bind/BindCodeValidator.java`

- [ ] **Step 1: Write the failing test**

Cover:
- six digits is valid
- blank / short / non-digit input is invalid

- [ ] **Step 2: Run test to verify it fails**

Run:
`./gradlew testDebugUnitTest --tests '*BindCodeValidatorTest' --no-daemon`

Expected:
FAIL because validator does not exist yet

- [ ] **Step 3: Write minimal implementation**

Implement:
- validator helper
- minimal bind layout and activity
- button click → validate → background bind call → show status
- splash route: no token => open bind activity and finish

- [ ] **Step 4: Run test to verify it passes**

Run:
`./gradlew testDebugUnitTest --tests '*BindCodeValidatorTest' --no-daemon`

Expected:
PASS

- [ ] **Step 5: Commit**

Commit:
`git commit -m "feat: add bind screen and splash routing"`

### Task 4: Verification and Logging

**Files:**
- Create: `docs/worklogs/2026-04-14-day3-bind-flow-log.md`

- [ ] **Step 1: Run focused Day3 tests**

Run:
`./gradlew testDebugUnitTest --tests '*PrefsStoreTest' --tests '*AuthRepositoryTest' --tests '*BindCodeValidatorTest' --no-daemon`

Expected:
PASS

- [ ] **Step 2: Run release build**

Run:
`./gradlew assembleRelease --no-daemon`

Expected:
PASS and unsigned APK output exists

- [ ] **Step 3: Write today’s work log**

Include:
- what was built
- what was verified
- current phase status
- next step

- [ ] **Step 4: Commit**

Commit:
`git commit -m "docs: add day3 work log"`

