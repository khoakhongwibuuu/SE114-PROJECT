# [TECH LEAD DIRECTIVE: MEDICAL DEEP FLOW PARITY - NEXT BATCH]

## CONTEXT

We are continuing the React Native -> Android Kotlin Jetpack Compose migration for **CareNest**.

The previous Community Deep Flow batch has been accepted:

- community/chat actions were deepened
- `authorId` backend contract was verified against Spring Boot
- `isMe` logic is now correctly locked to account-level `currentUserId`
- build verification passed

We are now moving to the next product-completion branch:

## TARGET PHASE

**Medical Deep Flow Parity**

Your mission is to bring the medical flows closer to the legacy React Native app, with special focus on:

- Medicine Cabinet behavior
- Add Medicine flow
- Medicine Schedule flow
- Add Medicine Schedule flow
- OCR scanner and post-scan review flow

The goal is not visual reinvention. The goal is truthful, stable parity progress.

---

## SOURCE OF TRUTH

You MUST use the legacy React Native codebase as the UI/UX reference.

### Legacy RN root

- `D:\DoAn_MB1\CareNest\frontend\CareNestApp\`

### Legacy reference commit

- `c56a8b8ae3ad2477fd11273ffb5aabc3215f279b`

### Kotlin app root

- `D:\DoAn_MB1\CareNest\frontend\app\src\main\java\com\example\carenest\`

---

## STRICT RULES

### DO NOT

- do not invent new UI or new steps not present in RN unless required for safe backend limitations
- do not fake OCR success if actual OCR parsing is not confirmed
- do not silently mark incomplete flows as fully done
- do not regress currently working add medicine / add schedule behavior
- do not break build

### MUST

- inspect RN screen(s) before editing Kotlin counterpart
- preserve valid UTF-8 Vietnamese
- use IME-safe layout for all forms and sheets
- use stable keys in lists
- build after the batch
- report blockers honestly

### CURRENT PRIORITY

We are still prioritizing:

1. product completeness
2. correct flow behavior
3. safe data/state handling
4. text cleanup
5. font/icon perfection later

---

## SCOPE OF THIS BATCH

## PART A - Medicine Cabinet Flow Hardening

### Kotlin files to inspect

- `D:\DoAn_MB1\CareNest\frontend\app\src\main\java\com\example\carenest\feature\medical\presentation\MedicineScreen.kt`
- `D:\DoAn_MB1\CareNest\frontend\app\src\main\java\com\example\carenest\feature\medical\presentation\AddMedicineScreen.kt`
- `D:\DoAn_MB1\CareNest\frontend\app\src\main\java\com\example\carenest\feature\medical\presentation\MedicineViewModel.kt`

### RN files to inspect

- `D:\DoAn_MB1\CareNest\frontend\CareNestApp\src\screens\medicine\MedicineCabinetScreen.tsx`
- `D:\DoAn_MB1\CareNest\frontend\CareNestApp\src\screens\medicine\AddMedicineToCabinetScreen.tsx`

### Tasks

1. Re-check `MedicineScreen` parity against RN:
   - cabinet list cards
   - warning/banner blocks
   - OCR CTA block
   - add/schedule affordances
   - empty state / loading state / error state

2. Re-check `AddMedicineScreen` behavior:
   - form labels / placeholder / unit chips / CTA
   - keyboard safety
   - loading state while submitting
   - success / failure feedback
   - confirm that cabinet refresh is visible after returning

3. Remove any remaining “dummy feel” from the flow if the data is already available from state/API.

### Acceptance criteria

- add medicine flow feels stable and intentional
- cabinet updates are visible after successful creation
- no dead actions in cabinet main flow
- no mojibake in cabinet/add medicine flow

---

## PART B - Medicine Schedule Flow Parity

### Kotlin files to inspect

- `D:\DoAn_MB1\CareNest\frontend\app\src\main\java\com\example\carenest\feature\medical\presentation\MedicineScheduleScreen.kt`
- `D:\DoAn_MB1\CareNest\frontend\app\src\main\java\com\example\carenest\feature\medical\presentation\AddMedicineScheduleScreen.kt`
- `D:\DoAn_MB1\CareNest\frontend\app\src\main\java\com\example\carenest\feature\medical\presentation\MedicineViewModel.kt`

### RN files to inspect

- `D:\DoAn_MB1\CareNest\frontend\CareNestApp\src\screens\medicine\MedicineScheduleScreen.tsx`
- `D:\DoAn_MB1\CareNest\frontend\CareNestApp\src\screens\medicine\AddMedicineScheduleScreen.tsx`

### Tasks

1. Bring `MedicineScheduleScreen` closer to RN:
   - schedule card layout
   - next dose / reminder display
   - empty state
   - CTA positioning
   - spacing and section hierarchy

2. Harden `AddMedicineScheduleScreen`:
   - family member selector from real state
   - cabinet medicine selector from real state
   - dosage/frequency/date/note layout parity
   - loading state during save
   - success/error feedback

3. Verify schedule creation truly updates the schedule list after returning.

### Acceptance criteria

- schedule list and create schedule flow feel coherent
- add schedule no longer feels partially mocked
- no dead save action
- no mojibake in main schedule flow

---

## PART C - OCR Scanner Truthful Parity

### Kotlin files to inspect

- `D:\DoAn_MB1\CareNest\frontend\app\src\main\java\com\example\carenest\feature\medical\presentation\OcrScannerScreen.kt`

### RN files to inspect

- `D:\DoAn_MB1\CareNest\frontend\CareNestApp\src\screens\medicine\OcrScannerScreen.tsx`

### Tasks

1. Compare the Kotlin OCR screen with RN:
   - scanner framing
   - action buttons
   - gallery/camera affordances
   - post-scan editable review UI
   - loading / retry / error states

2. If actual OCR backend flow is not fully wired:
   - keep the UI truthful
   - do not present fake parsed medicine data as if it were real OCR output
   - use clear user feedback when the action is still limited

3. If any existing mock values remain:
   - reduce false realism
   - keep the path usable without lying about capability

### Acceptance criteria

- OCR screen no longer feels like a misleading fake success flow
- user can understand the current state of the feature clearly
- UI stays close to RN structure
- build passes

---

## PART D - UTF-8 Sweep FOR MEDICAL SCOPE

### Mandatory files to sweep if needed

- `feature/medical/presentation/*`
- any related helper or model file containing user-facing strings

### Goal

Eliminate remaining strings like:

- `Thuá»‘c`
- `LÆ°u lá»‹ch`
- `Quay láº¡i`
- `Háº¡n sá»­ dá»¥ng`
- `Nháº­p tay`

All user-visible Vietnamese in this batch must be valid UTF-8.

---

## DATA / STATE SAFETY RULES

When wiring medical flows:

- prefer real shared `medicineViewModel`
- prefer real `dashboardViewModel` / session-driven member context
- do not reintroduce local dummy member/medicine arrays if real state exists

If backend/state is incomplete:

- show a truthful fallback
- keep the screen safe
- do not fabricate completed medical records or OCR parsing

---

## BUILD VERIFICATION

After completing this batch, you MUST run:

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

- cabinet flow is stable
- add medicine flow is real and safe
- schedule flow is materially closer to RN
- OCR flow is honest and clearer
- medical UTF-8 issues in main flow are cleaned
- build passes

---

## FINAL NOTE

Do not over-polish fonts or micro-visual details here.

The objective is:

**complete the medical product flow truthfully first, polish later.**
