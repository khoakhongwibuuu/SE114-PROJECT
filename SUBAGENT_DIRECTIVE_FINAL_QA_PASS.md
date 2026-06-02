# [TECH LEAD DIRECTIVE: FINAL PRODUCT QA PASS]

## CONTEXT

We are in the late stage of the React Native -> Android Kotlin Jetpack Compose migration for **CareNest**.

At this point, multiple functional batches have already been or are being handled separately:

- Community Deep Flow
- Medical Deep Flow
- Global UTF-8 Sweep Round 2

This final directive is for the closing validation phase:

## TARGET PHASE

**Final Product QA Pass**

Your mission is to perform a full Kotlin-vs-legacy parity sweep and identify remaining breakpoints, dead ends, placeholders, routing gaps, unsafe behaviors, and UX inconsistencies before we declare the migrated app “ready for serious testing”.

This is primarily a **verification and stabilization batch**, not a feature invention batch.

---

## SOURCE OF TRUTH

You MUST cross-check the Kotlin frontend against the legacy React Native frontend.

### Legacy RN root

- `D:\DoAn_MB1\CareNest\frontend\CareNestApp\`

### Legacy reference commit

- `c56a8b8ae3ad2477fd11273ffb5aabc3215f279b`

### Kotlin app root

- `D:\DoAn_MB1\CareNest\frontend\app\src\main\java\com\example\carenest\`

---

## STRICT RULES

### DO NOT

- do not introduce new product ideas
- do not redesign screens
- do not make speculative backend assumptions
- do not silently leave dead buttons in place
- do not claim parity if a flow is still only partial

### MUST

- verify the current app against legacy flow-by-flow
- fix safe, local issues directly if they are small and low risk
- report larger mismatches honestly if they require another focused batch
- preserve build stability
- be explicit about what is complete vs. what is still incomplete

### IMPORTANT

If something still depends on mock data, simulated OCR, or safe placeholder behavior:

- keep it truthful
- do not hide it
- report it clearly

---

## QA SCOPE

You must review the app across **four lenses**:

1. UI/UX parity
2. Navigation/routing integrity
3. Data/state behavior
4. Truthfulness / placeholder risk

---

## PART A - UI / UX Parity Sweep

### Check these major areas

- `feature/main/`
- `feature/auth/`
- `feature/family/`
- `feature/medical/`
- `feature/community/`
- `feature/chat/`
- `feature/profile/`
- `feature/notifications/`
- `feature/ekyc/`

### What to verify

- titles / subtitles / labels
- empty states
- loading states
- error states
- buttons and CTA placements
- cards / chips / bottom sheets
- tab labels
- visual consistency against the RN reference
- text overflow or truncation in tight components

### Fix immediately if low risk

- small text issues
- obvious wrong labels
- dead simple spacing bugs
- broken empty/loading/error copy

---

## PART B - Navigation / Routing Integrity

### Review

- `Navigation.kt`
- `NavigationKeys.kt`
- any screen that dispatches navigation events

### What to verify

- every major CTA leads somewhere valid
- no stale route keys are left behind
- no screen expects a route that is never registered
- back navigation behaves safely
- tabs do not trap the user
- modal/bottom-sheet flows dismiss correctly

### Fix immediately if low risk

- obvious dead routes
- trivial missing wiring
- safe back handling issues

### Do not fake route completion

If a route is not ready because backend parity is unknown:

- keep the action safe
- surface a clear temporary message
- report it as remaining work

---

## PART C - Data / State Integrity

### Review

- shared `dashboardViewModel`
- shared `medicineViewModel`
- any current session / active family / active profile state usage
- chat/community ownership logic
- OCR/save flows
- add/update flows for medicines and schedules

### What to verify

- active family/profile is not lost between screens
- cabinet/schedule state refreshes after successful mutation
- chat ownership logic matches verified backend identity
- no screen still depends on stale local dummy arrays where real shared state now exists
- no obviously incorrect fallback data is shown as real

### Fix immediately if low risk

- missing refresh trigger
- wrong state wiring
- safe callback sequencing issues

### Report instead of guessing

If backend contract is not certain:

- do not improvise
- report it as a blocker or follow-up item

---

## PART D - Truthfulness / Placeholder Risk Audit

This is extremely important.

Check whether the migrated Kotlin app still contains any flows that:

- look complete but are actually mocked
- imply backend support that is not real
- silently fail without user feedback
- simulate production AI/medical behavior without proper warning

### Examples to inspect

- OCR simulation
- family chat if not backend-verified
- doctor/community actions
- moderation actions
- medical schedule persistence

### Required behavior

If a flow is partial:

- it must remain usable or safely blocked
- it must not mislead the tester/user

---

## REQUIRED FLOW CHECKLIST

You must sanity-check these flows end to end against RN expectations:

### 1. App entry

- Onboarding
- Login
- Register
- Logout

### 2. Home / family context

- Dashboard loads
- family switch behavior
- profile-dependent routes

### 3. Medical

- Medicine cabinet
- Add medicine
- Medicine schedule
- Add schedule
- OCR simulation path

### 4. Community

- Groups tab
- Wiki feed
- doctor info sheet
- join group
- group chat room

### 5. Profile / settings

- profile info
- medical record
- policy/support/settings entries
- notifications center
- doctor verification navigation

If any of these flows are not testable, explain exactly why.

---

## BUILD VERIFICATION

After the QA pass and any low-risk fixes, you MUST run:

```powershell
cd D:\DoAn_MB1\CareNest\frontend
./gradlew.bat assembleDebug
```

Only hand off if:

- `BUILD SUCCESSFUL`

---

## REPORT FORMAT REQUIRED

Return your result in exactly this structure:

### Completed QA checks
- ...

### Low-risk fixes applied
- ...

### Remaining parity gaps
- ...

### Remaining blockers
- ...

### Build result
- ...

### Release-readiness assessment
- ...

---

## DEFINITION OF DONE

This batch is only considered done if:

- the app builds successfully
- major user flows have been reviewed
- dead buttons/routes have been identified or fixed
- partial/mock flows are clearly called out
- remaining parity gaps are documented honestly

---

## FINAL NOTE

Do not force a false “100% complete” conclusion.

The purpose of this batch is to produce a reliable final state assessment:

- what is truly done
- what is safe
- what still needs one more focused pass

**Accuracy matters more than optimism in this final QA round.**
