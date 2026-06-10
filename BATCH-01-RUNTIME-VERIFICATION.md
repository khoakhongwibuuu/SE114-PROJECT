# [RUNTIME VERIFICATION] BATCH-01-FAMILY-COMMUNITY-RUNTIME

## Purpose
This document defines the strict runtime verification pass for the current highest-priority communication flows in CareNest.

It exists to validate actual behavior on a runnable environment, not code-level assumptions.

This file may be used by:
- a subagent
- Codex directly
- the user during physical-device verification

---

## Batch Scope

Only verify these 2 flows:

1. `Gia đình -> Trò chuyện`
2. `Cộng đồng -> Hội nhóm -> mở phòng chat`

Do not broaden scope into:
- OCR
- AI Care intelligence
- social feed/post detail
- admin
- medicine
- profile

If another issue is discovered outside these two flows, record it as an out-of-scope note only.

---

## Runtime Preconditions

Before executing this batch, confirm all of the following:

- backend is running
- database is reachable
- Redis is reachable if required by backend runtime
- websocket endpoint is reachable
- app build installed on emulator/device is the current build under test
- login is available with at least one account that can:
  - access a family
  - access community groups

If any precondition fails, stop and report the exact blocker. Do not pretend partial inspection is runtime verification.

---

## Test Environment Record

Fill this in before testing:

- Date:
- Tester:
- Device / Emulator:
- Android version:
- Frontend build identifier:
- Backend branch / commit if known:
- Backend running: `Yes / No`
- Database running: `Yes / No`
- Redis running: `Yes / No`
- Websocket reachable: `Yes / No / Unknown`

---

# Flow 1 — Family Chat Runtime Verification

## Entry Path
`Trang chủ -> Gia đình -> Trò chuyện`

## Test Cases

### FC-01 — No family state
If the account has no family:
- open `Gia đình -> Trò chuyện`
- verify the empty state is truthful
- verify CTA does not lead to a dead end

Pass only if:
- no crash
- no misleading claim that a real chat room exists
- CTA behaves correctly

### FC-02 — Selected family room load
If the account has an active family:
- open `Gia đình -> Trò chuyện`
- verify correct family name appears
- verify room header context matches active family
- verify history load succeeds or fails truthfully

Pass only if:
- no crash
- no wrong-family mix-up
- loading/error state is understandable

### FC-03 — Same-family revisit
- open `Gia đình -> Trò chuyện`
- leave the tab
- return to `Gia đình -> Trò chuyện` without switching family

Pass only if:
- messages are not unnecessarily wiped
- room state does not behave like a brand-new room every time
- reconnect behavior is understandable

### FC-04 — Switch active family
- choose/select a different family through the family flow
- return to `Trò chuyện`

Pass only if:
- room context changes to the newly active family
- old family messages do not leak into the new family room
- state is not stale

### FC-05 — Empty-thread behavior
Use a family that has no prior messages if possible.

Pass only if:
- empty state is correct
- revisiting does not produce broken loading loops
- the room still feels valid, not broken

### FC-06 — Send message success path
- send a real message in family chat

Pass only if:
- message appears
- message is not duplicated
- message remains after reload/re-entry if history reload happens
- no false success state is shown

### FC-07 — Reconnect / degraded network behavior
If possible:
- disable network temporarily or simulate backend interruption
- observe reconnect state

Pass only if:
- app does not crash
- state messaging remains understandable
- repeated retries do not create obviously broken UX

---

# Flow 2 — Community Group Chat Runtime Verification

## Entry Path
`Cộng đồng -> Hội nhóm -> chọn nhóm -> mở phòng chat`

## Test Cases

### CG-01 — Group card entry
- open `Cộng đồng -> Hội nhóm`
- tap a group card
- tap any CTA that should also open the room

Pass only if:
- both entry methods open the intended room
- no no-op behavior remains

### CG-02 — Room history load
- enter a community room with existing history if possible

Pass only if:
- room opens
- history loads
- no crash
- loading and empty states are truthful

### CG-03 — Send message success path
- send a real message

Pass only if:
- message appears once
- optimistic echo is not duplicated when websocket/server confirms it
- UI does not regress into a confusing error state

### CG-04 — Fallback send path
If websocket is unavailable but backend REST fallback still works:
- trigger a fallback scenario if possible

Pass only if:
- fallback success is not shown as a red hard error
- user can understand what happened

### CG-05 — Reconnect behavior
Observe room behavior during reconnect conditions.

Pass only if:
- reconnect state is understandable
- app does not look broken
- retry guard does not create a visible infinite loop problem

### CG-06 — Leave/report/moderation actions
Where account permissions allow:
- open room options
- test leave room
- test report flow
- test moderation actions if role allows

Pass only if:
- actions do not crash
- success/failure feedback is truthful

---

## Evidence Rules

For every failed or suspicious test case, provide evidence in one of these forms:
- exact screen path
- exact visible message
- exact observed behavior
- screenshot reference if available
- relevant log note if available

Do not write vague claims like:
- “looks okay”
- “seems stable”
- “probably works”

---

## Severity Rules

Use exactly these severities:

- `P0` — crash, data corruption, impossible to proceed
- `P1` — core action blocked, wrong room/data, severe trust failure
- `P2` — action works but UX is misleading, stale, duplicated, or unreliable
- `P3` — minor polish issue that does not block use

---

## Required Report Format

### 1. Runtime Verification Status
Choose one:
- `Passed with no blocker`
- `Passed with non-blocking defects`
- `Failed with blocker`
- `Blocked by environment`

### 2. Environment Summary
Fill from the environment record.

### 3. Family Chat Results
For each case `FC-01` through `FC-07`:
- `Status: Pass / Fail / Not Executed`
- `Notes:`

### 4. Community Chat Results
For each case `CG-01` through `CG-06`:
- `Status: Pass / Fail / Not Executed`
- `Notes:`

### 5. Defects Found
For each defect:
- `Severity:`
- `Area:`
- `Path:`
- `What happened:`
- `Why it matters:`
- `Evidence:`
- `Suggested fix:`

### 6. Final Recommendation
Choose one:
- `Batch 01 can move forward`
- `Batch 01 needs another stabilization patch`
- `Batch 01 is blocked by environment and cannot be judged yet`

---

## Non-Negotiable Reporting Rules

- Do not call this runtime-verified if backend/websocket were not actually running.
- Do not mark a test case as `Pass` unless it was actually executed.
- Do not collapse `Not Executed` into `Pass`.
- Do not use compile success as runtime evidence.
- Do not mark the batch ready for closure if a P0 or P1 defect exists in either flow.

---

## Exit Condition

This runtime batch is complete only when:
- the environment was real and usable
- both flows were actually executed
- findings were recorded honestly
- remaining risk is based on observed runtime behavior, not assumptions
