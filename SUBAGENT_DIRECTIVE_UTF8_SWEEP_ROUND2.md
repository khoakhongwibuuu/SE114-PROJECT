# [TECH LEAD DIRECTIVE: GLOBAL UTF-8 SWEEP ROUND 2]

## CONTEXT

We are continuing the React Native -> Android Kotlin Jetpack Compose migration for **CareNest**.

Core flows are gradually being restored, but Vietnamese text corruption (mojibake) still appears in multiple screens outside the most recently touched modules.

This batch is dedicated to:

## TARGET PHASE

**Global UTF-8 Sweep Round 2**

Your mission is to aggressively clean remaining corrupted Vietnamese strings across the Kotlin frontend, while preserving behavior and avoiding unrelated refactors.

This is a cleanup-and-stability batch, not a feature batch.

---

## SOURCE OF TRUTH

You MUST use the legacy React Native codebase as the wording/UI reference whenever possible.

### Legacy RN root

- `D:\DoAn_MB1\CareNest\frontend\CareNestApp\`

### Legacy reference commit

- `c56a8b8ae3ad2477fd11273ffb5aabc3215f279b`

### Kotlin app root

- `D:\DoAn_MB1\CareNest\frontend\app\src\main\java\com\example\carenest\`

---

## STRICT RULES

### DO NOT

- do not redesign screens
- do not change business logic unless the corrupted string blocks a UI behavior
- do not refactor architecture in this batch
- do not introduce new routes, APIs, or backend assumptions
- do not “clean up” unrelated code just because you are in the file

### MUST

- fix corrupted Vietnamese strings to valid UTF-8
- preserve current working behavior
- keep labels, button text, placeholders, empty states, and alerts readable and natural
- use RN wording as reference when the equivalent screen exists
- build after the batch

### PRIORITY OF THIS BATCH

1. remove mojibake in user-visible screens
2. keep app stable
3. avoid scope creep

This batch is about **clarity and trust**, not feature expansion.

---

## PRIMARY SWEEP TARGETS

You MUST scan and fix at least the following areas:

### A. Main / Shell

- `D:\DoAn_MB1\CareNest\frontend\app\src\main\java\com\example\carenest\feature\main\presentation\MainScreen.kt`
- `D:\DoAn_MB1\CareNest\frontend\app\src\main\java\com\example\carenest\feature\main\presentation\HomeDashboardScreen.kt`
- `D:\DoAn_MB1\CareNest\frontend\app\src\main\java\com\example\carenest\feature\main\presentation\ChatHubScreen.kt`

### B. Profile / Settings

- `D:\DoAn_MB1\CareNest\frontend\app\src\main\java\com\example\carenest\feature\profile\presentation\ProfileScreen.kt`
- `D:\DoAn_MB1\CareNest\frontend\app\src\main\java\com\example\carenest\feature\profile\presentation\UserMedicalScreen.kt`
- `D:\DoAn_MB1\CareNest\frontend\app\src\main\java\com\example\carenest\feature\profile\presentation\PolicyScreen.kt`

### C. Notifications

- `D:\DoAn_MB1\CareNest\frontend\app\src\main\java\com\example\carenest\feature\notifications\presentation\NotificationsCenterScreen.kt`

### D. Medical

- all user-facing files under:
  - `D:\DoAn_MB1\CareNest\frontend\app\src\main\java\com\example\carenest\feature\medical\presentation\`

### E. Community / Chat

- all user-facing files under:
  - `D:\DoAn_MB1\CareNest\frontend\app\src\main\java\com\example\carenest\feature\community\presentation\`
  - `D:\DoAn_MB1\CareNest\frontend\app\src\main\java\com\example\carenest\feature\chat\presentation\`

---

## WHAT TO FIX

You must search for and eliminate strings such as:

- `ThÃ´ng tin`
- `Gia Ä‘Ã¬nh`
- `Cá»™ng Ä‘á»“ng`
- `Nháº¯n`
- `Quay láº¡i`
- `Há»— trá»£`
- `Äang`
- `BÃ¡c sÄ©`
- `Thuá»‘c`
- `LÆ°u lá»‹ch`
- `Sá»‘ Ä‘iá»‡n thoáº¡i`

This includes:

- titles
- subtitles
- button labels
- placeholders
- helper text
- dialog/alert text
- bottom sheet actions
- error messages
- empty states
- section labels

---

## Wording Guidance

When fixing text:

- prefer the exact RN wording if the RN equivalent exists
- otherwise use natural, production-friendly Vietnamese
- do not over-literal translate technical phrases
- do not leave mixed-language labels unless the legacy app already did so intentionally

Examples:

- `Trung tâm hỗ trợ`
- `Thông tin cá nhân`
- `Quay lại`
- `Thêm vào tủ thuốc`
- `Lưu lịch`
- `Cộng đồng`

---

## SCOPE SAFETY

If you touch a file:

- keep the logic intact unless a text-related fix requires tiny structural adjustment
- do not rewrite large sections unnecessarily
- do not reintroduce placeholders or regress working UI

Allowed:

- replacing corrupted strings
- small label cleanup
- tiny support changes to make a string render correctly

Not allowed:

- turning this into a visual redesign batch
- changing navigation behavior unless absolutely necessary to fix a text-bound dialog or action label

---

## OPTIONAL SECONDARY SWEEP

If the primary targets are fully cleaned and build remains healthy, you may also scan:

- domain/model files containing user-facing fallback strings
- repository/viewmodel files that emit user-visible Toast/snackbar/error messages

But keep this secondary sweep focused only on user-visible text.

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

### Remaining mojibake hotspots
- ...

### Recommended next batch
- ...

---

## DEFINITION OF DONE

This batch is only considered done if:

- obvious mojibake is removed from the major user-facing screens
- the cleaned text reads naturally in Vietnamese
- no new feature or architecture drift is introduced
- the app still builds successfully

---

## FINAL NOTE

This batch is not about perfection in typography or iconography.

It is about removing text corruption that damages trust and testability.

**Clean Vietnamese first. Keep everything else stable.**
