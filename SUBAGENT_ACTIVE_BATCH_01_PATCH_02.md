# [PATCH 02] BATCH-01-FAMILY-COMMUNITY-RUNTIME

## Context
Patch 01 improved the code-level behavior of:
- family chat state retention
- community chat optimistic echo handling

Codex has already fixed one additional small defect directly:
- empty family chat threads no longer needlessly reload on every tab revisit when the same family context is still active

This patch is now strictly for runtime-facing hardening and repo-accurate verification.

---

## Mission
Act as a Runtime Stabilization Engineer.

Do not broaden scope.

Your only responsibility is to harden and verify the current implementation of:
1. `Gia đình -> Trò chuyện`
2. `Cộng đồng -> Hội nhóm -> phòng chat`

---

## Exact Focus

### Family Chat
Validate and harden:
- same-family revisit behavior
- family switching behavior
- empty-thread behavior
- message send / reconnect / error messaging

### Community Group Chat
Validate and harden:
- room entry
- room history load
- send / reconnect / fallback status
- duplicate suppression under real room usage

---

## Strict Constraints

1. Do not touch OCR.
2. Do not touch AI chat intelligence.
3. Do not open a new batch or epic.
4. Do not refactor unrelated modules.
5. Do not claim runtime verification without real runnable evidence.
6. Do not say “fully stable” if websocket behavior is still only code-level inferred.

---

## Required Tasks

### Task 1 — Inspect Current Code State
Review only the files needed for this batch:
- `FamilyChatPane.kt`
- `FamilyChatViewModel.kt`
- `FamilyChatRepository.kt`
- `FamilyChatWebSocketClient.kt`
- `FamilyFlowScreen.kt`
- `CommunityScreen.kt`
- `SocialGroupsPane.kt`
- `ChatScreen.kt`
- `ChatViewModel.kt`

### Task 2 — Address Small Remaining Code-Level Risks
If you find a small, directly related issue in these flows, fix it.

Examples of acceptable fixes:
- retry/backoff guard improvement
- state hint cleanup
- send-state edge case cleanup
- room-entry edge case

Examples of unacceptable scope expansion:
- new social features
- admin changes
- medicine changes
- OCR/AI work

### Task 3 — Build Verification
Run:

```powershell
cd D:\DoAn_MB1\CareNest\frontend
.\gradlew.bat assembleDebug
```

---

## Required Report Format

### 1. Patch Status
Choose one:
- `Ready for runtime-only verification`
- `Partially hardened`
- `Blocked`

### 2. Files Changed
List every file changed.

### 3. Family Chat Runtime-Facing Notes
State:
- what is now code-level sound
- what still requires live backend/runtime proof

### 4. Community Chat Runtime-Facing Notes
State:
- what is now code-level sound
- what still requires live backend/runtime proof

### 5. Remaining Risks
List only real remaining risks.

### 6. Build Result
Paste the final summary from `assembleDebug`.

---

## Success Condition
This patch is successful only if:
- the repo still builds
- no new scope drift was introduced
- the remaining gaps are narrowed to runtime/backend-dependent behavior rather than obvious code-level defects
