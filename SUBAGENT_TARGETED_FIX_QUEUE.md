# [TECH LEAD TASK QUEUE: TARGETED PRODUCT FIXES AFTER CODE-LEVEL QA]

## [CONTEXT]
The latest QA artifact is acceptable only as a **limited code-level QA and architecture audit**.
It is **not** a runtime closure report.

We are now converting that audit into a **targeted product fix queue**.

This queue is ordered by **real user impact**, not by engineering neatness.
Your job is to fix what most directly affects user trust, product honesty, runtime behavior, and communication flows.

Do not widen scope.
Do not open new epics.
Do not spend time on broad cleanup unless it directly supports the queues below.

---

## [MISSION]
Execute the targeted product fixes below in priority order.

You must optimize for:
- honest user experience
- stable chat/community/family flows
- trustworthy social interactions
- reduction of misleading placeholders

---

## [NON-NEGOTIABLE RULES]

### 1. Fix user-facing truth before polish
If a feature is simulated, mocked, deferred, or placeholder-only, the UI must say so clearly.
Do not let the UI imply production-grade capability if the backend/runtime behavior is not actually there.

### 2. Do not claim runtime stability from compile success
Build success is required, but it is not evidence that a chat flow, social interaction, or OCR flow is truly working.

### 3. Do not fake direct messaging
If doctor chat or family chat is not fully implemented, do not simulate a real-time conversation product unless it is actually backed by working logic.

### 4. Stay tightly scoped
This queue is about:
- chat
- community
- family
- social truthfulness
- critical user-facing text

Do not perform unrelated refactors.

### 5. Do not update planning/tracker docs
Do not touch:
- `EPIC_PROGRESS_TRACKER.md`
- any planning notes
- any local directive files

unless explicitly asked after the fix pass.

---

# QUEUE 1 — USER TRUST & PRODUCT HONESTY

## Objective
Remove the most dangerous UX problem first:
**features that look more complete than they really are**.

### 1.1 AI Care honesty
Area:
- `ChatHubScreen.kt`
- AI Care related ViewModel/UI

Required action:
- verify whether AI Care is truly backed by real AI responses or still simulated
- if simulated, add or preserve clear wording that this is not a fully reliable production AI channel
- do not leave the interface looking like a guaranteed live medical assistant if that is not true

Expected result:
- AI Care is either:
  - a real backed feature, or
  - an honest clearly-labeled simulated assistant

### 1.2 OCR honesty
Area:
- `OcrScannerScreen.kt`

Required action:
- ensure the OCR screen clearly communicates when extracted prescription data is mock/demo/simulated
- ensure the confirm/save flow does not imply that OCR recognition itself was real if it was not

Expected result:
- user can understand:
  - what was scanned
  - what was simulated
  - what will actually be saved

### 1.3 Doctor chat honesty
Area:
- `ChatHubScreen.kt`

Required action:
- ensure the `Bác sĩ` tab does not look like a real doctor DM feature unless it actually is one
- if it is still a placeholder, label it clearly
- if it redirects to appointments, the CTA wording must match that truthfully

Expected result:
- no fake "doctor messaging" illusion

### 1.4 Family chat honesty
Area:
- `FamilyFlowScreen.kt`
- `FamilyChatPane.kt`

Required action:
- if family chat is not truly implemented, convert it into an honest and non-broken placeholder
- add clear wording if the feature is pending
- do not leave a dead or ambiguous empty state

Expected result:
- `Gia đình -> Trò chuyện` feels intentional, not broken

---

# QUEUE 2 — COMMUNICATION FLOW STABILIZATION

## Objective
Make the app’s communication surfaces behave reliably enough for internal QA.

### 2.1 Community group entry
Area:
- `CommunityScreen.kt`
- `SocialGroupsPane.kt`
- route wiring into `ChatRoom`

Required action:
- verify that clicking a community group card truly opens the room route
- verify that CTA buttons do the same
- remove any no-op callback behavior

Expected result:
- `Cộng đồng -> Hội nhóm -> mở phòng` is a real usable route

### 2.2 Community room fallback behavior
Area:
- `ChatScreen.kt`
- `ChatViewModel.kt`
- `ChatRepository.kt`

Required action:
- ensure fallback send success is shown as success/informational state, not false error
- ensure reconnect messages are not styled like hard failures
- ensure runtime wording does not confuse the user

Expected result:
- community chat fallback mode remains trustworthy

### 2.3 Realtime risk audit
Area:
- community chat / websocket flow

Required action:
- inspect reconnect loops, message duplication risk, load/reconnect wording, and room state consistency
- if runtime proof is unavailable, explicitly harden the code path where possible and report remaining runtime gaps honestly

