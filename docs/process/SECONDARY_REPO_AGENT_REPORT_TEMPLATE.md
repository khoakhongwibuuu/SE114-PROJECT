# Secondary Repo Agent Report Template

Copy this template exactly when reporting a promotion-ready batch from `CareNest_KL`.

---

# Batch Report

## 1. Batch Status

Choose exactly one:

- `PASS`
- `PARTIAL`
- `BLOCKED`
- `NOT READY FOR PROMOTION`

## 2. Batch Title

Short feature or fix name.

## 3. Goal

One paragraph only:

- what this batch was supposed to achieve
- which user flow it changes

## 4. Files Changed

List exact files only. No summaries mixed into this section.

Example:

- `frontend/app/src/main/java/.../DoctorProfileScreen.kt`
- `backend/src/main/java/.../BookingServiceImpl.java`

## 5. Exact Flow Changed

List concrete user flows only.

Good:

- User opens Doctor Profile and sends booking request
- Doctor confirms schedule and appointment ledger is created

Bad:

- Improved scheduling logic
- Refactored flow management

## 6. Scope Declaration

Write exactly one line:

- `No hidden scope changes: YES`
- `No hidden scope changes: NO`

If `NO`, list the extra files or side effects immediately below.

## 7. Build Result

Must include exact command and outcome.

Example:

- Backend: `.\mvnw.cmd compile` -> `BUILD SUCCESS`
- Frontend: `.\gradlew.bat assembleDebug` -> `BUILD SUCCESSFUL`

## 8. Runtime QA Result

For each checkpoint:

- `PASS`
- `FAIL`
- `NOT TESTED`
- `BLOCKED`

Format:

- `Checkpoint 1 - Patient creates booking: PASS`
- `Checkpoint 2 - Doctor confirms booking: PASS`

Do not say "all good" without checkpoint lines.

## 9. Defects Found

If none:

- `None found in this batch`

If defects remain, list them clearly with impact.

## 10. Known Gaps

List what is intentionally not solved.

Examples:

- Patient-side cancellation not included in this batch
- No message persistence migration in this batch

## 11. Promotion Recommendation

Choose exactly one:

- `Ready for promotion into primary repo`
- `Needs review before promotion`
- `Do not promote`

## 12. Evidence Notes

Optional but encouraged:

- screenshots
- log lines
- exact API behavior
- observed runtime caveats

---

# Forbidden Reporting Behavior

Do not do any of the following:

- do not say "complete" if any checkpoint is untested
- do not hide extra files outside scope
- do not claim runtime success from compile-only evidence
- do not mark `PASS` if build failed
- do not summarize vague confidence instead of exact outcomes

# Mandatory Honesty Rule

If something was not tested, say `NOT TESTED`.

If something is blocked by environment, say `BLOCKED`.

If the batch works only at code level, say so directly.
