# [ACTIVE BATCH] BATCH-01-FAMILY-COMMUNITY-RUNTIME

## Context
We are now executing the first guided completion batch from `PROJECT_COMPLETION_ROADMAP.md`.

The product completion critical path currently excludes:
- OCR productionization
- AI Care production intelligence

Your task is to improve real usability, not to broaden scope.

---

## Mission
Act as a Senior Android Product Stabilization Engineer.

You must validate and harden the two highest-priority communication flows:

1. `Gia đình -> Trò chuyện`
2. `Cộng đồng -> Hội nhóm -> mở phòng chat`

Do not work outside this batch unless a directly related fix is required to make these flows usable.

---

## Exact Scope

### Family Chat
Target path:
- `Gia đình`
- `Trò chuyện`

Required outcomes:
- selected family context maps to the correct family chat room
- history loads correctly
- sending a message works or fails truthfully
- reconnect state is understandable
- empty/no-family/no-selected-family states are honest and not misleading

### Community Group Chat
Target path:
- `Cộng đồng`
- `Hội nhóm`
- tap a group card / CTA
- enter the correct room

Required outcomes:
- group entry callback actually routes into the room
- room history loads
- send / reconnect / fallback states are understandable
- no false red-error styling when fallback succeeded

---

## Strict Constraints

1. Do not touch OCR implementation.
2. Do not implement real AI chat.
3. Do not open new epics.
4. Do not update `EPIC_PROGRESS_TRACKER.md`.
5. Do not claim runtime verification if you only performed code inspection.
6. Do not say “all fixed” unless the repo and evidence actually support it.
7. Do not rewrite unrelated product areas.

---

## Required Work Sequence

### Step 1 — Inspect the Current Wiring
Read and verify the current code for:
- `FamilyFlowScreen.kt`
- `FamilyChatPane.kt`
- `FamilyChatViewModel.kt`
- `FamilyChatRepository.kt`
- `FamilyChatWebSocketClient.kt`
- `CommunityScreen.kt`
- `SocialGroupsPane.kt`
- `ChatScreen.kt`
- `ChatViewModel.kt`

You must understand the current behavior before changing anything.

### Step 2 — Stabilize Family Chat Flow
Focus only on defects that impact actual use:
- wrong family binding
- broken loading state
- broken sending state
- broken reconnect state
- misleading empty state
- obvious keyboard/composer/runtime UX defects

### Step 3 — Stabilize Community Group Chat Flow
Fix only what is required for:
- entering the room
- understanding room state
- sending a message
- understanding fallback/reconnect behavior

### Step 4 — Build Verification
Run:

```powershell
cd D:\DoAn_MB1\CareNest\frontend
.\gradlew.bat assembleDebug
```

Do not report completion without a successful build result.

---

## Expected Deliverable Format

Reply in this exact structure:

### 1. Batch Status
Choose one:
- `Accepted for next runtime pass`
- `Partially stabilized`
- `Blocked`

### 2. Files Changed
List every file changed.

### 3. Family Chat Findings and Fixes
State:
- what was broken
- what was fixed
- what still needs runtime confirmation

### 4. Community Chat Findings and Fixes
State:
- what was broken
- what was fixed
- what still needs runtime confirmation

### 5. Remaining Gaps
List only real remaining issues.

### 6. Build Result
Paste the final result summary from `assembleDebug`.

---

## Reporting Rules

- If a flow is only code-level inspected, say so plainly.
- If websocket behavior cannot be runtime-verified, say so plainly.
- If a placeholder remains, say whether it is safe or misleading.
- Never use “clean”, “stable”, or “verified” language loosely.

---

## Completion Condition for This Batch

This batch is considered successful only if:
- the repo builds
- the family chat flow is materially more usable than before
- the community group entry/chat flow is materially more trustworthy than before
- the report is honest about what still needs runtime confirmation
