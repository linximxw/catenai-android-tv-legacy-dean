# Day4 Session Validation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Validate the persisted device token at app startup, keep authorized sessions, and send invalid sessions back to the bind screen.

**Architecture:** Keep Day4 narrow. Add a dedicated session response model and `SessionRepository` for `/api/device/session`, then let `SplashActivity` orchestrate a minimal startup decision: no token -> bind page, invalid session -> clear token and return to bind, active session -> stay on splash.

**Tech Stack:** Java, SharedPreferences, OkHttp 3.12, Gson, JUnit 4, Android XML, build-server verification

---

### Task 1: Session Validation Repository

**Files:**
- Create: `app/src/main/java/com/catenai/hotelos/legacy/data/model/DeviceSessionResponse.java`
- Create: `app/src/main/java/com/catenai/hotelos/legacy/data/repo/SessionRepository.java`
- Create: `app/src/test/java/com/catenai/hotelos/legacy/data/repo/SessionRepositoryTest.java`

- [x] **Step 1: Write the failing test**
- [x] **Step 2: Run test to verify it fails**
- [x] **Step 3: Write minimal implementation**
- [x] **Step 4: Run test to verify it passes**
- [x] **Step 5: Commit**

### Task 2: Startup Session Flow

**Files:**
- Modify: `app/src/main/java/com/catenai/hotelos/legacy/SplashActivity.java`
- Modify: `app/src/main/res/values/strings.xml`

- [x] **Step 1: Add startup checking status**
- [x] **Step 2: Validate token-backed session in background**
- [x] **Step 3: Route invalid sessions back to bind**
- [x] **Step 4: Keep active sessions on splash**
- [x] **Step 5: Commit**

### Task 3: Verification

**Files:**
- Create: `docs/worklogs/2026-04-14-day4-session-validation-log.md`

- [x] **Step 1: Run focused unit test**
- [x] **Step 2: Run combined Day3-Day4 tests**
- [x] **Step 3: Run release build**
- [x] **Step 4: Record work log**

