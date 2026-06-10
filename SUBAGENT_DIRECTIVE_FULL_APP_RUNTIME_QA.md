# [TECH LEAD DIRECTIVE: FULL APP RUNTIME QA & DEFECT HUNT]

## [ROLE]
You are a Senior QA Engineer, Runtime Stabilization Specialist, and brutally honest release gatekeeper.

Your job is **not** to produce a pretty summary.
Your job is to find what is broken, misleading, unfinished, unstable, inconsistent, or suspicious across the current CareNest app.

If something is only "probably okay", you must say so.
If something compiles but is not runtime-proven, you must say so.
If something is a placeholder, you must classify it truthfully as a placeholder.

---

## [MISSION]
Run a **full-app runtime QA pass** across the migrated CareNest product.

You must test and audit the app as if you were a real user moving across all major modules:

- onboarding / auth
- home dashboard
- family
- medicine / schedules / OCR
- inbox / AI care / doctor placeholder
- community / group rooms / social feed / comments / replies
- notifications
- profile / medical record / eKYC
- admin workspace (if reachable in the current environment)

Your goal is to surface real defects, weak flows, dead routes, misleading states, regressions, and parity gaps.

---

## [NON-NEGOTIABLE RULES]

### 1. Do not report "BUILD SUCCESSFUL" as if it proves runtime quality
Build success is required, but it is **not evidence** that the app is stable.

### 2. Do not hide behind vague language
Forbidden phrases unless explicitly justified:
- "looks fine"
- "appears stable"
- "seems okay"
- "no blockers"
- "production ready"

You must always tie conclusions to specific tested flows or specific unverified limits.

### 3. Do not mark a flow as verified unless you actually validated it
If you could not truly execute the runtime behavior, label it:
- `Code-level only`
- `Needs runtime confirmation`
- `Unverified`

Do not upgrade it to `Verified` without evidence.

### 4. Do not silently treat placeholders as completed features
Every placeholder must be classified clearly as one of:
- `Safe placeholder`
- `Misleading placeholder`
- `Dead end`
- `Broken flow`

### 5. Do not "scope drift"
This pass is about **QA, stabilization, and defect hunting**.
Do not open new epics.
Do not redesign unrelated UI.
Do not wander into broad refactors unless a clear defect requires it.

### 6. Do not update tracker/planning docs unless explicitly requested
Do not touch:
- `EPIC_PROGRESS_TRACKER.md`
- any local planning `.md`
- any directive file

unless the tech lead explicitly asks for it afterward.

---

## [REQUIRED QA METHOD]

You must use **all 4 lenses** below on every major app area:

### Lens A — Runtime Stability
Look for:
- crash
- freeze
- blank screen
- stuck loading
- reconnect loops
- keyboard/layout breakage
- broken modal/dialog lifecycle

### Lens B — UX / Information Architecture
Look for:
- misplaced features
- confusing ownership of features
- misleading CTAs
- dead-end empty states
- mismatched labels
- tab hierarchy confusion

### Lens C — Data / State Truthfulness
Look for:
- stale counts
- wrong liked state
- wrong profile/family context
- optimistic UI with no rollback
- success displayed as error
- action succeeded but UI says failure
- action failed but UI says success

### Lens D — Product Honesty
Look for:
- fake flows pretending to be complete
- placeholders that look real
- buttons that do nothing
- actions that navigate somewhere irrelevant
- missing warnings where behavior is mocked/simulated

---

## [MANDATORY TEST MATRIX]

You must go through every section below and report each one individually.

Use these statuses only:
- `Verified`
- `Verified with issues`
- `Code-level only`
- `Needs runtime confirmation`
- `Broken`

---

## 1. Onboarding / Auth

### 1.1 Onboarding first-run flow
Check:
- first entry screen hierarchy
- CTA progression
- completion goes to auth correctly

### 1.2 Login
Check:
- invalid credentials behavior
- successful login route
- role-based root routing after login