Expected result:
- reduced surprise and clearer failure states

### 2.4 Family vs Inbox ownership
Area:
- `FamilyFlowScreen.kt`
- `ChatHubScreen.kt`
- related routing

Required action:
- preserve the new ownership model:
  - `Gia đình -> Trò chuyện`
  - `Tin nhắn -> AI Care / Bác sĩ`
  - community groups remain under `Cộng đồng`

Expected result:
- no IA regressions back into mixed chat ownership

---

# QUEUE 3 — SOCIAL INTERACTION TRUTHFULNESS

## Objective
Make social reactions and comments reflect reality instead of stale or misleading UI.

### 3.1 Feed liked-state truthfulness
Area:
- `SocialModels.kt`
- `SocialFeedScreen.kt`
- `PostCard.kt`

Required action:
- ensure the feed reflects `likedByMe` accurately
- ensure failed like actions do not leave the feed visually lying

Expected result:
- like state in feed matches user/server truth as closely as current architecture allows

### 3.2 Post detail like consistency
Area:
- `PostDetailScreen.kt`
- related ViewModel/repository flow

Required action:
- preserve rollback if like fails
- ensure optimistic state does not remain wrong after failure

Expected result:
- detail like interactions are trustworthy

### 3.3 Comment and reply persistence truthfulness
Area:
- `PostDetailScreen.kt`
- `PostDetailViewModel.kt`
- repository/API integration

Required action:
- ensure top-level comment and nested reply submission produce honest post-action state
- ensure count/header consistency remains correct
- ensure no stale visual count after mutation

Expected result:
- comments/replies look persisted, not fake

### 3.4 Doctor badge consistency
Area:
- feed
- post detail
- comment items

Required action:
- ensure doctor verification badge displays consistently across all relevant social surfaces

Expected result:
- doctor trust markers do not disappear or mismatch by screen

---

# QUEUE 4 — USER-FACING TEXT HOTSPOT CLEANUP

## Objective
Fix the text problems that most damage user trust in the current app state.

### Priority files
- `ChatHubScreen.kt`
- `FamilyChatPane.kt`
- `OcrScannerScreen.kt`
- `HomeDashboardScreen.kt`
- `ChatGroupDirectoryPane.kt`

Required action:
- clean mojibake and broken Vietnamese in these hotspot files
- preserve behavior while repairing wording
- do not claim full encoding victory beyond the touched scope

Expected result:
- most-visible user-facing text becomes readable and trustworthy

---

# QUEUE 5 — RUNTIME ENVIRONMENT UNBLOCK

## Objective
Create the conditions for real runtime verification instead of code-level guesswork.

### 5.1 Backend runtime environment
Required action:
- identify what is currently missing to run the backend locally:
  - Docker
  - Postgres
  - Redis
  - environment config

### 5.2 Local verification strategy
Required action:
- propose a practical path for runtime verification:
  - run real containers, or
  - add a QA/local profile if appropriate

Expected result:
- future QA passes can move from code-level only to runtime-backed evidence

---

## [EXECUTION ORDER]
You must execute in this exact priority order:

1. Queue 1 — User Trust & Product Honesty
2. Queue 2 — Communication Flow Stabilization
3. Queue 3 — Social Interaction Truthfulness
4. Queue 4 — User-Facing Text Hotspot Cleanup
5. Queue 5 — Runtime Environment Unblock

Do not jump to lower queues before addressing higher-impact queues unless a blocking dependency requires it.

---

## [REQUIRED REPORT FORMAT]
When you return, you must use exactly this structure:

# Targeted Fix Queue Report

## 1. Queues Addressed
List which queues you actually worked on.

## 2. Files Changed
List every changed file.

## 3. Fix Summary
Describe what changed and why.

## 4. Remaining Risks
Separate into:
- user trust risks
- runtime risks
- polish risks

## 5. Build Result
Include exact results of:

```powershell
cd D:\DoAn_MB1\CareNest\frontend
.\gradlew.bat assembleDebug
```

and if backend was touched:

```powershell
cd D:\DoAn_MB1\CareNest\backend
.\mvnw.cmd compile
```

## 6. Closure Recommendation
Choose exactly one:
- `Continue with next queue`
- `Needs one more pass on current queue`
- `Ready for runtime validation`

---

## [FORBIDDEN REPORTING]
Do not:
- say "all fixed" unless every touched queue is actually resolved
- say "safe placeholder" unless it is honestly presented and non-misleading
- say "runtime verified" if you only audited code
- broaden the work into unrelated epics

---

## [FINAL INSTRUCTION]
This queue exists to improve the actual product, not the appearance of progress.

Prioritize what a real user would notice first.
