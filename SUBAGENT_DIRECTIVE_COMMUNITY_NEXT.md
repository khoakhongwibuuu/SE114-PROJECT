# [TECH LEAD DIRECTIVE: COMMUNITY DEEP FLOW PARITY - NEXT BATCH]

## CONTEXT

We are continuing the React Native -> Android Kotlin Jetpack Compose migration for **CareNest**.

The previous batch was accepted and completed successfully:

- shared `dashboardViewModel` / `medicineViewModel` were promoted to `Navigation.kt`
- add medicine and add medicine schedule flows were wired more safely
- targeted UTF-8 cleanup was applied
- build verification passed with `./gradlew.bat assembleDebug`

We are now moving to the next phase:

## TARGET PHASE

**Community Deep Flow Parity**

Your mission is to continue the migration by restoring the deeper **Community / Wiki / Chat Hub** flows so they feel significantly closer to the legacy React Native app, while staying safe and truthful about backend parity.

---

## SOURCE OF TRUTH

You MUST use the legacy React Native codebase as the visual and UX reference.

### Legacy RN root

- `D:\DoAn_MB1\CareNest\frontend\CareNestApp\`

### Legacy reference commit

- `c56a8b8ae3ad2477fd11273ffb5aabc3215f279b`

### Kotlin app root

- `D:\DoAn_MB1\CareNest\frontend\app\src\main\java\com\example\carenest\`

---

## STRICT RULES

### DO NOT

- do not invent new UI
- do not redesign flows beyond what RN already established
- do not fake backend-complete behavior if parity is not actually verified
- do not map `family.id` directly into a real chat room unless backend support is explicitly confirmed
- do not introduce dead buttons (`onClick = {}`) without safe feedback
- do not break existing build

### MUST

- compare Kotlin screens with their RN equivalents before editing
- keep Vietnamese text valid UTF-8
- use `LazyColumn` / `LazyRow` with stable `key`
- use IME-safe layout for all input forms / bottom sheets
- build after the batch with Gradle
- report remaining blockers honestly

### CURRENT PRIORITY

We are prioritizing:

1. product completeness
2. UX flow safety
3. text cleanup
4. font/icon perfect parity later

Do not get distracted by final font polishing in this batch.

---

## SCOPE OF THIS BATCH

## PART A - Community Groups Pane

### Kotlin files to inspect

- `D:\DoAn_MB1\CareNest\frontend\app\src\main\java\com\example\carenest\feature\community\presentation\CommunityScreen.kt`
- `D:\DoAn_MB1\CareNest\frontend\app\src\main\java\com\example\carenest\feature\community\presentation\CommunityGroupsPane.kt`

### RN files to inspect

- `D:\DoAn_MB1\CareNest\frontend\CareNestApp\src\screens\community\CommunityGroupsScreen.tsx`
- `D:\DoAn_MB1\CareNest\frontend\CareNestApp\src\screens\community\CommunityGroupPreviewScreen.tsx`
  - if this screen does not exist, inspect equivalent preview/join UI in the RN community flow

### Tasks

1. Make `CommunityGroupsPane` visually and behaviorally closer to RN:
   - list cards
   - joined/discover states
   - host/doctor metadata
   - tags/category display
   - empty state

2. Ensure preview/join flow is not a shell:
   - tapping a group should either:
     - open a proper preview/bottom sheet/modal, or
     - enter the room only if that was the RN behavior and the group is already joined

3. If a join action exists:
   - it must use the actual repository/API call if already available
   - it must show safe feedback on failure

### Acceptance criteria

- no obviously dead join/preview/group entry actions
- no mojibake in Community Groups main flow
- group cards reflect real data when available
- build passes

---

## PART B - Community Wiki Deep Pass

### Kotlin files to inspect

- `D:\DoAn_MB1\CareNest\frontend\app\src\main\java\com\example\carenest\feature\community\presentation\CommunityWikiScreen.kt`
- `D:\DoAn_MB1\CareNest\frontend\app\src\main\java\com\example\carenest\feature\community\data\repository\CommunityRepository.kt`
- `D:\DoAn_MB1\CareNest\frontend\app\src\main\java\com\example\carenest\feature\community\data\remote\CommunityApi.kt`

### RN files to inspect

- `D:\DoAn_MB1\CareNest\frontend\CareNestApp\src\screens\community\CommunityWikiScreen.tsx`
- `D:\DoAn_MB1\CareNest\frontend\CareNestApp\src\screens\community\CreateArticleScreen.tsx`

### Tasks

1. Check full parity of Wiki feed:
   - loading state
   - empty state
   - article card spacing
   - author info
   - tags
   - footer actions

2. Check create-article UX:
   - bottom sheet / modal / form layout
   - keyboard safety
   - image pick/upload state
   - loading button state
   - success behavior after post creation

3. Check like/comment UI:
   - no dead buttons
   - comments sheet behaves safely
   - error feedback is user-visible

### Acceptance criteria

- Wiki feed feels production-usable
- create article flow does not feel placeholder
- no main mojibake left in Wiki flow
- build passes

---

## PART C - Chat Hub & Group Chat Follow-Through

### Kotlin files to inspect

- `D:\DoAn_MB1\CareNest\frontend\app\src\main\java\com\example\carenest\feature\main\presentation\ChatHubScreen.kt`
- `D:\DoAn_MB1\CareNest\frontend\app\src\main\java\com\example\carenest\feature\chat\presentation\ChatScreen.kt`
- `D:\DoAn_MB1\CareNest\frontend\app\src\main\java\com\example\carenest\feature\chat\presentation\components\MessageBubble.kt`

### RN files to inspect

- `D:\DoAn_MB1\CareNest\frontend\CareNestApp\src\navigation\ChatHubNavigator.tsx`
- `D:\DoAn_MB1\CareNest\frontend\CareNestApp\src\screens\community\GroupDetailScreen.tsx`

### Tasks

1. Improve `ChatHubScreen` so it feels less placeholder:
   - review card layout
   - family section empty state
   - AI pane prompts
   - safe actions on cards

2. Improve `ChatScreen` only within confirmed parity bounds:
   - keep group menu alive
   - keep disclaimer / empty state / composer / slow mode polished
   - if leave-group backend is not fully wired, keep the action honest and safe

3. Check `MessageBubble` parity:
   - doctor badge
   - reply-preview block
   - sender identity treatment
   - bubble alignment / time display

### Acceptance criteria

- no dead buttons in main Chat Hub / group chat flow
- no false “family room” behavior pretending to be complete
- no mojibake in the tested chat path
- build passes

---

## PART D - UTF-8 Sweep FOR COMMUNITY SCOPE

### Mandatory files to sweep if needed

- `feature/community/presentation/*`
- `feature/chat/presentation/*`
- `feature/main/presentation/ChatHubScreen.kt`

### Goal

Eliminate strings like:

- `Cá»™ng Ä‘á»“ng`
- `NhÃ³m`
- `Äang`
- `BÃ¡c sÄ©`
- `TrÃ² chuyá»‡n`

All user-facing Vietnamese text in this batch must be valid UTF-8.

---

## BACKEND / DATA SAFETY RULES

If backend parity is uncertain:

- do not pretend a flow is fully connected
- do not hardcode fake success
- do not silently swallow failure

Allowed:

- safe placeholder messaging
- disabled behavior with explanation
- Toast / Snackbar / inline message that honestly explains current limitation

Not allowed:

- fake room entry
- fake join success
- fake moderation actions

---

## BUILD VERIFICATION

After completing the batch, you MUST run:

```powershell
cd D:\DoAn_MB1\CareNest\frontend
./gradlew.bat assembleDebug
```

Only hand off if the result is:

- `BUILD SUCCESSFUL`

---

## REPORT FORMAT REQUIRED

Return your result in exactly this structure:

### Completed
- ...

### Files changed
- ...

### Build result
- ...

### Remaining blockers
- ...

### Recommended next batch
- ...

---

## DEFINITION OF DONE FOR THIS BATCH

This batch is only considered done if:

- Community Groups flow is meaningfully closer to RN
- Wiki flow is not obviously placeholder
- Chat Hub and Group Chat have no dead primary actions
- community/chat UTF-8 issues in main flow are cleaned
- build passes

---

## FINAL NOTE

Do not drift into font perfection or visual micro-polish here.

The goal is:

**complete the product truthfully first, polish later.**