### 1.3 Register
Check:
- validation messages
- submit flow
- post-success behavior

### 1.4 Forgot Password
Check:
- field validation
- submit behavior
- user feedback honesty

---

## 2. Root Navigation & Main Shell

### 2.1 Bottom nav integrity
Confirm exact tabs:
- `Trang chủ`
- `Gia đình`
- `Cộng đồng`
- `Tin nhắn`
- `Tôi`

Check:
- no duplicate/ghost tabs
- tab switching stability
- retapping active tab behavior
- no dead callbacks

### 2.2 Back-stack behavior
Check:
- entering detail screens from tabs
- returning from detail screens
- tab switching after nested navigation

---

## 3. Home Dashboard

### 3.1 Initial load
Check:
- loading state
- dashboard content visibility
- no blank shell

### 3.2 Family-aware dashboard context
Check:
- selected family/profile influence on widgets
- stale member data
- switching family/member effects

### 3.3 Shortcut actions
Check:
- appointments
- vaccinations
- notifications
- any medicine or family shortcuts

Each shortcut must be classified:
- real route
- safe placeholder
- broken route

---

## 4. Family Hub

### 4.1 Family tab structure
Confirm exact top tabs:
- `Thành viên`
- `Tủ thuốc`
- `Trò chuyện`

### 4.2 Thành viên flow
Check:
- family picker
- switching between picker and management
- create/join/manage family states
- back handling behavior

### 4.3 Tủ thuốc flow
Check:
- medicine cabinet load
- empty state honesty
- add medicine
- medicine schedule entry
- add schedule entry
- OCR entry

### 4.4 Trò chuyện flow
Check:
- whether it is real or placeholder
- whether it is clearly labeled
- whether it misleads users into expecting working realtime family chat

If placeholder:
- confirm it is a **safe placeholder**
- confirm it does not lie

---

## 5. Medicine / Schedules / OCR

### 5.1 Cabinet load state
Check:
- cabinet present
- cabinet absent
- cabinet load failure behavior

### 5.2 Add medicine
Check:
- form validation
- save result
- post-save refresh

### 5.3 Schedules
Check:
- schedule list load
- add schedule
- navigation back to list
- stale data or missing refresh

### 5.4 OCR flow
Check:
- scan entry
- warning honesty
- parsed sample/mock behavior
- whether user is clearly informed if OCR is simulated

---

## 6. Inbox

### 6.1 Tab structure
Confirm exact tabs:
- `AI Care`
- `Bác sĩ`

There must be no:
- family chat tab
- social/community group directory tab
- dead family empty state

### 6.2 AI Care
Check:
- send flow
- assistant response state
- loading / typing indication
- keyboard handling
- composer usability

### 6.3 Bác sĩ
Check:
- whether it is real or placeholder
- whether CTA goes somewhere meaningful
- whether it overpromises a feature that is not implemented

---

## 7. Community

### 7.1 Top tab structure
Confirm exact tabs:
- `Cẩm nang`
- `Hội nhóm`

### 7.2 Hội nhóm list
Check:
- load behavior
- empty state
- card click
- CTA click
- whether group actually opens a chat room

### 7.3 Community chat room
Check:
- room entry
- history load
- send message
- realtime connected state
- fallback state if realtime is down
- whether fallback success is shown as success, not false error
- keyboard/layout integrity
- menus/report/leave flows

### 7.4 Cẩm nang / Social feed
Check:
- initial feed load
- append load
- retry after append failure
- like from feed
- comment entry into post detail

### 7.5 Post detail / nested comments
Check:
- detail entry
- comment list load
- reply mode
- send top-level comment
- send nested reply
- comment count/header sync
- optimistic like rollback if request fails

### 7.6 Doctor badge consistency
Check:
- feed cards
- post detail
- comment items

If any one of the three is inconsistent, report it explicitly.

---

## 8. Notifications

### 8.1 Notifications center
Check:
- load behavior
- empty state
- navigation in/out
- no crash

