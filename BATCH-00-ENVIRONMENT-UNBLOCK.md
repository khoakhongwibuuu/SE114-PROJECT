# [ENVIRONMENT UNBLOCK] BATCH-00

## Purpose
This batch exists to unblock runtime verification for CareNest.

Right now, the app has meaningful code-level progress, but true runtime judgment is blocked because the local execution environment is incomplete.

This document defines the exact work needed to make runtime QA possible.

---

## Goal
Provision a runnable local environment that supports:
- backend startup
- database connectivity
- Redis connectivity
- websocket connectivity
- frontend installation on emulator/device
- real execution of the current runtime verification batch

If this batch succeeds, we should be able to execute:
- [BATCH-01-RUNTIME-VERIFICATION.md](D:\DoAn_MB1\CareNest\BATCH-01-RUNTIME-VERIFICATION.md)

---

## Scope

### In Scope
- backend startup readiness
- PostgreSQL availability
- Redis availability
- backend configuration sanity
- local dependency/runtime verification
- Android emulator/device readiness for testing
- websocket reachability checks

### Out of Scope
- fixing unrelated product features
- OCR implementation
- AI Care production intelligence
- new user-facing feature work

---

## Primary Deliverable
At the end of this batch, the tester must be able to answer **Yes** to all of the following:

- Backend running?
- Database connected?
- Redis connected?
- Websocket endpoint reachable?
- App installed on emulator/device?
- Login flow testable?
- Family/community chat runtime testable?

If any answer is still `No`, the batch is not complete.

---

## Execution Plan

### Step 1 — Inspect Local Backend Requirements
Review the backend’s actual runtime dependencies and startup expectations.

Inspect at minimum:
- `backend/pom.xml`
- `backend/src/main/resources/application.yml`
- any `.env`, docker-compose, or local config references
- websocket/security configuration
- DB/Redis host expectations

You must identify:
- required services
- required ports
- required environment variables
- any credentials/defaults needed for local startup

---

### Step 2 — Verify Infrastructure Availability
Check whether the local machine currently has:
- Docker installed and usable
- PostgreSQL running or runnable
- Redis running or runnable

If Docker is used, identify:
- compose file / container names
- startup order
- health/readiness indicators

If Docker is not used, identify:
- how PostgreSQL is expected to be started
- how Redis is expected to be started

---

### Step 3 — Start the Backend Stack
Bring up the services required for backend runtime.

Required success criteria:
- database reachable from backend
- Redis reachable from backend
- backend starts without fatal configuration error

You must capture:
- startup commands used
- ports used
- final running state
- any unresolved failures

---

### Step 4 — Validate Backend Reachability
Confirm the backend is actually usable.

At minimum verify:
- HTTP API base is reachable
- authentication endpoint responds
- websocket endpoint is reachable

If possible, identify exact URLs for:
- REST API base
- websocket base

---

### Step 5 — Verify Frontend Runtime Readiness
Confirm the Android side can actually be tested.

At minimum:
- debug APK builds
- emulator or device is available
- app can be installed
- login can be attempted

If installation is blocked:
- report whether the blocker is permissions, emulator absence, adb issue, or something else

---

### Step 6 — Hand Off to Runtime QA
If environment is ready, explicitly state that:
- Batch 00 is complete
- Batch 01 runtime verification can begin

If not ready, explicitly state:
- exactly what remains blocked
- whether the blocker is local machine infrastructure, credentials, Docker, ports, adb/device, or backend config

---

## Required Evidence

You must provide concrete evidence, not vague summaries.

Acceptable evidence includes:
- exact commands used
- exact service statuses
- exact ports
- exact reachable URLs
- exact startup failure messages
- exact missing dependency names

Unacceptable evidence:
- “backend seems fine”
- “Docker probably not running”
- “runtime still blocked” without explaining why

---

## Required Report Format

### 1. Batch Status
Choose exactly one:
- `Environment ready for runtime QA`
- `Partially unblocked`
- `Blocked`

### 2. Backend Requirements Summary
List:
- database dependency
- Redis dependency
- backend startup method
- websocket dependency

### 3. Infrastructure Findings
State:
- Docker available: `Yes / No / Not needed`
- PostgreSQL available: `Yes / No`
- Redis available: `Yes / No`
- Backend started: `Yes / No`

### 4. Commands Executed
List the exact commands run.

### 5. Reachability Results
State:
- backend HTTP reachable: `Yes / No`
- auth endpoint reachable: `Yes / No`
- websocket endpoint reachable: `Yes / No`
- app install target available: `Yes / No`

### 6. Remaining Blockers
List only real blockers.

### 7. Final Recommendation
Choose one:
- `Proceed to Batch 01 runtime verification`
- `Need one more environment-unblock pass`
- `Blocked pending external setup`

---

## Non-Negotiable Rules

- Do not say the environment is ready unless backend and supporting services actually run.
- Do not mark websocket reachable unless it was actually checked.
- Do not mark device/emulator available unless install/test target exists.
- Do not drift into feature coding.
- Do not convert missing infrastructure into a product bug report.

---

## Completion Condition

This batch is complete only when runtime QA is realistically executable.

That means:
- backend is running
- DB is running
- Redis is running
- websocket is reachable
- app can be installed and opened on a test target

Until then, this batch remains open.