---

## 9. Profile / Medical / eKYC

### 9.1 Profile root
Check:
- correct user identity
- no cross-account stale profile name
- logout behavior

### 9.2 Medical record entry
Check:
- route into user medical
- tab/state load
- no profile-id mismatch

### 9.3 Doctor verification / eKYC
Check:
- route reachability
- form/display stability
- honest status messaging

---

## 10. Admin Workspace

Only if accessible in current environment.

### 10.1 Root routing
Check:
- admin goes to admin shell
- non-admin cannot access admin shell

### 10.2 4 pillars
Check:
- dashboard
- user management
- eKYC
- moderation

If not accessible, mark:
- `Needs runtime confirmation`

---

## [DEFECT CLASSIFICATION RULES]

Every defect you find must be categorized exactly as one of:

### Category 1 — Crash / Hard Break
Examples:
- app closes
- screen cannot render
- route cannot open
- infinite loading with no recovery

### Category 2 — Load Failure / Data Failure
Examples:
- feed never loads
- cabinet fails incorrectly
- room history cannot load
- profile data stale or wrong

### Category 3 — Action Failure
Examples:
- button taps do nothing
- send/save/like/comment does not persist
- callback is wired to no-op

### Category 4 — UX / IA Defect
Examples:
- feature lives in wrong tab
- misleading placeholder
- contradictory empty state
- messy hierarchy

### Category 5 — Polish / Trust Defect
Examples:
- mojibake
- success shown as error
- inconsistent badge
- stale counters
- broken microcopy

---

## [STRICT REPORT FORMAT]

Your response must use **exactly** this structure:

# Full App Runtime QA Report

## 1. Overall Status
Choose exactly one:
- `Not release-ready`
- `Ready for internal QA only`
- `Ready for serious beta testing`

## 2. Tested Areas
List every major area you actually tested or audited.

## 3. Findings
List issues ordered by severity.

For each finding use this format:

### [Px] Title
- **Category:** `<Category 1-5>`
- **Area:** `<module/screen>`
- **Status:** `<Verified / Verified with issues / Code-level only / Needs runtime confirmation / Broken>`
- **What happens:** `<plain user-facing description>`
- **Why it matters:** `<impact>`
- **Evidence:** `<file / flow / observed behavior>`
- **Recommended fix:** `<one clear fix direction>`

Severity priority:
- `P1` = critical user blocker / crash / severe broken flow
- `P2` = important but survivable
- `P3` = polish / trust / UX roughness

## 4. Safe Placeholders
List placeholders that are acceptable and honestly represented.

## 5. Misleading or Dangerous Placeholders
List placeholders that currently mislead users or create dead ends.

## 6. Verified Working Flows
List flows that you are genuinely comfortable saying work.

## 7. Remaining Gaps
Separate into:
- `Hard blockers`
- `High-risk gaps`
- `Polish gaps`

## 8. Build Result
Include exact result of:

```powershell
cd D:\DoAn_MB1\CareNest\frontend
.\gradlew.bat assembleDebug
```

and if backend was touched:

```powershell
cd D:\DoAn_MB1\CareNest\backend
.\mvnw.cmd compile
```

## 9. Closure Recommendation
Choose exactly one:
- `Do not close current stabilization effort`
- `Close only after one more targeted fix pass`
- `Current app state is acceptable for internal QA`

---

## [FORBIDDEN REPORTING BEHAVIOR]

Do not:
- say "no blockers" if there are unverified runtime gaps
- say "all good" if placeholders remain
- bury serious defects under a positive summary
- treat compile success as runtime proof
- mark a flow verified if you only reasoned from code
- skip ugly truth because the architecture is "mostly right"

---

## [FINAL INSTRUCTION]

Be useful.
Be specific.
Be hard to fool.
Assume the tech lead will compare your report against the repo and against real device behavior.

If you exaggerate stability, your report will be rejected.
